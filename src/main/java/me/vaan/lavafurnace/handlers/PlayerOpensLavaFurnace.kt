package me.vaan.lavafurnace.handlers

import me.vaan.lavafurnace.dataclasses.Coords
import me.vaan.lavafurnace.dataclasses.FurnacesDB
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class PlayerOpensLavaFurnace(private val f: FurnacesDB) : Listener {
    private val guiname = "§4Lava Furnace GUI"

    @EventHandler
    fun openFurnace(e: PlayerInteractEvent) {
        val player = e.player
        val clicked = e.clickedBlock
        val furn = clicked?.state as? Furnace ?: return
        //val name = furn.customName() as TextComponent

        //region Guards
        if(furn.customName!= "§4Lava Furnace") return
        if(e.action != Action.RIGHT_CLICK_BLOCK) return

        if(!player.isSneaking) return

        if(clicked.blockData.material != Material.FURNACE) return
        //endregion

        e.isCancelled = true
        val c = Coords(clicked.location)

        //region first gui creation
        val elements = ArrayList<Pair<Material,String>>()
        elements.add(Pair(Material.BUCKET, "§4Upgrade Fuel Capacity"))
        elements.add(Pair(Material.SUGAR, "§4Upgrade Speed"))
        elements.add(Pair(Material.COAL, "§4Fuel"))
        elements.add(Pair(Material.LIGHT_GRAY_STAINED_GLASS, "$c"))

        val gui = create9Gui(guiname, elements)
        //endregion

        player.openInventory(gui)
    }

    private fun create9Gui(s: String, e: ArrayList<Pair<Material, String>>) : Inventory {
        val gui = Bukkit.createInventory(null, 9, s)
        e.forEach {
            val (a, b) = it
            val stack = ItemStack(a, 1)
            stack.editMeta { x -> x.displayName(Component.text(b)) }
            gui.addItem(stack)
        }

        return gui
    }

    @EventHandler
    fun clickItem(e: InventoryClickEvent) {
        val view = e.view

        if(view.title != guiname) return
        e.isCancelled = true

        when(e.currentItem?.itemMeta?.displayName) {
            "§4Fuel" -> fuel(e)
            "§4Upgrade Fuel Capacity" -> upgradeCapacity(e)
            "§4Upgrade Speed" -> upgradeSpeed(e)
        }
    }

    private fun upgradeSpeed(e: InventoryClickEvent) {
        val player = e.view.player

        //region Requirements
        var counter = 0
        player.inventory.forEach {
            if (it==null) return@forEach
            if(it.type==Material.FURNACE && !it.itemMeta.hasDisplayName()) counter+=it.amount
        }

        val d = player.inventory.contains(Material.DIAMOND)

        if(counter < 8 || !d) {
            player.sendMessage("§4You need to have 8 furnaces and 1 diamond to upgrade the furnace speed")
            return
        }

        val item = e.inventory.getItem(3)
        if(item==null) {
            Bukkit.getLogger().info("Glass Pane not found")
            return
        }

        val coords = getCoords(item)
        val (ind, values) = f.getLine(coords)

        if(values.speed == 10) {
            player.sendMessage("§4Maximum speed level reached!")
            return
        }
        //endregion

        player.inventory.removeItem(ItemStack(Material.FURNACE, 8))
        player.inventory.removeItem(ItemStack(Material.DIAMOND, 1))

        values.speed++
        player.sendMessage("§4Speed upgraded to ${values.speed}")
        f.updateLine(ind, values)
    }

    private fun upgradeCapacity(e: InventoryClickEvent) {
        val player = e.view.player

        //region Requirements
        var counter = 0
        player.inventory.forEach {
            if (it==null) return@forEach
            if(it.type==Material.COPPER_BLOCK) counter+=it.amount
        }

        if(counter < 16) {
            player.sendMessage("§4You need to have 16 copper block to upgrade the furnace capacity")
            return
        }

        player.inventory.removeItem(ItemStack(Material.COPPER_BLOCK, 16))
        //endregion

        val item = e.inventory.getItem(3)
        if(item==null) {
            Bukkit.getLogger().info("Glass Pane not found")
            return
        }

        val coords = getCoords(item)

        val (ind, line) = f.getLine(coords)

        line.fuelMax+=100
        f.updateLine(ind, line)

        player.sendMessage("§4You have upgraded the lava furnace, the new capacity is ${line.fuelMax}")
    }


    private fun fuel(e: InventoryClickEvent) {
        val player = e.view.player

        //region Requirements
        if (!player.inventory.contains(Material.LAVA_BUCKET)) {
            player.sendMessage("§4You don't have any lava buckets to fill the furnace with!")
            return
        }
        //endregion

        //region Getting Data
        val item = e.inventory.getItem(3)
        if (item == null) {
            Bukkit.getLogger().info("Glass Pane not found")
            return
        }

        val coords = getCoords(item)

        val (ind, line) = f.getLine(coords)
        val (_, lavaAmount, lavaMax, _) = line
        //endregion

        if (lavaAmount == lavaMax) {
            player.sendMessage("§4The furnace is already full!")
            return
        }

        player.inventory.removeItem(ItemStack(Material.LAVA_BUCKET, 1))
        player.inventory.addItem(ItemStack(Material.BUCKET, 1))

        val newLava = if (lavaAmount + 100 > lavaMax) lavaMax else lavaAmount + 100
        line.fuel = newLava

        player.sendMessage("§4Added 100 cB, furnace has now $newLava cB of fuel.")
        f.updateLine(ind, line)
    }

    private fun getCoords(item: ItemStack): Coords {
        val s = item.itemMeta.displayName.split(" ")
        return Coords(s[0], s[1].toInt(), s[2].toInt(), s[3].toInt())
    }
}
