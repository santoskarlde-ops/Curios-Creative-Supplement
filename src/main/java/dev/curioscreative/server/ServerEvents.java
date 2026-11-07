package dev.curioscreative.server;

import dev.curioscreative.CuriosCreativeSupplement;
import dev.curioscreative.network.Net;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CuriosCreativeSupplement.MOD_ID)
public final class ServerEvents {
    private ServerEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) Net.sendSettings(sp);
    }
}
