package dev.weaponleveling.network;

import dev.weaponleveling.WeaponLevelingMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Exact kill XP already granted by the authoritative server.
 *
 * @param amount XP added to the weapon
 * @param streak kill streak the XP belongs to; zero while streaks are disabled
 */
public record KillXpPayload(long amount, int streak) implements CustomPacketPayload {
    public static final Type<KillXpPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "kill_xp"));
    public static final StreamCodec<RegistryFriendlyByteBuf, KillXpPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public KillXpPayload decode(RegistryFriendlyByteBuf buffer) {
            long amount = buffer.readVarLong();
            int streak = buffer.readVarInt();
            if (amount <= 0 || streak < 0) {
                throw new DecoderException("Invalid kill XP payload");
            }
            return new KillXpPayload(amount, streak);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, KillXpPayload payload) {
            buffer.writeVarLong(payload.amount());
            buffer.writeVarInt(payload.streak());
        }
    };

    public KillXpPayload {
        if (amount <= 0 || streak < 0) {
            throw new IllegalArgumentException("Kill XP must be positive and the streak non-negative");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
