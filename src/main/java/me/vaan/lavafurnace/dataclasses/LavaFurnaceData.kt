package me.vaan.lavafurnace.dataclasses

data class LavaFurnaceData(val c: Coords, var fuel: Int, var fuelMax: Int, var speed: Int) {
    override fun toString(): String {
        val (world, x, y, z) = c
        return "$world;$x;$y;$z;Fuel: $fuel cB;FuelMax: $fuelMax cB;SpeedLevel: $speed"
    }
}