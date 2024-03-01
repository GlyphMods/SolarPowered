package io.github.glyphmods.solarpowered.content.optics

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity
import io.github.glyphmods.solarpowered.content.optics.network.AbstractOpticalNetwork
import io.github.glyphmods.solarpowered.content.optics.network.ServerOpticalNetwork
import io.github.glyphmods.solarpowered.content.optics.network.manager.AbstractOpticalNetworkManager
import io.github.glyphmods.solarpowered.content.optics.network.manager.ClientOpticalNetworkManager
import io.github.glyphmods.solarpowered.content.optics.network.manager.ServerOpticalNetworkManager
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState


abstract class OpticalBlockEntity(
    type: BlockEntityType<*>?, pos: BlockPos, state: BlockState
) : SmartBlockEntity(type, pos, state) {
    var opticalNetworkId: Long? = null

    override fun remove() {
        if (!level!!.isClientSide) {
            getOrCreateNetwork().remove(this)
        }
        super.remove()
    }

//    override fun read(tag: CompoundTag, clientPacket: Boolean) {
//        if (tag.contains("Network")) {
//            opticalNetworkId = tag.getLong("Network")
//        }
//        super.read(tag, clientPacket)
//    }
//
//    override fun write(tag: CompoundTag, clientPacket: Boolean) {
//        if (opticalNetworkId != null) tag.putLong("Network", opticalNetworkId!!)
//        super.write(tag, clientPacket)
//    }

    private fun getManager() =
        AbstractOpticalNetworkManager.getManagerForLevel(level!!)

    fun getNetwork() =
        getManager().getNetwork(level!!, opticalNetworkId!!)

    fun getOrCreateNetwork(): ServerOpticalNetwork {
        return when (val manager = getManager() as AbstractOpticalNetworkManager<*>) {
            is ClientOpticalNetworkManager -> error("Cannot call getOrCreateNetwork() on the client!")
            is ServerOpticalNetworkManager -> manager.getOrCreateNetwork(
                level as ServerLevel,
                opticalNetworkId ?: manager.randomSource.nextLong()
            )
        }
    }

    abstract fun getEmittedRays(receivedRays: Set<SunlightBeam>): Set<SunlightBeam>

    open fun onAddedToNetwork(network: AbstractOpticalNetwork<*>) {}
    open fun onRemovedFromNetwork(network: AbstractOpticalNetwork<*>) {}
}

abstract class OpticalSourceBlockEntity(
    type: BlockEntityType<*>?, pos: BlockPos, state: BlockState
) : OpticalBlockEntity(type, pos, state) {
    override fun tick() {
        super.tick()
        if (opticalNetworkId == null && !level!!.isClientSide) {
            getOrCreateNetwork().add(this)
//            sendData()
        }
    }
}