package dev.curioscreative.network;

import dev.curioscreative.Config;
import dev.curioscreative.CuriosCreativeSupplement;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Net {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CuriosCreativeSupplement.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private Net() {}

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, EquipPacket.class, EquipPacket::encode, EquipPacket::decode,
                EquipPacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, SettingsUpdatePacket.class, SettingsUpdatePacket::encode, SettingsUpdatePacket::decode,
                SettingsUpdatePacket::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(id++, SettingsSyncPacket.class, SettingsSyncPacket::encode, SettingsSyncPacket::decode,
                SettingsSyncPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendSettings(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new SettingsSyncPacket(Config.ENABLED.get(), Config.MODE.get()));
    }

    public static void broadcastSettings() {
        CHANNEL.send(PacketDistributor.ALL.noArg(),
                new SettingsSyncPacket(Config.ENABLED.get(), Config.MODE.get()));
    }
}
