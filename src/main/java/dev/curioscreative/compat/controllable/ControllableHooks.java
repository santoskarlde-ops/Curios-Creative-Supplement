package dev.curioscreative.compat.controllable;

import com.mrcrayfish.controllable.client.Buttons;                    // UNVERIFIED package
import com.mrcrayfish.controllable.client.binding.BindingContext;      // UNVERIFIED package
import com.mrcrayfish.controllable.client.binding.BindingRegistry;     // UNVERIFIED package
import com.mrcrayfish.controllable.client.binding.ButtonBinding;       // UNVERIFIED package
import com.mrcrayfish.controllable.event.ControllerEvent;              // UNVERIFIED package / class
import net.minecraftforge.common.MinecraftForge;

/**
 * THE ONLY FILE THAT TOUCHES THE CONTROLLABLE API.
 *
 * I could not verify these signatures offline for Controllable on 1.20.1. Check against the jar:
 *   1. packages of Buttons / ButtonBinding / BindingRegistry / BindingContext
 *   2. ButtonBinding constructor (button, descriptionKey, categoryKey, context) and getButton()
 *   3. the button-input event: class name, and getters for button + pressed state
 * If 3 is not a Forge-bus event in your version, change register() to Controllable's own event hook.
 * Nothing else in the mod depends on this file.
 */
final class ControllableHooks {
    private static ButtonBinding openSettings;

    private ControllableHooks() {}

    static void register() {
        openSettings = new ButtonBinding(Buttons.SELECT, "key.curioscreative.open_settings",
                "key.categories.curioscreative", BindingContext.IN_GAME);                      // UNVERIFIED ctor
        BindingRegistry.getInstance().register(openSettings);                                  // UNVERIFIED
        MinecraftForge.EVENT_BUS.addListener(ControllableHooks::onButtonInput);
    }

    private static void onButtonInput(ControllerEvent.ButtonInput event) {                      // UNVERIFIED type
        if (!event.getState()) return;                                                          // UNVERIFIED getter (pressed)
        int button = event.getButton();                                                         // UNVERIFIED getter

        ControllerActions.Action action = null;
        if (openSettings != null && button == openSettings.getButton()) action = ControllerActions.Action.OPEN_SETTINGS;
        else if (button == Buttons.LEFT_BUMPER) action = ControllerActions.Action.PAGE_PREV;
        else if (button == Buttons.RIGHT_BUMPER) action = ControllerActions.Action.PAGE_NEXT;
        else if (button == Buttons.Y) action = ControllerActions.Action.CATEGORY_NEXT;
        else if (button == Buttons.B) action = ControllerActions.Action.CLOSE;
        // Triggers/aim are deliberately never inspected.

        if (action != null && ControllerActions.dispatch(action)) event.setCanceled(true);
    }
}
