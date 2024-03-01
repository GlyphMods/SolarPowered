package io.github.glyphmods.solarpowered.registry

import com.tterrag.registrate.util.entry.BlockEntry
import io.github.glyphmods.solarpowered.SolarPowered
import io.github.glyphmods.solarpowered.content.TestLaserBlock
import net.minecraft.world.level.block.Blocks

object SolarBlocks {
    private val REGISTRATE = SolarPowered.REGISTRATE

    val TEST_LASER: BlockEntry<TestLaserBlock> = REGISTRATE.block<TestLaserBlock>("test_laser", ::TestLaserBlock)
        .initialProperties { Blocks.STONE }
        .lang("Test Laser")
        .simpleItem()
        .register()

    fun register() {}
}