package dev.curioscreative.client;

import dev.curioscreative.Config;
import dev.curioscreative.ModeSetting;
import dev.curioscreative.network.Net;
import dev.curioscreative.network.SettingsUpdatePacket;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * In a world: reads ClientState, sends changes to the server (operators only, server re-checks).
 * From the main menu (Mods -> Config): edits the local config file directly.
 */
public class SettingsScreen extends Screen {
    private static final int W = 220, H = 170;

    @Nullable private final Screen parent;
    private Button toggleButton, modeButton;
    private boolean enabled;
    private ModeSetting mode = ModeSetting.CREATIVE_ONLY;

    public SettingsScreen(@Nullable Screen parent) {
        super(Component.translatable("gui.curioscreative.title"));
        this.parent = parent;
    }

    private boolean inWorld() {
        return minecraft != null && minecraft.level != null && minecraft.player != null;
    }

    private boolean canEdit() {
        return !inWorld() || minecraft.getSingleplayerServer() != null || minecraft.player.hasPermissions(2);
    }

    private void load() {
        if (inWorld()) {
            enabled = ClientState.enabled;
            mode = ClientState.mode;
        } else {
            enabled = Config.ENABLED.get();
            mode = Config.MODE.get();
        }
    }

    @Override
    protected void init() {
        load();
        int cx = width / 2, top = (height - H) / 2;
        toggleButton = addRenderableWidget(Button.builder(Component.empty(), b -> { enabled = !enabled; push(); })
                .bounds(cx - 90, top + 44, 180, 20).build());
        modeButton = addRenderableWidget(Button.builder(Component.empty(), b -> { mode = mode.next(); push(); })
                .bounds(cx - 90, top + 68, 180, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.curioscreative.done"), b -> onClose())
                .bounds(cx - 50, top + H - 28, 100, 20).build());
        updateButtons();
    }

    private void push() {
        if (inWorld()) {
            ClientState.apply(enabled, mode);                               // optimistic; server sync overrides
            Net.CHANNEL.sendToServer(new SettingsUpdatePacket(enabled, mode));
        } else {
            Config.ENABLED.set(enabled);
            Config.MODE.set(mode);
            Config.SPEC.save();
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean edit = canEdit();
        toggleButton.setMessage(Component.translatable("gui.curioscreative.enable").append(": ")
                .append(Component.translatable(enabled ? "gui.curioscreative.on" : "gui.curioscreative.off")));
        modeButton.setMessage(Component.translatable("gui.curioscreative.game_mode").append(": ").append(mode.label()));
        toggleButton.active = edit;
        modeButton.active = edit;
    }

    @Override
    public void tick() {
        load();            // picks up server syncs while the screen is open
        updateButtons();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int left = (width - W) / 2, top = (height - H) / 2;
        g.fill(left - 2, top - 2, left + W + 2, top + H + 2, 0xFF5A5A70);
        g.fill(left, top, left + W, top + H, 0xF0181820);

        g.drawCenteredString(font, title, width / 2, top + 8, 0xFFFFFF);
        g.drawCenteredString(font, Component.translatable("gui.curioscreative.subtitle"), width / 2, top + 22, 0xAAAAAA);

        super.render(g, mouseX, mouseY, partialTick);

        g.drawCenteredString(font, Component.translatable(enabled ? "gui.curioscreative.status.enabled"
                : "gui.curioscreative.status.disabled"), width / 2, top + 96, enabled ? 0x55FF55 : 0xFF5555);
        g.drawCenteredString(font, Component.translatable("gui.curioscreative.note"), width / 2, top + 110, 0x999999);
        if (!canEdit()) {
            g.drawCenteredString(font, Component.translatable("gui.curioscreative.readonly"), width / 2, top + 126, 0xFFAA55);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
