package me.vaan.lavafurnace

import me.vaan.lavafurnace.dataclasses.FurnacesDB
import me.vaan.lavafurnace.handlers.CookingHandler
import me.vaan.lavafurnace.handlers.LFurnaceBreakPlace
import me.vaan.lavafurnace.handlers.PlayerOpensLavaFurnace
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class LavaFurnace : JavaPlugin() {
    private lateinit var furnacesFile: File
    private lateinit var config: FileConfiguration
    private lateinit var furnaceDB: FurnacesDB

    override fun onEnable() {
        furnacesFile = File(dataFolder, "furnaces.csv")

        if(!dataFolder.exists()) dataFolder.mkdirs()
        if(!furnacesFile.exists()) furnacesFile.createNewFile()
        if(!File(dataFolder, "config.yml").exists()) saveDefaultConfig()

        config = this.getConfig()

        furnaceDB = FurnacesDB(furnacesFile)
        furnaceDB.loadValues()

        server.pluginManager.registerEvents(PlayerOpensLavaFurnace(furnaceDB), this)
        server.pluginManager.registerEvents(LFurnaceBreakPlace(furnaceDB), this)
        server.pluginManager.registerEvents(CookingHandler(furnaceDB), this)
        addRecipe()
    }


    override fun onDisable() {
        furnaceDB.saveValues()
    }
    private fun addRecipe() {
        val furnace = ItemStack(Material.FURNACE,1)
        val name = Component.text("§4Lava Furnace")

        //region Lore Setup
        val lore = ArrayList<String>()

        lore.add("Fuel: 0 cB")
        lore.add("FuelMax: 100 cB")
        lore.add("SpeedLevel: 1")

        furnace.editMeta { m ->
            m.displayName(name)
            m.lore = lore
        }
        //endregion

        val keyName = config.getString("recipe.key")
        val key = NamespacedKey(this, keyName ?: "lava_furnace")

        val recipeIngredients = (config.getList("recipe.craft") as MutableList<String>).toTypedArray()
        val recipe = ShapedRecipe(key, furnace)
        recipe.shape(*recipeIngredients)


        val association = config.getList("recipe.materialReplace") as MutableList<MutableList<*>>
        association.forEach {
            val c =  it[0].toString()[0]
            val material = Material.getMaterial(it[1] as String)
            if (material == null) {
                logger.info("§4Material not loaded properly")
                return@forEach
            }
            recipe.setIngredient(c, material)
        }

        /*recipe.setIngredient('b', Material.BUCKET)
        recipe.setIngredient('c', Material.BLACKSTONE)
        recipe.setIngredient('x', Material.FURNACE)
        recipe.setIngredient('a', Material.COPPER_BLOCK)*/
        server.addRecipe(recipe)
    }
}
