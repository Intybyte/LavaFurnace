package me.vaan.lavafurnace.dataclasses

import org.bukkit.Bukkit
import java.io.File

class FurnacesDB(private val f: File) {
    private val db = HashMap<Int, LavaFurnaceData>()

    fun getLine(coord: Coords) : Pair<Int, LavaFurnaceData> {

        db.forEach { (ind, s) ->
            val (c, _, _, _) = s
            if (c == coord) return Pair(ind, s)
        }

        Bukkit.getLogger().info("CSV getLine: Line not found")
        throw NullPointerException()
    }

    fun setLine(pos: Int, new: LavaFurnaceData?) {
        if (new != null) {
            db[pos] = new
            return
        }

        db.remove(pos)
    }

    fun updateLine(ind: Int, d: LavaFurnaceData) {
        db[ind] = d
    }

    fun addLine(s: LavaFurnaceData) {
        db[db.size+1] = s
    }

    fun getAsList(pos: Int) : ArrayList<String> {
        val l = ArrayList<String>()
        //Bukkit.getLogger().info(db[pos].toString())
        val fuel = db[pos]?.fuel
        val max = db[pos]?.fuelMax
        val speed = db[pos]?.speed

        l.add("Fuel: $fuel cB")
        l.add("FuelMax: $max cB")
        l.add("SpeedLevel: $speed")

        return l
    }

    fun loadValues() {
        f.forEachLine {
            val values = it.split(';')
            val c = Coords(values[0], values[1].toInt(), values[2].toInt(), values[3].toInt())
            val f = values[4].split(" ")[1].toInt()
            val fM = values[5].split(" ")[1].toInt()
            val s = values[6].split(" ")[1].toInt()

            this.addLine(LavaFurnaceData(c,f,fM,s))
        }
    }

    fun saveValues() {
        f.writeText("")
        db.forEach { u ->  f.appendText(u.value.toString() + System.lineSeparator())}
    }
}