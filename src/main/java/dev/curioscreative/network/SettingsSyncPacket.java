package dev.curioscreative.network;

import dev.curioscreative.ModeSetting;
import dev.curioscreative.client.ClientState;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/** S2C: authoritative settings state (sent on login and on every change). */
public record SettingsSyncPacket(boolean enabled, ModeSetting mode) {

    public static void encode(SettingsSyncPacket p, FriendlyByteBuf buf) {
        buf.writeBoolean(p.enabled);
        buf.writeByte(p.mode.ordinal());
    }

    public static SettingsSyncPacket decode(FriendlyByteBuf buf) {
        return new SettingsSyncPacket(buf.readBoolean(), ModeSetting.byOrdinal(buf.readByte()));
    }

    public static void handle(SettingsSyncPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientState.apply(p.enabled, p.mode)));
        ctx.get().setPacketHandled(true);
    }
}
