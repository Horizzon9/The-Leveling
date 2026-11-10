package dev.weaponleveling.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.IntFunction;

/**
 * Codecs for saved data that keep what they can read instead of rejecting the whole value, and that
 * bring item stacks written by an older game version up to date.
 */
final class LenientCodecs {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DATA_VERSION_KEY = "DataVersion";

    private LenientCodecs() {
    }

    /**
     * A list that skips elements which fail to decode, so one damaged entry cannot erase the others.
     *
     * @param what names an element in the log line written for each skipped one
     */
    static <A> Codec<List<A>> listOf(Codec<A> element, String what) {
        return Codec.of(element.listOf(), new Decoder<>() {
            @Override
            public <T> DataResult<Pair<List<A>, T>> decode(DynamicOps<T> ops, T input) {
                return ops.getStream(input).map(stream -> Pair.of(stream
                        .flatMap(item -> element.parse(ops, item)
                                .ifError(error -> LOGGER.error("Skipping unreadable {}: {}", what, error.message()))
                                .result().stream())
                        .toList(), input));
            }
        });
    }

    /**
     * Stamps what is written with the current data version, the way vanilla stamps its own saved
     * data, and reads it with the codec made for the version it was written in. Data without a
     * stamp is taken to be from {@code unstampedVersion}.
     */
    static <A> Codec<A> versioned(IntFunction<Codec<A>> codecForVersion, int unstampedVersion) {
        return new Codec<>() {
            @Override
            public <T> DataResult<T> encode(A input, DynamicOps<T> ops, T prefix) {
                int version = currentDataVersion();
                return codecForVersion.apply(version).encode(input, ops, prefix).flatMap(encoded ->
                        ops.mergeToMap(encoded, ops.createString(DATA_VERSION_KEY), ops.createInt(version)));
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                int version = ops.get(input, DATA_VERSION_KEY).flatMap(ops::getNumberValue)
                        .map(Number::intValue).result().orElse(unstampedVersion);
                return codecForVersion.apply(version).decode(ops, ops.remove(input, DATA_VERSION_KEY));
            }
        };
    }

    /** Item stacks as written by the given data version; older ones are data-fixed before they are read. */
    static Codec<ItemStack> itemStack(int dataVersion) {
        int current = currentDataVersion();
        if (dataVersion >= current) {
            return ItemStack.CODEC;
        }
        return Codec.of(ItemStack.CODEC, new Decoder<>() {
            @Override
            public <T> DataResult<Pair<ItemStack, T>> decode(DynamicOps<T> ops, T input) {
                try {
                    Dynamic<T> fixed = DataFixers.getDataFixer()
                            .update(References.ITEM_STACK, new Dynamic<>(ops, input), dataVersion, current);
                    return ItemStack.CODEC.decode(ops, fixed.getValue());
                } catch (RuntimeException exception) {
                    return DataResult.error(() -> "Could not update item from data version " + dataVersion + ": "
                            + exception);
                }
            }
        });
    }

    private static int currentDataVersion() {
        return SharedConstants.getCurrentVersion().dataVersion().version();
    }
}
