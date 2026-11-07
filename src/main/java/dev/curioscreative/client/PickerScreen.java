package dev.curioscreative.client;

import dev.curioscreative.CuriosCreativeSupplement;
import dev.curioscreative.network.EquipPacket;
import dev.curioscreative.network.Net;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * Searchable item picker for one Curios slot. The item grid is made of real widgets (AbstractButton)
 * so vanilla Tab/arrow/Enter navigation AND Controllable's automatic GUI navigation work with zero extra code.
 * Public changePage/cycleCategory are the hooks used by the optional Controllable compat.
 */
public class PickerScreen extends Screen {
    private static final ResourceLocation SLOT_TEX =
            new ResourceLocation(CuriosCreativeSupplement.MOD_ID, "textures/gui/slot.png");
    private static final int COLS = 9, ROWS = 5, CELL = 18, W = 176, H = 172;
    private static final int PER_PAGE = COLS * ROWS;

    private final Screen parent;
    private final String slotId;
    private final int slotIndex;
    private final boolean cosmetic;
    private final ValidItemCache.Entry entry;
    private final List<CellButton> cells = new ArrayList<>();

    private List<ValidItemCache.Candidate> filtered = List.of();
    private String query = "";
    @Nullable private String category;       // null = all mods
    private int page;
    private int left, top;
    private EditBox search;
    private Button categoryButton, prevButton, nextButton;

    public PickerScreen(Screen parent, String slotId, int slotIndex, boolean cosmetic) {
        super(Component.translatable("gui.curioscreative.picker.title", Component.translatable("curios.identifier." + slotId)));
        this.parent = parent;
        this.slotId = slotId;
        this.slotIndex = slotIndex;
        this.cosmetic = cosmetic;
        this.entry = ValidItemCache.get(slotId, cosmetic);
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;

        search = new EditBox(font, left + 7, top + 18, 100, 14, Component.translatable("gui.curioscreative.picker.search"));
        search.setMaxLength(48);
        search.setHint(Component.translatable("gui.curioscreative.picker.search"));
        search.setValue(query);
        search.setResponder(s -> { query = s; page = 0; refilter(); });
        addRenderableWidget(search);

        categoryButton = addRenderableWidget(Button.builder(Component.empty(), b -> cycleCategory(hasShiftDown() ? -1 : 1))
                .bounds(left + 110, top + 17, 59, 16).build());
        prevButton = addRenderableWidget(Button.builder(Component.literal("<"), b -> changePage(-1))
                .bounds(left + 7, top + 132, 20, 16).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), b -> changePage(1))
                .bounds(left + 149, top + 132, 20, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.curioscreative.picker.clear"), b -> clearSlot())
                .bounds(left + 7, top + 150, 80, 16).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.curioscreative.picker.close"), b -> onClose())
                .bounds(left + 89, top + 150, 80, 16).build());

        setInitialFocus(search);
        cells.clear();
        refilter();
    }

    // ---- data ----

    private void refilter() {
        String q = query.toLowerCase(Locale.ROOT).trim();
        List<ValidItemCache.Candidate> out = new ArrayList<>();
        for (ValidItemCache.Candidate c : entry.items()) {
            if (category != null && !c.id().getNamespace().equals(category)) continue;
            if (!q.isEmpty() && !c.searchText().contains(q)) continue;
            out.add(c);
        }
        filtered = out;
        page = Mth.clamp(page, 0, maxPage());
        refreshCells();
        updateWidgets();
    }

    private int maxPage() { return Math.max(0, (filtered.size() - 1) / PER_PAGE); }

    private void refreshCells() {
        for (CellButton c : cells) removeWidget(c);
        cells.clear();
        int start = page * PER_PAGE;
        for (int i = 0; i < PER_PAGE && start + i < filtered.size(); i++) {
            ValidItemCache.Candidate cand = filtered.get(start + i);
            CellButton b = new CellButton(left + 7 + (i % COLS) * CELL, top + 38 + (i / COLS) * CELL, cand);
            cells.add(b);
            addRenderableWidget(b);
        }
    }

    private void updateWidgets() {
        categoryButton.setMessage(category == null
                ? Component.translatable("gui.curioscreative.picker.all_mods")
                : Component.literal(font.plainSubstrByWidth(category, 52)));
        prevButton.active = page > 0;
        nextButton.active = page < maxPage();
    }

    // ---- actions (also used by controller compat) ----

    public void changePage(int delta) {
        int np = Mth.clamp(page + delta, 0, maxPage());
        if (np == page) return;
        page = np;
        refreshCells();
        updateWidgets();
    }

    public void cycleCategory(int dir) {
        List<String> names = new ArrayList<>();
        names.add(null);
        TreeSet<String> ns = new TreeSet<>();
        for (ValidItemCache.Candidate c : entry.items()) ns.add(c.id().getNamespace());
        names.addAll(ns);
        int idx = Math.max(0, names.indexOf(category));
        category = names.get(Math.floorMod(idx + dir, names.size()));
        page = 0;
        refilter();
    }

    private void choose(ValidItemCache.Candidate c) {
        Net.CHANNEL.sendToServer(new EquipPacket(slotId, slotIndex, cosmetic, c.id()));
        onClose();
    }

    private void clearSlot() {
        Net.CHANNEL.sendToServer(new EquipPacket(slotId, slotIndex, cosmetic, null));
        onClose();
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    // ---- input ----

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_PAGE_UP || key == GLFW.GLFW_KEY_PAGE_DOWN) {
            int dir = key == GLFW.GLFW_KEY_PAGE_UP ? -1 : 1;
            if (hasControlDown()) cycleCategory(dir); else changePage(dir);
            return true;
        }
        if ((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && search.isFocused() && !filtered.isEmpty()) {
            choose(filtered.get(0));
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double delta) {
        changePage(delta > 0 ? -1 : 1);
        return true;
    }

    // ---- rendering ----

    @Override
    public void tick() {
        if (!entry.isDone() && minecraft.player != null) {
            if (entry.scan(minecraft.player, slotId, cosmetic, 6_000_000L)) refilter();
        }
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        g.fill(left - 2, top - 2, left + W + 2, top + H + 2, 0xFF000000);
        g.fill(left - 1, top - 1, left + W + 1, top + H + 1, 0xFFFFFFFF);
        g.fill(left, top, left + W, top + H, 0xFFC6C6C6);
        g.drawString(font, title, left + 7, top + 6, 0x404040, false);

        if (!entry.isDone()) {
            Component scanning = Component.translatable("gui.curioscreative.picker.scanning", (int) (entry.progress() * 100));
            g.drawString(font, scanning, left + W - 7 - font.width(scanning), top + 6, 0x808080, false);
        }

        super.render(g, mouseX, mouseY, partialTick);

        g.drawCenteredString(font, Component.translatable("gui.curioscreative.picker.page", page + 1, maxPage() + 1),
                left + W / 2, top + 136, 0x404040);
        if (entry.isDone() && filtered.isEmpty()) {
            g.drawCenteredString(font, Component.translatable("gui.curioscreative.picker.empty"), width / 2, top + 80, 0x555555);
        }
        for (CellButton c : cells) {
            if (c.isHoveredOrFocused()) {
                g.renderTooltip(font, c.candidate.stack(), mouseX, mouseY);
                break;
            }
        }
    }

    private final class CellButton extends AbstractButton {
        private final ValidItemCache.Candidate candidate;

        CellButton(int x, int y, ValidItemCache.Candidate candidate) {
            super(x, y, CELL, CELL, candidate.stack().getHoverName());
            this.candidate = candidate;
        }

        @Override
        public void onPress() { choose(candidate); }

        @Override
        protected void renderWidget(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
            g.blit(SLOT_TEX, getX(), getY(), 0, 0, CELL, CELL, CELL, CELL);
            g.renderItem(candidate.stack(), getX() + 1, getY() + 1);
            if (isHoveredOrFocused()) {
                g.fillGradient(RenderType.guiOverlay(), getX() + 1, getY() + 1, getX() + 17, getY() + 17,
                        0x80FFFFFF, 0x80FFFFFF, 0);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput out) {
            defaultButtonNarrationText(out);
        }
    }
}
