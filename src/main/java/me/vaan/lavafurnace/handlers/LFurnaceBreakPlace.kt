package me.vaan.lavafurnace.handlers

import me.vaan.lavafurnace.dataclasses.Coords
import me.vaan.lavafurnace.dataclasses.FurnacesDB
import me.vaan.lavafurnace.dataclasses.LavaFurnaceData
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

class LFurnaceBreakPlace(private val f: FurnacesDB) : Listener {
    private val d = ";"

    @EventHandler
    fun placeBlock(e: BlockPlaceEvent) {
        val item = e.itemInHand

        if(item.itemMeta.displayName != "§4Lava Furnace") {
            return
        }

        val b = e.blockPlaced

        var furnaceData = ""

        if(item.lore==null) {
            Bukkit.getLogger().info("Lava furnace has no data.")
            return
        }

        val fuel = item.lore!![0].split(" ")[1].toInt()
        val fuelMax = item.lore!![1].split(" ")[1].toInt()
        val speed = item.lore!![2].split(" ")[1].toInt()

        //item.lore?.forEach { string -> furnaceData= "$furnaceData$string$d" }

        val values = LavaFurnaceData(Coords(b.location), fuel, fuelMax, speed)
        //Bukkit.getLogger().info(placing)
        f.addLine(values)

    }

    @EventHandler
    fun breakBlock(e: BlockBreakEvent) {
        val block = e.block
        val furn = block.state as? Furnace ?: return
        //val name = furn.customName() as TextComponent

        if(furn.customName != "§4Lava Furnace") {
            return
        }

        e.isDropItems = false

        //lines.forEach { s -> Bukkit.getLogger().info("File = $s\n") }

        val blockBroken = Coords(block.location)
        val (index, _) = f.getLine(blockBroken)
        val lore = f.getAsList(index)
        f.setLine(index, null)

        //Bukkit.getLogger().info(lore.toString())

        giveFurnace(e.player.inventory, lore)
    }

    private fun giveFurnace(inv: PlayerInventory, lore: ArrayList<String>) {
        val furnace = ItemStack(Material.FURNACE,1)
        val furnName = Component.text("§4Lava Furnace")

        furnace.editMeta { m ->
            m.displayName(furnName)
            m.lore = lore
        }

        inv.addItem(furnace)
    }
}