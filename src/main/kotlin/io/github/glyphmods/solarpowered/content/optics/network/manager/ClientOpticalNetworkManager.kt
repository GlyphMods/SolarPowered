package io.github.glyphmods.solarpowered.content.optics.network.manager

import io.github.glyphmods.solarpowered.content.optics.network.ClientOpticalNetwork
import io.github.glyphmods.solarpowered.content.optics.network.OpticalNetworkChangePacket
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

data object ClientOpticalNetworkManager : AbstractOpticalNetworkManager<ClientLevel>() {
    private fun getOrCreateNetwork(level: ClientLevel, id: Long) =
        networks.getOrPut(level) { hashMapOf() }
            .getOrPut(id) { ClientOpticalNetwork(id, level) } as ClientOpticalNetwork

    fun handleNetworkChangePacket(packet: OpticalNetworkChangePacket, ctx: Supplier<NetworkEvent.Context>) {
        val level = Minecraft.getInstance().level!!
        assert(level.dimension() == packet.dimension) {
            "Received a network sync packet for network ${packet.id} in ${packet.dimension.location()}, but we're in ${
                level.dimension().location()
            }!"
        }
        val network = getOrCreateNetwork(level, packet.id)
        network.handleChangePacket(packet)
        ctx.get().packetHandled = true
    }
}