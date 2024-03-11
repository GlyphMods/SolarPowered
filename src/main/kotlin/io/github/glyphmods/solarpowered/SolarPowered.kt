package io.github.glyphmods.solarpowered

import io.github.glyphmods.solarpowered.content.optics.OpticalDebugger
import io.github.glyphmods.solarpowered.content.optics.network.manager.ServerOpticalNetworkManager
import io.github.glyphmods.solarpowered.infrastructure.SolarPacketHandler
import io.github.glyphmods.solarpowered.infrastructure.SolarRegistrate
import io.github.glyphmods.solarpowered.registry.SolarBlockEntities
import io.github.glyphmods.solarpowered.registry.SolarBlocks
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.event.TickEvent.ClientTickEvent
import net.minecraftforge.event.VanillaGameEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.fml.common.Mod
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.callWhenOn

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
        SolarPacketHandler.register()

        FORGE_BUS.addListener(::onLevelLoaded)
        FORGE_BUS.addListener(::onLevelUnloaded)
        FORGE_BUS.addListener(::onVanillaGameEvent)
        FORGE_BUS.addListener(::onPlayerJoined)
        callWhenOn(Dist.CLIENT) {
            FORGE_BUS.addListener(::onClientTick)
        }
    }

    private fun onLevelLoaded(event: LevelEvent.Load) {
        if (!event.level.isClientSide) {
            ServerOpticalNetworkManager.onLevelLoaded(event.level)
        }
    }

    private fun onLevelUnloaded(event: LevelEvent.Unload) {
        if (!event.level.isClientSide) {
            ServerOpticalNetworkManager.onLevelUnloaded(event.level)
        }
    }

    private fun onVanillaGameEvent(event: VanillaGameEvent) {
        if (!event.level.isClientSide && (event.vanillaEvent == GameEvent.BLOCK_PLACE || event.vanillaEvent == GameEvent.BLOCK_DESTROY)) {
            ServerOpticalNetworkManager.onBlockModified(event.level, BlockPos.containing(event.eventPosition))
        }
    }

    private fun onPlayerJoined(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity
        if (player is ServerPlayer) {
            ServerOpticalNetworkManager.syncAllNetworksTo(player)
        }
    }

    private fun onClientTick(event: ClientTickEvent) {
        OpticalDebugger.tick()
    }
}