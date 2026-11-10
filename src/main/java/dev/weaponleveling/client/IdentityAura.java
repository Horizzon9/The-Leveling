package dev.weaponleveling.client;

import dev.weaponleveling.config.ClientConfig;
import dev.weaponleveling.core.model.LegacyTier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * The amber aura of Relic and Legacy items, drawn from geometry and vanilla particles only.
 *
 * In inventories and the hotbar the item gets softly pulsing amber corner brackets; a Legacy item
 * also has a bright spark that circles its slot. In the world, an amber dust drifts up from the
 * hand of any nearby player holding such an item. Everything runs on the client.
 */
public final class IdentityAura {
    private static final int RELIC_RGB = 0xE0A847;
    private static final int LEGACY_RGB = 0xFFD75E;
    private static final int SPARK_RGB = 0xFFF4C2;
    private static final int SLOT_PERIMETER = 60;
    private static final double PARTICLE_RANGE_SQR = 32 * 32;
    private static final DustParticleOptions RELIC_DUST = new DustParticleOptions(new Vector3f(0.88F, 0.66F, 0.28F), 0.6F);
    private static final DustParticleOptions LEGACY_DUST = new DustParticleOptions(new Vector3f(1.0F, 0.84F, 0.37F), 0.75F);

    private static int tickCount;

    private IdentityAura() {
    }

    public static void drawSlot(GuiGraphics graphics, ItemStack stack, int x, int y) {
        LegacyTier tier = ItemNameStyle.identityTier(stack);
        if (tier == LegacyTier.NONE || !ClientConfig.IDENTITY_AURA.get()) {
            return;
        }
        boolean legacy = tier == LegacyTier.LEGACY;
        long now = Util.getMillis();
        float pulse = 0.5F + 0.5F * (float) Math.sin(now / 400.0);
        int alpha = legacy ? 150 + Math.round(pulse * 105) : 110 + Math.round(pulse * 70);
        int color = (alpha << 24) | (legacy ? LEGACY_RGB : RELIC_RGB);

        graphics.fill(x, y, x + 3, y + 1, color);
        graphics.fill(x, y, x + 1, y + 3, color);
        graphics.fill(x + 13, y, x + 16, y + 1, color);
        graphics.fill(x + 15, y, x + 16, y + 3, color);
        graphics.fill(x, y + 15, x + 3, y + 16, color);
        graphics.fill(x, y + 13, x + 1, y + 16, color);
        graphics.fill(x + 13, y + 15, x + 16, y + 16, color);
        graphics.fill(x + 15, y + 13, x + 16, y + 16, color);

        if (legacy) {
            int head = (int) ((now / 30) % SLOT_PERIMETER);
            int[] trailAlpha = {255, 150, 70};
            for (int step = 0; step < trailAlpha.length; step++) {
                int position = Math.floorMod(head - step, SLOT_PERIMETER);
                int px = x + perimeterX(position);
                int py = y + perimeterY(position);
                graphics.fill(px, py, px + 1, py + 1, (trailAlpha[step] << 24) | SPARK_RGB);
            }
        }
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()
                || !ClientConfig.IDENTITY_AURA.get()) {
            return;
        }
        tickCount++;
        for (Player player : minecraft.level.players()) {
            LegacyTier tier = ItemNameStyle.identityTier(player.getMainHandItem());
            if (tier == LegacyTier.NONE || player.isInvisible()
                    || player.distanceToSqr(minecraft.player) > PARTICLE_RANGE_SQR) {
                continue;
            }
            int interval = tier == LegacyTier.LEGACY ? 3 : 7;
            // The own hand is right in front of the camera in first person; the slot aura is enough there.
            boolean ownFirstPerson = player == minecraft.player && minecraft.options.getCameraType().isFirstPerson();
            if (ownFirstPerson || (tickCount + player.getId()) % interval != 0) {
                continue;
            }
            Vec3 hand = handPosition(player);
            RandomSource random = player.getRandom();
            minecraft.level.addParticle(tier == LegacyTier.LEGACY ? LEGACY_DUST : RELIC_DUST,
                    hand.x + (random.nextDouble() - 0.5) * 0.3,
                    hand.y + (random.nextDouble() - 0.5) * 0.3,
                    hand.z + (random.nextDouble() - 0.5) * 0.3,
                    0, 0.02, 0);
        }
    }

    /** Rough world position of the main hand: beside the body, at waist height, slightly forward. */
    private static Vec3 handPosition(Player player) {
        double yaw = Math.toRadians(player.yBodyRot);
        double side = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        double rightX = -Math.cos(yaw) * side;
        double rightZ = -Math.sin(yaw) * side;
        double forwardX = -Math.sin(yaw);
        double forwardZ = Math.cos(yaw);
        return player.position().add(rightX * 0.4 + forwardX * 0.25, player.getBbHeight() * 0.5,
                rightZ * 0.4 + forwardZ * 0.25);
    }

    private static int perimeterX(int position) {
        if (position < 16) {
            return position;
        }
        if (position < 31) {
            return 15;
        }
        if (position < 46) {
            return 15 - (position - 30);
        }
        return 0;
    }

    private static int perimeterY(int position) {
        if (position < 16) {
            return 0;
        }
        if (position < 31) {
            return position - 15;
        }
        if (position < 46) {
            return 15;
        }
        return 15 - (position - 45);
    }
}
