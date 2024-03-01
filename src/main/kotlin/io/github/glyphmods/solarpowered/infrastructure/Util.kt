package io.github.glyphmods.solarpowered.infrastructure

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3

fun Vec3.addDirectionVector(vec: Vec3i, distance: Double): Vec3 {
    return Vec3(
        x + distance * vec.x,
        y + distance * vec.y,
        z + distance * vec.z,
    )
}