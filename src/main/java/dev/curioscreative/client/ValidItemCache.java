package dev.curioscreative.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import dev.curioscreative.CurioChecks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.SlotContext;

/**
 * Lists every registry item that Curios itself considers valid for a slot type.
 * Scanned incrementally within a per-tick time budget so big modpacks don't hitch.
 * Client-thread only. Cleared on logout (tags/registries can change between worlds).
 */
public final class ValidItemCache {
    public record Candidate(ItemStack stack, ResourceLocation id, String searchText) {}

    public static final class Entry {
        private final List<Candidate> items = new ArrayList<>();
        private final List<Item> pending = new ArrayList<>();
        private int cursor;

        Entry() {
            for (Item item : ForgeRegistries.ITEMS) {
                if (item != Items.AIR) pending.add(item);
            }
            pending.sort(Comparator.comparing(i -> String.valueOf(ForgeRegistries.ITEMS.getKey(i))));
        }

        public List<Candidate> items() { return items; }
        public boolean isDone() { return cursor >= pending.size(); }
        public float progress() { return pending.isEmpty() ? 1f : (float) cursor / pending.size(); }

        /** @return true if new candidates were added. */
        public boolean scan(Player player, String slotId, boolean cosmetic, long budgetNanos) {
            boolean changed = false;
            SlotContext ctx = new SlotContext(slotId, player, 0, cosmetic, true);
            long end = System.nanoTime() + budgetNanos;
            while (cursor < pending.size() && System.nanoTime() < end) {
                Item item = pending.get(cursor++);
                try {
                    ItemStack stack = new ItemStack(item);
                    if (!CurioChecks.isValid(ctx, stack, false)) continue;
                    ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
                    if (id == null) continue;
                    String text = (stack.getHoverName().getString() + " " + id).toLowerCase(Locale.ROOT);
                    items.add(new Candidate(stack, id, text));
                    changed = true;
                } catch (Throwable ignored) {
                    // a broken modded item must never break the picker
                }
            }
            return changed;
        }
    }

    private static final Map<String, Entry> CACHE = new HashMap<>();

    private ValidItemCache() {}

    public static Entry get(String slotId, boolean cosmetic) {
        return CACHE.computeIfAbsent(slotId + (cosmetic ? "#cosmetic" : ""), k -> new Entry());
    }

    public static void clear() { CACHE.clear(); }
}
