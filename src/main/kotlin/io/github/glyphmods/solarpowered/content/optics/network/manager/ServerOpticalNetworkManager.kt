package io.github.glyphmods.solarpowered.content.optics.network.manager

import com.simibubi.create.foundation.utility.WorldHelper
import io.github.glyphmods.solarpowered.SolarPowered
import io.github.glyphmods.solarpowered.content.optics.network.ServerOpticalNetwork
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor

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

//    private fun fullNetworkSync(network: ServerOpticalNetwork) {
//        SolarPacketHandler.CHANNEL.send(
//            PacketDistributor.DIMENSION.with { network.level.dimension() },
//            OpticalNetworkSyncPacket(
//                OpticalNetworkSyncPacket.Type.ADD,
//                network.id,
//                network.level.dimension().location(),
//                network.links.mapValues { entry ->
//                    val be = entry.value
//                    BlockEntityDescriptor(
//                        ForgeRegistries.BLOCK_ENTITY_TYPES.getResourceKey(be.type).get(),
//                        be.blockPos
//                    )
//                },
//                network.connectionPositions
//            )
//        )
//    }
//
//    fun syncChange(network: ServerOpticalNetwork, type: OpticalNetworkSyncPacket.Type, links: Collection<Link>, connectionPositions: Collection<BlockPos>) {
//        SolarPacketHandler.CHANNEL.send(
//            PacketDistributor.DIMENSION.with { network.level.dimension() },
//            OpticalNetworkSyncPacket(
//                type,
//                network.id,
//                network.level.dimension().location(),
//                links.associateWith { network.links[it]!!.toDescriptor() },
//                connectionPositions.associateWith { network.connectionPositions[it]!! }
//            )
//        )
//    }

    fun getOrCreateNetwork(level: ServerLevel, id: Long) =
        networks.getOrPut(level) { hashMapOf() }.getOrPut(id) {
            ServerOpticalNetwork(
                id,
                level
            )
        } as ServerOpticalNetwork

}