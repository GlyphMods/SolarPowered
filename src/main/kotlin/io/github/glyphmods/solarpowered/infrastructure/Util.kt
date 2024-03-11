package io.github.glyphmods.solarpowered.infrastructure

import com.simibubi.create.foundation.utility.Color
import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import kotlin.math.absoluteValue

fun Vec3.addDirectionVector(vec: Vec3i, distance: Double): Vec3 {
    return Vec3(
        x + distance * vec.x,
        y + distance * vec.y,
        z + distance * vec.z,
    )
}

enum class NetworkColors(colorCode: Int) {
    YELLOW(0xEBC255),
    GREEN(0x51C054),
    BLUE(0x5391E1),
    ORANGE(0xE36E36),
    LAVENDER(0xCB92BA),
    RED(0xA43538),
    CYAN(0x6EDAD9),
    BROWN(0xA17C58),
    WHITE(0xE5E1DC);

    val color = Color(colorCode)

    companion object {
        fun colorForId(id: Long) = entries[(id % entries.size).toInt().absoluteValue]
    }
}