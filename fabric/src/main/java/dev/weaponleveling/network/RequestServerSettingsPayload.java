package dev.weaponleveling.network;

import dev.weaponleveling.WeaponLevelingMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client request used when an inventory or the settings screen opens to refresh the authoritative snapshot. */
public record RequestServerSettingsPayload() implements CustomPacketPayload {
    public static final Type<RequestServerSettingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "request_server_settings"));
    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, RequestServerSettingsPayload> STREAM_CODEC =
            new net.minecraft.network.codec.StreamCodec<>() {
                @Override
                public RequestServerSettingsPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new RequestServerSettingsPayload();
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, RequestServerSettingsPayload payload) {
                    // Marker payload: no data comes from the client.
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
