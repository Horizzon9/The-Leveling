package dev.weaponleveling.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Soulbound items waiting for their owner to respawn, saved with the world. */
public final class SoulboundStash extends SavedData {
    private static final Identifier DATA_ID = Identifier.fromNamespaceAndPath("weaponleveling", "soulbound");
    /** Data version of Minecraft 1.21.1, whose stash files carry no version stamp of their own. */
    private static final int UNSTAMPED_DATA_VERSION = 3955;
    private static final Codec<SoulboundStash> CODEC = LenientCodecs.versioned(SoulboundStash::codecFor,
            UNSTAMPED_DATA_VERSION);
    /** No data fixer touches this fix type; the item stacks are updated by {@link #CODEC} itself. */
    private static final SavedDataType<SoulboundStash> TYPE = new SavedDataType<>(DATA_ID,
            SoulboundStash::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, List<Entry>> stash = new HashMap<>();

    /** @param slot inventory slot the item was taken from */
    public record Entry(int slot, ItemStack stack) {
    }

    private record PlayerStash(UUID id, List<Entry> items) {
    }

    private SoulboundStash() {
    }

    public static SoulboundStash get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
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

    private static Codec<SoulboundStash> codecFor(int dataVersion) {
        Codec<Entry> entryCodec = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").orElse(0).forGetter(Entry::slot),
                LenientCodecs.itemStack(dataVersion).fieldOf("stack").forGetter(Entry::stack)
        ).apply(instance, Entry::new));
        Codec<PlayerStash> playerCodec = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(PlayerStash::id),
                LenientCodecs.listOf(entryCodec, "soulbound item").fieldOf("items").orElse(List.of())
                        .forGetter(PlayerStash::items)
        ).apply(instance, PlayerStash::new));
        return RecordCodecBuilder.create(instance -> instance.group(
                LenientCodecs.listOf(playerCodec, "soulbound player").fieldOf("players").orElse(List.of())
                        .forGetter(SoulboundStash::playerStashes)
        ).apply(instance, SoulboundStash::of));
    }

    private List<PlayerStash> playerStashes() {
        List<PlayerStash> players = new ArrayList<>(stash.size());
        stash.forEach((playerId, entries) -> players.add(new PlayerStash(playerId,
                entries.stream().filter(entry -> !entry.stack().isEmpty()).toList())));
        return players;
    }

    private static SoulboundStash of(List<PlayerStash> loaded) {
        SoulboundStash data = new SoulboundStash();
        for (PlayerStash player : loaded) {
            if (!player.items().isEmpty()) {
                data.stash.put(player.id(), new ArrayList<>(player.items()));
            }
        }
        return data;
    }
}
