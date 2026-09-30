package dev.weaponleveling.network;

import dev.weaponleveling.WeaponLevelingMod;
import dev.weaponleveling.core.model.LegacyTier;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server-confirmed level-up presentation data for the owner of a levelable item. */
public record LevelUpSealPayload(String itemId, String itemName, int level, LegacyTier legacyTier,
                                 boolean identityUnlocked, boolean showOverlay) implements CustomPacketPayload {
    private static final int MAX_ITEM_ID_LENGTH = 128;
    private static final int MAX_ITEM_NAME_LENGTH = 256;
    public static final Type<LevelUpSealPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "level_up_seal"));
    public static final StreamCodec<RegistryFriendlyByteBuf, LevelUpSealPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LevelUpSealPayload decode(RegistryFriendlyByteBuf buffer) {
            String itemId = buffer.readUtf(MAX_ITEM_ID_LENGTH);
            String itemName = buffer.readUtf(MAX_ITEM_NAME_LENGTH);
            int level = buffer.readVarInt();
            if (ResourceLocation.tryParse(itemId) == null || level < 0 || level > 10_000) {
                throw new DecoderException("Invalid level-up seal payload");
            }
            LegacyTier legacyTier = LegacyTier.fromSerializedName(buffer.readUtf(16));
            return new LevelUpSealPayload(itemId, itemName, level, legacyTier,
                    buffer.readBoolean(), buffer.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, LevelUpSealPayload payload) {
            buffer.writeUtf(payload.itemId, MAX_ITEM_ID_LENGTH);
            buffer.writeUtf(payload.itemName, MAX_ITEM_NAME_LENGTH);
            buffer.writeVarInt(payload.level);
            buffer.writeUtf(payload.legacyTier.serializedName(), 16);
            buffer.writeBoolean(payload.identityUnlocked);
            buffer.writeBoolean(payload.showOverlay);
        }
    };

    public LevelUpSealPayload {
        if (ResourceLocation.tryParse(itemId) == null || itemName == null || legacyTier == null
                || itemName.length() > MAX_ITEM_NAME_LENGTH
                || level < 0 || level > 10_000) {
            throw new IllegalArgumentException("Invalid level-up seal payload");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
