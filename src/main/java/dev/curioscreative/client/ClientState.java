package dev.curioscreative.client;

import dev.curioscreative.ModeSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Client mirror of the server's settings (display + UI gating only; the server re-checks everything). */
public final class ClientState {
    public static volatile boolean enabled = true;
    public static volatile ModeSetting mode = ModeSetting.CREATIVE_ONLY;

    private ClientState() {}

    public static void apply(boolean enabled, ModeSetting mode) {
        ClientState.enabled = enabled;
        ClientState.mode = mode;
    }

    public static boolean mayUse(LocalPlayer p) {
        if (!enabled || p.isSpectator()) return false;
        boolean admin = p.hasPermissions(2) || Minecraft.getInstance().getSingleplayerServer() != null;
        return switch (mode) {
            case CREATIVE_ONLY -> p.isCreative();
            case CREATIVE_AND_OP -> p.isCreative() || admin;
            case ALL -> true;
        };
    }
}
