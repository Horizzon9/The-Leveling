package dev.weaponleveling.network;

import dev.weaponleveling.WeaponLevelingMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Requests that the server validate and share one slot from the player's current menu. */
public record ShareItemPayload(int containerId, int menuSlot) implements CustomPacketPayload {
    public static final Type<ShareItemPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "share_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShareItemPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ShareItemPayload decode(RegistryFriendlyByteBuf buffer) {
            int containerId = buffer.readVarInt();
            int menuSlot = buffer.readVarInt();
            if (containerId < 0 || menuSlot < 0 || menuSlot > 1_024) {
                throw new DecoderException("Invalid item share payload");
            }
            return new ShareItemPayload(containerId, menuSlot);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ShareItemPayload payload) {
            buffer.writeVarInt(payload.containerId());
            buffer.writeVarInt(payload.menuSlot());
        }
    };

    public ShareItemPayload {
        if (containerId < 0 || menuSlot < 0 || menuSlot > 1_024) {
            throw new IllegalArgumentException("Invalid item share slot");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
