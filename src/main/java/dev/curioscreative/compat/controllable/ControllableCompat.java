package dev.curioscreative.compat.controllable;

/**
 * Entry point, loaded reflectively by ClientInit ONLY after ModList.isLoaded("controllable").
 * Client-only. Compiled only when controllable_file_id is set in gradle.properties.
 */
public final class ControllableCompat {
    private ControllableCompat() {}

    public static void init() {
        ControllableHooks.register();
    }
}
