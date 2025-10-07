package net.zelythia.tool_trinkets.forge.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Networking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(ResourceLocation.fromNamespaceAndPath("tools_trinkets", "main"))
            .clientAcceptedVersions(PROTOCOL::equals)
            .serverAcceptedVersions(PROTOCOL::equals)
            .networkProtocolVersion(() -> PROTOCOL)
            .simpleChannel();

    private static int id = 0;
    public static int nextId() { return id++; }

    public static void register() {
        CHANNEL.registerMessage(nextId(), CMoveCurioPacket.class,
                CMoveCurioPacket::encode, CMoveCurioPacket::decode, CMoveCurioPacket::handle);
    }

}
