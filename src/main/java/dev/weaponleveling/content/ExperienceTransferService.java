package dev.weaponleveling.content;

import dev.weaponleveling.config.ServerConfig;
import dev.weaponleveling.core.model.WeaponProgress;
import dev.weaponleveling.core.xp.ProgressionEngine;
import dev.weaponleveling.integration.ArmorEligibility;
import dev.weaponleveling.integration.BrokenWeaponHandler;
import dev.weaponleveling.integration.LevelableItemEligibility;
import dev.weaponleveling.integration.WeaponComponents;
import dev.weaponleveling.integration.WeaponDamage;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** Validates and builds the irreversible result of an item-XP transfer. */
public final class ExperienceTransferService {
    private ExperienceTransferService() {
    }

    public static TransferResult preview(ItemStack source, ItemStack target) {
        if (!LevelableItemEligibility.isEligibleOnServer(source)) {
            return new TransferResult.Failure(FailureReason.INVALID_SOURCE);
        }
        if (!LevelableItemEligibility.isEligibleOnServer(target) || BrokenWeaponHandler.isBroken(target)) {
            return new TransferResult.Failure(FailureReason.INVALID_TARGET);
        }
        if (!sameEquipmentFamily(source, target)) {
            return new TransferResult.Failure(FailureReason.INCOMPATIBLE_TARGET);
        }

        WeaponProgress sourceProgress = source.get(WeaponComponents.PROGRESS.get());
        if (sourceProgress == null || retainedXp(sourceProgress.totalXp()) == 0) {
            return new TransferResult.Failure(FailureReason.NO_TRANSFERABLE_XP);
        }
        WeaponProgress targetProgress = target.get(WeaponComponents.PROGRESS.get());

        long transferredXp = retainedXp(sourceProgress.totalXp());
        long targetXp = targetProgress == null ? 0 : targetProgress.totalXp();
        long resultingXp = combinedXp(targetXp, transferredXp);
        WeaponProgress baseProgress = targetProgress == null
                ? WeaponProgress.create(WeaponDamage.baseAttackDamage(target))
                : targetProgress;
        WeaponProgress rebuilt = ProgressionEngine.setTotalXp(baseProgress, resultingXp,
                ServerConfig.xpCurve(), ServerConfig.legacyPolicy());
        ItemStack output = target.copyWithCount(1);
        output.set(WeaponComponents.PROGRESS.get(), rebuilt);
        return new TransferResult.Success(output, transferredXp, rebuilt.level());
    }

    static long retainedXp(long totalXp) {
        return Math.max(0, totalXp) / 2;
    }

    static long combinedXp(long targetXp, long transferredXp) {
        long safeTargetXp = Math.max(0, targetXp);
        long safeTransferredXp = Math.max(0, transferredXp);
        if (safeTransferredXp > Long.MAX_VALUE - safeTargetXp) {
            return Long.MAX_VALUE;
        }
        return safeTargetXp + safeTransferredXp;
    }

    private static boolean sameEquipmentFamily(ItemStack source, ItemStack target) {
        boolean sourceArmor = ArmorEligibility.isEligible(source);
        boolean targetArmor = ArmorEligibility.isEligible(target);
        if (sourceArmor != targetArmor) {
            return false;
        }
        if (!sourceArmor) {
            return true;
        }
        if (!(source.getItem() instanceof ArmorItem sourceArmorItem)
                || !(target.getItem() instanceof ArmorItem targetArmorItem)) {
            return false;
        }
        return sourceArmorItem.getEquipmentSlot() == targetArmorItem.getEquipmentSlot();
    }

    public sealed interface TransferResult {
        record Success(ItemStack output, long transferredXp, int resultingLevel) implements TransferResult {
        }

        record Failure(FailureReason reason) implements TransferResult {
        }
    }

    public enum FailureReason {
        INVALID_SOURCE("container.weaponleveling.legacy_transfer.error.invalid_source"),
        INVALID_TARGET("container.weaponleveling.legacy_transfer.error.invalid_target"),
        INCOMPATIBLE_TARGET("container.weaponleveling.legacy_transfer.error.incompatible_target"),
        NO_TRANSFERABLE_XP("container.weaponleveling.legacy_transfer.error.no_transferable_xp");

        private final String translationKey;

        FailureReason(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }
}
