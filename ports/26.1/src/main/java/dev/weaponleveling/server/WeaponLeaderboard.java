package dev.weaponleveling.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.weaponleveling.core.chronicle.WeaponChronicle;
import dev.weaponleveling.core.model.WeaponProgress;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;

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
    private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath("weaponleveling", "leaderboard");
    private static final int CAPACITY = 100;
    private static final Comparator<Entry> STRONGEST_FIRST = Comparator.comparingLong(Entry::totalXp).reversed()
            .thenComparing(Comparator.comparingLong(Entry::kills).reversed());
    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("weapon").forGetter(Entry::weaponId),
            Codec.STRING.fieldOf("name").orElse("").forGetter(Entry::weaponName),
            Codec.STRING.fieldOf("owner").orElse("").forGetter(Entry::owner),
            Codec.INT.fieldOf("level").orElse(0).forGetter(Entry::level),
            Codec.LONG.fieldOf("total_xp").orElse(0L).forGetter(Entry::totalXp),
            Codec.LONG.fieldOf("kills").orElse(0L).forGetter(Entry::kills)
    ).apply(instance, (weaponId, weaponName, owner, level, totalXp, kills) -> new Entry(weaponId, weaponName,
            owner, Math.max(0, level), Math.max(0L, totalXp), Math.max(0L, kills))));
    private static final Codec<WeaponLeaderboard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LenientCodecs.listOf(ENTRY_CODEC, "leaderboard entry").fieldOf("entries").orElse(List.of())
                    .forGetter(leaderboard -> List.copyOf(leaderboard.entries.values()))
    ).apply(instance, WeaponLeaderboard::of));
    /** No data fixer touches this fix type, so the saved data is read exactly as it was written. */
    private static final SavedDataType<WeaponLeaderboard> TYPE = new SavedDataType<>(DATA_ID,
            WeaponLeaderboard::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, Entry> entries = new HashMap<>();

    /**
     * @param weaponName title if the weapon has one, otherwise its item name at the time of the record
     */
    public record Entry(UUID weaponId, String weaponName, String owner, int level, long totalXp, long kills) {
    }

    private WeaponLeaderboard() {
    }

    public static WeaponLeaderboard get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static void record(ServerPlayer owner, ItemStack weapon, WeaponProgress progress) {
        WeaponChronicle chronicle = ChronicleService.read(weapon);
        String name = chronicle.hasTitle() ? chronicle.title() : weapon.getHoverName().getString();
        get(owner.level().getServer()).put(new Entry(progress.instanceId(), name, owner.getGameProfile().name(),
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

    private static WeaponLeaderboard of(List<Entry> loaded) {
        WeaponLeaderboard leaderboard = new WeaponLeaderboard();
        for (Entry entry : loaded) {
            leaderboard.entries.put(entry.weaponId(), entry);
        }
        return leaderboard;
    }
}
