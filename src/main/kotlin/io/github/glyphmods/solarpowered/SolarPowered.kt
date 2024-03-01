package io.github.glyphmods.solarpowered

import io.github.glyphmods.solarpowered.content.optics.network.manager.ServerOpticalNetworkManager
import io.github.glyphmods.solarpowered.infrastructure.SolarRegistrate
import io.github.glyphmods.solarpowered.registry.SolarBlockEntities
import io.github.glyphmods.solarpowered.registry.SolarBlocks
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS

/**
 * Main mod class. Should be an `object` declaration annotated with `@Mod`.
 * The modid should be declared in this object and should match the modId entry
 * in mods.toml.
 *
 * An example for blocks is in the `blocks` package of this mod.
 */
@Mod(SolarPowered.ID)
object SolarPowered {
    const val ID = "solarpowered"

    val REGISTRATE = SolarRegistrate(ID)
    val LOGGER: Logger = LogManager.getLogger(ID)

    init {
        LOGGER.log(Level.INFO, "Hello from Solar Powered!")
        REGISTRATE.registerEventListeners(MOD_BUS)

        SolarBlocks.register()
        SolarBlockEntities.register()

        FORGE_BUS.addListener(::onLevelLoaded)
        FORGE_BUS.addListener(::onLevelUnloaded)

    }

    private fun onLevelLoaded(event: LevelEvent.Load) {
        ServerOpticalNetworkManager.onLevelLoaded(event.level)
    }

    private fun onLevelUnloaded(event: LevelEvent.Unload) {
        ServerOpticalNetworkManager.onLevelUnloaded(event.level)
    }
}