package io.github.glyphmods.solarpowered.registry

import io.github.glyphmods.solarpowered.SolarPowered
import io.github.glyphmods.solarpowered.content.TestLaserBlockEntity

object SolarBlockEntities {
    private val REGISTRATE = SolarPowered.REGISTRATE

    val TEST_LASER = REGISTRATE.blockEntity<TestLaserBlockEntity>("test_laser", ::TestLaserBlockEntity)
        .validBlock { SolarBlocks.TEST_LASER.get() }
        .register()

    fun register() {}
}