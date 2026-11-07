package dev.curioscreative.network;

import dev.curioscreative.Config;
import dev.curioscreative.ModeSetting;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** C2S: operator changes the settings. Validated and applied on the server, then broadcast to everyone. */
public record SettingsUpdatePacket(boolean enabled, ModeSetting mode) {

    public static void encode(SettingsUpdatePacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.enabled);
        buf.writeByte(p.mode.ordinal());
    }

    public static SettingsUpdatePacket decode(FriendlyByteBuf buf) {
        return new SettingsUpdatePacket(buf.readBoolean(), ModeSetting.byOrdinal(buf.readByte()));
    }

    public static void handle(SettingsUpdatePacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender == null) return;
            if (!Config.isAdmin(sender)) {
                sender.displayClientMessage(Component.translatable("message.curioscreative.denied.not_admin"), true);
                Net.sendSettings(sender); // revert the sender's optimistic UI
                return;
            }
            Config.ENABLED.set(p.enabled);
            Config.MODE.set(p.mode);
            Config.SPEC.save();
            Net.broadcastSettings();
        });
        ctx.get().setPacketHandled(true);
    }
}
