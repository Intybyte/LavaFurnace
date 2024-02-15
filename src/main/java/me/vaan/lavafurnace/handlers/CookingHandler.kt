package me.vaan.lavafurnace.handlers

import me.vaan.lavafurnace.dataclasses.Coords
import me.vaan.lavafurnace.dataclasses.FurnacesDB
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.event.inventory.FurnaceStartSmeltEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.FurnaceInventory


class CookingHandler(private val f: FurnacesDB) : Listener {


    @EventHandler
    fun onSmelt(e: FurnaceSmeltEvent) {
        val furnace = e.block.state as? Furnace ?: return

        if (furnace.customName != "§4Lava Furnace") return
        val c = Coords(furnace.location)


        val (ind, values) = f.getLine(c)

        if (values.fuel == 0) {
            furnace.burnTime = 0
            furnace.update()
            return
        }

        values.fuel--
        f.setLine(ind, values)
    }

    @EventHandler
    fun onStartSmelt(e: FurnaceStartSmeltEvent) {
        val furnace = e.block.state as? Furnace ?: return
        if (furnace.customName != "§4Lava Furnace") return

        val c = Coords(furnace.location)
        val (_, values) = f.getLine(c)


        furnace.cookTime= ((values.speed - 1) * 20).toShort()
        furnace.update()
    }

    @EventHandler
    fun openFurnace(e: PlayerInteractEvent) {
        val player = e.player
        val clicked = e.clickedBlock
        val furn = clicked?.state as? Furnace ?: return
        //val name = furn.customName() as TextComponent

        //region Guards
        if (furn.customName != "§4Lava Furnace") return
        if (e.action != Action.RIGHT_CLICK_BLOCK) return

        if (player.isSneaking) return

        if (clicked.blockData.material != Material.FURNACE) return
        //endregion

        val c = Coords(clicked.location)

        //region Burn condition
        val (_, values) = f.getLine(c)
        //val values = line.split(";")
        val (_, lavaAmount, _, _) = values

        val furnData = clicked.blockData as org.bukkit.block.data.type.Furnace

        val check = lavaAmount == 0
        furnData.isLit = !check
        clicked.blockData = furnData
        furn.burnTime = if (check) 0 else Short.MAX_VALUE
        furn.update()
        //endregion
    }

    @EventHandler
    fun tryAddFuel(e: InventoryClickEvent) {
        val furnInv = e.view.topInventory as? FurnaceInventory ?: return
        val furn = (furnInv.holder ?: return)

        if(furn.customName!="§4Lava Furnace") return

        if(e.slot==1) e.isCancelled=true
        //if ()
    }
}