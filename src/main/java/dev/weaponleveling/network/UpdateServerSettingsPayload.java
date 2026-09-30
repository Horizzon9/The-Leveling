package dev.weaponleveling.network;

import dev.weaponleveling.WeaponLevelingMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Client request containing a bounded, atomically validated set of setting edits. */
public record UpdateServerSettingsPayload(List<SettingChange> changes) implements CustomPacketPayload {
    private static final int MAX_CHANGES = 16;
    public static final Type<UpdateServerSettingsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(WeaponLevelingMod.MOD_ID, "update_server_settings"));
    public static final net.minecraft.network.codec.StreamCodec<RegistryFriendlyByteBuf, UpdateServerSettingsPayload> STREAM_CODEC =
            new net.minecraft.network.codec.StreamCodec<>() {
                @Override
                public UpdateServerSettingsPayload decode(RegistryFriendlyByteBuf buffer) {
                    int count = buffer.readVarInt();
                    if (count < 1 || count > MAX_CHANGES) {
                        throw new DecoderException("Invalid settings change count: " + count);
                    }
                    var changes = new java.util.ArrayList<SettingChange>(count);
                    for (int index = 0; index < count; index++) {
                        changes.add(new SettingChange(buffer.readUtf(32), buffer.readUtf(64)));
                    }
                    return new UpdateServerSettingsPayload(changes);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, UpdateServerSettingsPayload payload) {
                    buffer.writeVarInt(payload.changes().size());
                    for (SettingChange change : payload.changes()) {
                        buffer.writeUtf(change.key(), 32);
                        buffer.writeUtf(change.value(), 64);
                    }
                }
            };

    public UpdateServerSettingsPayload {
        changes = List.copyOf(changes);
        if (changes.isEmpty() || changes.size() > MAX_CHANGES) {
            throw new IllegalArgumentException("Settings changes must contain 1 to " + MAX_CHANGES + " entries");
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record SettingChange(String key, String value) {
        public SettingChange {
            if (key == null || key.length() > 32 || value == null || value.length() > 64) {
                throw new IllegalArgumentException("Setting change is too large");
            }
        }
    }
}
