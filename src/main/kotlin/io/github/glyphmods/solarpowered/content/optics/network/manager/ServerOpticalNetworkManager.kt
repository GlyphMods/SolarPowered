package io.github.glyphmods.solarpowered.content.optics.network.manager

import com.simibubi.create.foundation.utility.WorldHelper
import io.github.glyphmods.solarpowered.SolarPowered
import io.github.glyphmods.solarpowered.content.optics.network.OpticalNetworkSyncRequestPacket
import io.github.glyphmods.solarpowered.content.optics.network.ServerOpticalNetwork
import io.github.glyphmods.solarpowered.content.optics.network.graph.SyncedNetworkGraph
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.server.ServerLifecycleHooks
import java.util.function.Supplier

data object ServerOpticalNetworkManager : AbstractOpticalNetworkManager<ServerLevel>() {
    fun onLevelLoaded(level: LevelAccessor) {
        networks[level] = hashMapOf()
        SolarPowered.LOGGER.debug("Optical network space prepared for level {}", WorldHelper.getDimensionID(level))
    }

    fun onLevelUnloaded(level: LevelAccessor) {
        networks.remove(level)
        SolarPowered.LOGGER.debug("Optical network space cleared for level {}", WorldHelper.getDimensionID(level))
    }

    fun onBlockModified(level: Level, pos: BlockPos) {
        networks[level]?.values?.forEach { (it as ServerOpticalNetwork).notifyBlockModified(pos) }
    }

    fun syncAllNetworksTo(player: ServerPlayer) {
        networks[player.level()]!!.values.forEach { (it.getGraph() as SyncedNetworkGraph).fullSync() }
    }

    fun getOrCreateNetwork(level: ServerLevel, id: Long) =
        networks.getOrPut(level) { hashMapOf() }.getOrPut(id) {
            ServerOpticalNetwork(
                id,
                level
            )
        } as ServerOpticalNetwork

    fun handleSyncRequest(packet: OpticalNetworkSyncRequestPacket, ctx: Supplier<NetworkEvent.Context>) {
        val level = ServerLifecycleHooks.getCurrentServer()!!
            .getLevel(ResourceKey.create(Registries.DIMENSION, packet.dimension))
        if (level == null) {
            SolarPowered.LOGGER.warn("Sync requested for invalid level ${packet.dimension} by ${ctx.get().sender?.name}!")
            return
        }
        (getNetwork(level, packet.id)?.getGraph() as? SyncedNetworkGraph)?.fullSync()
            ?: SolarPowered.LOGGER.warn("Sync requested for invalid network ${packet.id} in ${packet.dimension} by ${ctx.get().sender?.name}!")
    }
}