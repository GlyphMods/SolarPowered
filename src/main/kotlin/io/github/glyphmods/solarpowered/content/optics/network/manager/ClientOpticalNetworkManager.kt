package io.github.glyphmods.solarpowered.content.optics.network.manager

import io.github.glyphmods.solarpowered.content.optics.network.OpticalNetworkSyncPacket
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

data object ClientOpticalNetworkManager : AbstractOpticalNetworkManager<ClientLevel>() {
    private fun getOrCreateNetwork(level: ClientLevel, id: Long) {
        TODO()
    }

    fun handleNetworkSyncPacket(packet: OpticalNetworkSyncPacket, ctx: Supplier<NetworkEvent.Context>) {
        TODO()
    }
}