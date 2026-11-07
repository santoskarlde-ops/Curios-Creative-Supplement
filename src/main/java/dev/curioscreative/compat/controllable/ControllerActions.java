package dev.curioscreative.compat.controllable;

import dev.curioscreative.client.ClientEvents;
import dev.curioscreative.client.PickerScreen;
import dev.curioscreative.client.SettingsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Controllable-independent behaviour (no Controllable imports here), so the only code that can
 * break on an API mismatch is ControllableHooks.
 *
 * D-pad / stick navigation and A (select) are NOT handled here on purpose: the picker/settings
 * screens use real vanilla widgets, which Controllable navigates and clicks natively.
 * Trigger and aim inputs are never read.
 */
public final class ControllerActions {
    public enum Action { PAGE_PREV, PAGE_NEXT, CATEGORY_NEXT, CLOSE, OPEN_SETTINGS }

    private ControllerActions() {}

    /** @return true if the input was consumed. */
    public static boolean dispatch(Action action) {
        Minecraft mc = Minecraft.getInstance();
        Screen screen = mc.screen;

        if (screen instanceof PickerScreen picker) {
            switch (action) {
                case PAGE_PREV -> picker.changePage(-1);
                case PAGE_NEXT -> picker.changePage(1);
                case CATEGORY_NEXT -> picker.cycleCategory(1);
                case CLOSE -> picker.onClose();
                default -> { return false; }
            }
            return true;
        }
        if (screen instanceof SettingsScreen settings && action == Action.CLOSE) {
            settings.onClose();
            return true;
        }
        if (action == Action.OPEN_SETTINGS && mc.player != null && (screen == null || ClientEvents.isCuriosScreen(screen))) {
            mc.setScreen(new SettingsScreen(screen));
            return true;
        }
        return false;
    }
}
