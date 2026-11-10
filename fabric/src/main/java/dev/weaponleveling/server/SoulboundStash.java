package dev.weaponleveling.server;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Soulbound items waiting for their owner to respawn, saved with the world. */
public final class SoulboundStash extends SavedData {
    private static final String DATA_ID = "weaponleveling_soulbound";

    private final Map<UUID, List<Entry>> stash = new HashMap<>();

    /** @param slot inventory slot the item was taken from */
    public record Entry(int slot, ItemStack stack) {
    }

    private SoulboundStash() {
    }

    public static SoulboundStash get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(SoulboundStash::new, SoulboundStash::load, null), DATA_ID);
    }

    public void add(UUID playerId, List<Entry> entries) {
        stash.computeIfAbsent(playerId, id -> new ArrayList<>()).addAll(entries);
        setDirty();
    }

    public List<Entry> take(UUID playerId) {
        List<Entry> entries = stash.remove(playerId);
        if (entries == null) {
            return List.of();
        }
        setDirty();
        return entries;
    }

    private static SoulboundStash load(CompoundTag tag, HolderLookup.Provider registries) {
        SoulboundStash data = new SoulboundStash();
        ListTag players = tag.getList("players", Tag.TAG_COMPOUND);
        for (int index = 0; index < players.size(); index++) {
            CompoundTag player = players.getCompound(index);
            if (!player.hasUUID("id")) {
                continue;
            }
            List<Entry> entries = new ArrayList<>();
            ListTag items = player.getList("items", Tag.TAG_COMPOUND);
            for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
                CompoundTag item = items.getCompound(itemIndex);
                ItemStack.parse(registries, item.getCompound("stack"))
                        .ifPresent(stack -> entries.add(new Entry(item.getInt("slot"), stack)));
            }
            if (!entries.isEmpty()) {
                data.stash.put(player.getUUID("id"), entries);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag players = new ListTag();
        stash.forEach((playerId, entries) -> {
            CompoundTag player = new CompoundTag();
            player.putUUID("id", playerId);
            ListTag items = new ListTag();
            for (Entry entry : entries) {
                if (entry.stack().isEmpty()) {
                    continue;
                }
                CompoundTag item = new CompoundTag();
                item.putInt("slot", entry.slot());
                item.put("stack", entry.stack().save(registries));
                items.add(item);
            }
            player.put("items", items);
            players.add(player);
        });
        tag.put("players", players);
        return tag;
    }
}
