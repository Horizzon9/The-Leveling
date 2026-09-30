package dev.weaponleveling.server;

import dev.weaponleveling.core.chronicle.WeaponChronicle;
import dev.weaponleveling.core.model.WeaponProgress;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * World-wide record of the most progressed weapons. Items live in inventories and chests the
 * server cannot enumerate, so each weapon reports itself whenever it earns XP. Only the strongest
 * {@link #CAPACITY} weapons are kept, which bounds the save file on busy servers.
 */
public final class WeaponLeaderboard extends SavedData {
    private static final String DATA_ID = "weaponleveling_leaderboard";
    private static final int CAPACITY = 100;
    private static final Comparator<Entry> STRONGEST_FIRST = Comparator.comparingLong(Entry::totalXp).reversed()
            .thenComparing(Comparator.comparingLong(Entry::kills).reversed());

    private final Map<UUID, Entry> entries = new HashMap<>();

    /**
     * @param weaponName title if the weapon has one, otherwise its item name at the time of the record
     */
    public record Entry(UUID weaponId, String weaponName, String owner, int level, long totalXp, long kills) {
    }

    private WeaponLeaderboard() {
    }

    public static WeaponLeaderboard get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(WeaponLeaderboard::new, WeaponLeaderboard::load, null), DATA_ID);
    }

    public static void record(ServerPlayer owner, ItemStack weapon, WeaponProgress progress) {
        WeaponChronicle chronicle = ChronicleService.read(weapon);
        String name = chronicle.hasTitle() ? chronicle.title() : weapon.getHoverName().getString();
        get(owner.server).put(new Entry(progress.instanceId(), name, owner.getGameProfile().getName(),
                progress.level(), progress.totalXp(), progress.totalKills()));
    }

    /** Drops a weapon whose history was reset, so its old standing is not shown under a new life. */
    public static void forget(MinecraftServer server, UUID weaponId) {
        WeaponLeaderboard leaderboard = get(server);
        if (leaderboard.entries.remove(weaponId) != null) {
            leaderboard.setDirty();
        }
    }

    public List<Entry> top(int limit) {
        return entries.values().stream().sorted(STRONGEST_FIRST).limit(Math.max(0, limit)).toList();
    }

    private void put(Entry entry) {
        entries.put(entry.weaponId(), entry);
        if (entries.size() > CAPACITY) {
            entries.values().stream().max(STRONGEST_FIRST)
                    .ifPresent(weakest -> entries.remove(weakest.weaponId()));
        }
        setDirty();
    }

    private static WeaponLeaderboard load(CompoundTag tag, HolderLookup.Provider registries) {
        WeaponLeaderboard leaderboard = new WeaponLeaderboard();
        ListTag list = tag.getList("entries", Tag.TAG_COMPOUND);
        for (int index = 0; index < list.size(); index++) {
            CompoundTag entry = list.getCompound(index);
            if (!entry.hasUUID("weapon")) {
                continue;
            }
            Entry parsed = new Entry(entry.getUUID("weapon"), entry.getString("name"), entry.getString("owner"),
                    Math.max(0, entry.getInt("level")), Math.max(0, entry.getLong("total_xp")),
                    Math.max(0, entry.getLong("kills")));
            leaderboard.entries.put(parsed.weaponId(), parsed);
        }
        return leaderboard;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Entry entry : entries.values()) {
            CompoundTag saved = new CompoundTag();
            saved.putUUID("weapon", entry.weaponId());
            saved.putString("name", entry.weaponName());
            saved.putString("owner", entry.owner());
            saved.putInt("level", entry.level());
            saved.putLong("total_xp", entry.totalXp());
            saved.putLong("kills", entry.kills());
            list.add(saved);
        }
        tag.put("entries", list);
        return tag;
    }
}
