package dev.curioscreative.client;

import dev.curioscreative.CuriosCreativeSupplement;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;

/** Only ever referenced through DistExecutor, so it is never loaded on a dedicated server. */
public final class ClientInit {
    private static final String COMPAT_CLASS = "dev.curioscreative.compat.controllable.ControllableCompat";

    private ClientInit() {}

    public static void init() {
        // Mods list -> Config button
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new SettingsScreen(parent)));

        // Soft dependency: touched ONLY by reflection and ONLY after the mod-list check,
        // so no Controllable class is ever resolved when it is absent (or when compat wasn't compiled in).
        if (ModList.get().isLoaded("controllable")) {
            try {
                Class.forName(COMPAT_CLASS).getMethod("init").invoke(null);
            } catch (ReflectiveOperationException | LinkageError e) {
                CuriosCreativeSupplement.LOGGER.warn("Controllable is installed but compat could not be loaded: {}", e.toString());
            }
        }
    }
}
