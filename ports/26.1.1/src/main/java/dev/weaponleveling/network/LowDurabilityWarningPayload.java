package dev.weaponleveling.network;

import dev.weaponleveling.WeaponLevelingMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record LowDurabilityWarningPayload(String itemName, int remaining, int maxDamage) implements CustomPacketPayload {
    private static final int MAX_NAME_LENGTH = 256;
    public static final Type<LowDurabilityWarningPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "low_durability_warning"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LowDurabilityWarningPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public LowDurabilityWarningPayload decode(RegistryFriendlyByteBuf buffer) {
            String itemName = buffer.readUtf(MAX_NAME_LENGTH);
            int remaining = buffer.readVarInt();
            int maxDamage = buffer.readVarInt();
            if (remaining < 0 || maxDamage < 0) {
                throw new DecoderException("Invalid low durability payload");
            }
            return new LowDurabilityWarningPayload(itemName, remaining, maxDamage);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, LowDurabilityWarningPayload payload) {
            buffer.writeUtf(payload.itemName(), MAX_NAME_LENGTH);
            buffer.writeVarInt(payload.remaining());
            buffer.writeVarInt(payload.maxDamage());
        }
    };

    public LowDurabilityWarningPayload {
        if (itemName == null || itemName.length() > MAX_NAME_LENGTH || remaining < 0 || maxDamage < 0) {
            throw new IllegalArgumentException("Invalid low durability payload");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
