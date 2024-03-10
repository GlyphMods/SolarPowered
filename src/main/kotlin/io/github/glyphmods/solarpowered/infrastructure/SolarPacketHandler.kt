package io.github.glyphmods.solarpowered.infrastructure

import io.github.glyphmods.solarpowered.SolarPowered
import io.github.glyphmods.solarpowered.content.optics.network.OpticalNetworkEdgeChangePacket
import io.github.glyphmods.solarpowered.content.optics.network.OpticalNetworkSyncPacket
import io.github.glyphmods.solarpowered.content.optics.network.OpticalNetworkSyncRequestPacket
import io.github.glyphmods.solarpowered.content.optics.network.OpticalNetworkVertexChangePacket
import io.github.glyphmods.solarpowered.content.optics.network.manager.ClientOpticalNetworkManager
import io.github.glyphmods.solarpowered.content.optics.network.manager.ServerOpticalNetworkManager
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.network.NetworkRegistry

object SolarPacketHandler {
    private var discriminator = 0
    private val PROTOCOL_VERSION = "1"
    val CHANNEL_NAME = ResourceLocation(SolarPowered.ID, "channel")
    val CHANNEL = NetworkRegistry.newSimpleChannel(
        CHANNEL_NAME,
        { PROTOCOL_VERSION },
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals,
    )

    init {
        CHANNEL.messageBuilder(OpticalNetworkSyncPacket::class.java, discriminator++)
            .encoder(OpticalNetworkSyncPacket::serialize)
            .decoder(::OpticalNetworkSyncPacket)
            .consumerMainThread(ClientOpticalNetworkManager::handleNetworkChangePacket)
            .add()
        CHANNEL.messageBuilder(OpticalNetworkEdgeChangePacket::class.java, discriminator++)
            .encoder(OpticalNetworkEdgeChangePacket::serialize)
            .decoder(::OpticalNetworkEdgeChangePacket)
            .consumerMainThread(ClientOpticalNetworkManager::handleNetworkChangePacket)
            .add()
        CHANNEL.messageBuilder(OpticalNetworkVertexChangePacket::class.java, discriminator++)
            .encoder(OpticalNetworkVertexChangePacket::serialize)
            .decoder(::OpticalNetworkVertexChangePacket)
            .consumerMainThread(ClientOpticalNetworkManager::handleNetworkChangePacket)
            .add()
        CHANNEL.messageBuilder(OpticalNetworkSyncRequestPacket::class.java, discriminator++)
            .encoder(OpticalNetworkSyncRequestPacket::serialize)
            .decoder(::OpticalNetworkSyncRequestPacket)
            .consumerMainThread(ServerOpticalNetworkManager::handleSyncRequest)
            .add()
    }

    fun register() {}
}