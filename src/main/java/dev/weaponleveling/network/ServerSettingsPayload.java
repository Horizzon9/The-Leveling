package dev.weaponleveling.network;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.config.ServerConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** A read-only snapshot of the server-owned settings shown on the client. */
public record ServerSettingsPayload(int maxLevel, long baseXp, double growthFactor,
                                    boolean allowCreativeProgress,
                                    int relicLevelPercent, int legacyLevelPercent,
                                    boolean killXpEnabled, boolean playerKillXpEnabled, double killBonus, long maxXpPerAttack,
                                    boolean armorLevelingEnabled, double armorXpPerDamage, long armorMaxXpPerDamageEvent,
                                    boolean armorLevelBonusEnabled, double armorFlatBonusPerLevel, double armorMaxLevelBonus,
                                    boolean levelBonusEnabled,
                                    double levelMaxMultiplier, double levelCurveExponent,
                                    boolean autoDetectExternalWeapons,
                                    List<Long> xpTable, boolean canConfigure, boolean usesExactXpTable,
                                    boolean scaleCurveWithMaxLevel, boolean preserveBrokenItems,
                                    boolean autoReplaceBroken) implements CustomPacketPayload {
    private static final int MAX_XP_TABLE_ENTRIES = 10_000;
    public static final Type<ServerSettingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "server_settings"));
    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, ServerSettingsPayload> STREAM_CODEC =
            new net.minecraft.network.codec.StreamCodec<>() {
                @Override
                public ServerSettingsPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new ServerSettingsPayload(buffer.readVarInt(), buffer.readVarLong(), buffer.readDouble(),
                            buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(),
                            buffer.readBoolean(), buffer.readBoolean(),
                            buffer.readDouble(), buffer.readVarLong(),
                            buffer.readBoolean(), buffer.readDouble(), buffer.readVarLong(),
                            buffer.readBoolean(), buffer.readDouble(), buffer.readDouble(),
                            buffer.readBoolean(), buffer.readDouble(),
                            buffer.readDouble(), buffer.readBoolean(), readXpTable(buffer),
                            buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(),
                            buffer.readBoolean(), buffer.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ServerSettingsPayload payload) {
                    buffer.writeVarInt(payload.maxLevel());
                    buffer.writeVarLong(payload.baseXp());
                    buffer.writeDouble(payload.growthFactor());
                    buffer.writeBoolean(payload.allowCreativeProgress());
                    buffer.writeVarInt(payload.relicLevelPercent());
                    buffer.writeVarInt(payload.legacyLevelPercent());
                    buffer.writeBoolean(payload.killXpEnabled());
                    buffer.writeBoolean(payload.playerKillXpEnabled());
                    buffer.writeDouble(payload.killBonus());
                    buffer.writeVarLong(payload.maxXpPerAttack());
                    buffer.writeBoolean(payload.armorLevelingEnabled());
                    buffer.writeDouble(payload.armorXpPerDamage());
                    buffer.writeVarLong(payload.armorMaxXpPerDamageEvent());
                    buffer.writeBoolean(payload.armorLevelBonusEnabled());
                    buffer.writeDouble(payload.armorFlatBonusPerLevel());
                    buffer.writeDouble(payload.armorMaxLevelBonus());
                    buffer.writeBoolean(payload.levelBonusEnabled());
                    buffer.writeDouble(payload.levelMaxMultiplier());
                    buffer.writeDouble(payload.levelCurveExponent());
                    buffer.writeBoolean(payload.autoDetectExternalWeapons());
                    writeXpTable(buffer, payload.xpTable());
                    buffer.writeBoolean(payload.canConfigure());
                    buffer.writeBoolean(payload.usesExactXpTable());
                    buffer.writeBoolean(payload.scaleCurveWithMaxLevel());
                    buffer.writeBoolean(payload.preserveBrokenItems());
                    buffer.writeBoolean(payload.autoReplaceBroken());
                }
            };

    public static ServerSettingsPayload defaults() {
        return new ServerSettingsPayload(500, 50, 1.02432611896515, false, 25, 100,
                true, false, 5, 10_000,
                true, 1.0, 10_000,
                true, 0.02, 20.0,
                true, 11.0, 1.05, true, List.of(), false, false, true, true, false);
    }

    public static ServerSettingsPayload fromServer(boolean canConfigure) {
        return new ServerSettingsPayload(ServerConfig.MAX_LEVEL.get(), ServerConfig.effectiveBaseXp(),
                ServerConfig.GROWTH_FACTOR.get(), ServerConfig.ALLOW_CREATIVE_PROGRESS.get(),
                ServerConfig.RELIC_LEVEL_PERCENT.get(), ServerConfig.LEGACY_LEVEL_PERCENT.get(),
                ServerConfig.KILL_XP_ENABLED.get(), ServerConfig.PLAYER_KILL_XP_ENABLED.get(),
                ServerConfig.KILL_BONUS.get(), ServerConfig.MAX_XP_PER_ATTACK.get(),
                ServerConfig.ARMOR_LEVELING_ENABLED.get(), ServerConfig.ARMOR_XP_PER_DAMAGE.get(),
                ServerConfig.ARMOR_MAX_XP_PER_DAMAGE_EVENT.get(),
                ServerConfig.ARMOR_LEVEL_BONUS_ENABLED.get(), ServerConfig.ARMOR_FLAT_BONUS_PER_LEVEL.get(),
                ServerConfig.ARMOR_MAX_LEVEL_BONUS.get(),
                ServerConfig.LEVEL_DAMAGE_BONUS_ENABLED.get(),
                ServerConfig.LEVEL_DAMAGE_MAX_MULTIPLIER.get(), ServerConfig.LEVEL_DAMAGE_CURVE_EXPONENT.get(),
                ServerConfig.AUTO_DETECT_EXTERNAL_WEAPONS.get(),
                ServerConfig.effectiveXpTable(),
                canConfigure, !ServerConfig.XP_TABLE.get().isEmpty(),
                ServerConfig.SCALE_CURVE_WITH_MAX_LEVEL.get(), ServerConfig.PRESERVE_BROKEN_ITEMS.get(),
                ServerConfig.AUTO_REPLACE_BROKEN.get());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    private static List<Long> readXpTable(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_XP_TABLE_ENTRIES) {
            throw new io.netty.handler.codec.DecoderException("Invalid XP table size: " + size);
        }
        var values = new java.util.ArrayList<Long>(size);
        for (int index = 0; index < size; index++) {
            long value = buffer.readVarLong();
            if (value < 1) {
                throw new io.netty.handler.codec.DecoderException("Invalid XP table value");
            }
            values.add(value);
        }
        return List.copyOf(values);
    }

    private static void writeXpTable(RegistryFriendlyByteBuf buffer, List<Long> xpTable) {
        buffer.writeVarInt(xpTable.size());
        for (long value : xpTable) {
            buffer.writeVarLong(value);
        }
    }
}
