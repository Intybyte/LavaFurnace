package me.vaan.lavafurnace.dataclasses

import org.bukkit.Location


data class Coords(val world: String, val x: Int, val y: Int, val z: Int) {
    constructor(l: Location) : this(l.world.name, l.x.toInt(), l.y.toInt(), l.z.toInt())

    override fun toString(): String {
        return "$world $x $y $z"
    }
}