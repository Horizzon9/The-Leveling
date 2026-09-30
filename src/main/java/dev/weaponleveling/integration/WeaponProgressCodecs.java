package dev.weaponleveling.integration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.model.LegacyTier;
import net.minecraft.core.UUIDUtil;

import java.util.Map;
import java.util.function.Function;

public final class WeaponProgressCodecs {
    private static final Codec<Map<String, Integer>> RANKS_CODEC = Codec.unboundedMap(Codec.STRING, Codec.INT);
    private static final Codec<LegacyTier> LEGACY_TIER_CODEC = Codec.STRING.xmap(
            LegacyTier::fromSerializedName, LegacyTier::serializedName);

    private static final Codec<WeaponProgress> STORED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            // Components written before the level-zero migration did not necessarily contain this field.
            Codec.INT.optionalFieldOf("schema_version", 1).forGetter(WeaponProgress::schemaVersion),
            UUIDUtil.CODEC.fieldOf("instance_id").forGetter(WeaponProgress::instanceId),
            Codec.INT.fieldOf("level").forGetter(WeaponProgress::level),
            Codec.LONG.fieldOf("current_xp").forGetter(WeaponProgress::currentXp),
            Codec.LONG.fieldOf("total_xp").forGetter(WeaponProgress::totalXp),
            Codec.INT.fieldOf("unspent_points").forGetter(WeaponProgress::unspentPoints),
            RANKS_CODEC.optionalFieldOf("upgrade_ranks", Map.of()).forGetter(WeaponProgress::upgradeRanks),
            Codec.DOUBLE.optionalFieldOf("total_damage_dealt", 0.0).forGetter(WeaponProgress::totalDamageDealt),
            Codec.LONG.optionalFieldOf("total_kills", 0L).forGetter(WeaponProgress::totalKills),
            LEGACY_TIER_CODEC.optionalFieldOf("legacy_tier", LegacyTier.NONE).forGetter(WeaponProgress::legacyTier),
            Codec.DOUBLE.optionalFieldOf("base_attack_damage", 0.0).forGetter(WeaponProgress::baseAttackDamage)
    ).apply(instance, WeaponProgress::new));

    public static final Codec<WeaponProgress> CODEC = STORED_CODEC.xmap(
            WeaponProgress::migrateFromStoredSchema, Function.identity());

    private WeaponProgressCodecs() {
    }
}
