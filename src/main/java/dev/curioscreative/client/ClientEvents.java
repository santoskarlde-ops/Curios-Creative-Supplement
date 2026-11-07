package dev.curioscreative.client;

import dev.curioscreative.CuriosCreativeSupplement;
import dev.curioscreative.network.EquipPacket;
import dev.curioscreative.network.Net;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.common.inventory.CosmeticCurioSlot;
import top.theillusivec4.curios.common.inventory.CurioSlot;

/**
 * Adds the settings button and intercepts clicks on Curios slots. No mixins needed:
 * everything goes through Forge's ScreenEvent. The Curios screen is detected by its slots
 * (CurioSlot), not by screen class, so it survives Curios screen rewrites.
 *
 * Interaction (only while the feature is usable for this player, cursor empty, no Shift held):
 *   left-click slot  -> open picker      right-click filled slot -> delete the copy
 *   Shift-click / cursor holding an item -> untouched vanilla Curios behaviour
 */
@Mod.EventBusSubscriber(modid = CuriosCreativeSupplement.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    public static final ResourceLocation SETTINGS_ICON =
            new ResourceLocation(CuriosCreativeSupplement.MOD_ID, "textures/gui/settings_button.png");

    private ClientEvents() {}

    public static boolean isCuriosScreen(Screen screen) {
        if (!(screen instanceof AbstractContainerScreen<?> acs)) return false;
        for (Slot slot : acs.getMenu().slots) {
            if (slot instanceof CurioSlot) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen) || !isCuriosScreen(screen)) return;
        int x = screen.getGuiLeft() + screen.getXSize() - 15;   // top-right corner of the panel; adjust if it overlaps
        int y = screen.getGuiTop() + 4;
        ImageButton button = new ImageButton(x, y, 12, 12, 0, 0, 12, SETTINGS_ICON, 12, 24,
                b -> Minecraft.getInstance().setScreen(new SettingsScreen(screen)),
                Component.translatable("gui.curioscreative.open_settings"));
        button.setTooltip(Tooltip.create(Component.translatable("gui.curioscreative.open_settings")));
        event.addListener(button);
    }

    @SubscribeEvent
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !ClientState.mayUse(player)) return;
        if (!screen.getMenu().getCarried().isEmpty() || Screen.hasShiftDown()) return;

        Slot slot = screen.getSlotUnderMouse();
        if (!(slot instanceof CurioSlot curioSlot)) return;

        String id = curioSlot.getIdentifier();
        int index = slot.getContainerSlot();
        boolean cosmetic = slot instanceof CosmeticCurioSlot;

        if (event.getButton() == 0) {
            Minecraft.getInstance().setScreen(new PickerScreen(screen, id, index, cosmetic));
            event.setCanceled(true);
        } else if (event.getButton() == 1 && slot.hasItem()) {
            Net.CHANNEL.sendToServer(new EquipPacket(id, index, cosmetic, null));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ValidItemCache.clear();
    }
}
