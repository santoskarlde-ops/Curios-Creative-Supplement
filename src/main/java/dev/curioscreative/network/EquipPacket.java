package dev.curioscreative.network;

import dev.curioscreative.server.CurioEquipService;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** C2S: put registry item {@code item} into a Curios slot, or clear it when {@code item} is null. Only ids are sent, never stacks. */
public record EquipPacket(String slotId, int index, boolean cosmetic, @Nullable ResourceLocation item) {

    public static void encode(EquipPacket p, FriendlyByteBuf buf) {
        buf.writeUtf(p.slotId, 64);
        buf.writeVarInt(p.index);
        buf.writeBoolean(p.cosmetic);
        buf.writeBoolean(p.item != null);
        if (p.item != null) buf.writeResourceLocation(p.item);
    }

    public static EquipPacket decode(FriendlyByteBuf buf) {
        String slot = buf.readUtf(64);
        int index = buf.readVarInt();
        boolean cosmetic = buf.readBoolean();
        ResourceLocation item = buf.readBoolean() ? buf.readResourceLocation() : null;
        return new EquipPacket(slot, index, cosmetic, item);
    }

    public static void handle(EquipPacket p, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (sender != null) CurioEquipService.apply(sender, p.slotId, p.index, p.cosmetic, p.item);
        });
        ctx.get().setPacketHandled(true);
    }
}
