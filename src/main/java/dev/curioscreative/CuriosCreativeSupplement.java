package dev.curioscreative;

import com.mojang.logging.LogUtils;
import dev.curioscreative.client.ClientInit;
import dev.curioscreative.network.Net;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

@Mod(CuriosCreativeSupplement.MOD_ID)
public class CuriosCreativeSupplement {
    public static final String MOD_ID = "curioscreative";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CuriosCreativeSupplement() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        Net.register();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CuriosCreativeSupplement::onConfigReload);
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientInit::init);
    }

    /** Config file edited by hand (or saved by us): push the new state to every client. */
    private static void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != Config.SPEC) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) server.execute(Net::broadcastSettings);
    }
}
