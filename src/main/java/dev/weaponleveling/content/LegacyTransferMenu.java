package dev.weaponleveling.content;

import dev.weaponleveling.content.ExperienceTransferService.TransferResult;
import dev.weaponleveling.content.ExperienceTransferService.FailureReason;
import dev.weaponleveling.integration.LevelableItemEligibility;
import dev.weaponleveling.integration.WeaponComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Three-input smithing-style menu: template + source + clean target -> transferred target. */
public final class LegacyTransferMenu extends ItemCombinerMenu {
    public static final int TEMPLATE_SLOT = 0;
    public static final int SOURCE_SLOT = 1;
    public static final int TARGET_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    private static final int INPUT_Y = 40;

    @Nullable
    private TransferResult.Success selectedTransfer;
    @Nullable
    private FailureReason failureReason;

    public LegacyTransferMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public LegacyTransferMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModContent.LEGACY_TRANSFER_MENU.get(), containerId, playerInventory, access);
    }

    @Override
    protected ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(TEMPLATE_SLOT, 24, INPUT_Y,
                        stack -> stack.is(ModContent.LEGACY_TRANSFER_TEMPLATE.get()))
                .withSlot(SOURCE_SLOT, 42, INPUT_Y, LevelableItemEligibility::isEligible)
                .withSlot(TARGET_SLOT, 85, INPUT_Y, LevelableItemEligibility::isEligible)
                .withResultSlot(RESULT_SLOT, 136, INPUT_Y)
                .build();
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(ModContent.LEGACY_TRANSFER_TABLE.get());
    }

    @Override
    protected boolean mayPickup(Player player, boolean hasStack) {
        return hasStack && selectedTransfer != null;
    }

    @Override
    protected void onTake(Player player, ItemStack stack) {
        stack.onCraftedBy(player.level(), player, stack.getCount());
        shrinkInput(TEMPLATE_SLOT);
        shrinkInput(TARGET_SLOT);
        forgetSourceStanding(player);
        resetSourceProgress();
        selectedTransfer = null;
        access.execute((level, pos) -> level.levelEvent(1044, pos, 0));
    }

    @Override
    public void createResult() {
        selectedTransfer = null;
        failureReason = null;
        if (!inputSlots.getItem(TEMPLATE_SLOT).is(ModContent.LEGACY_TRANSFER_TEMPLATE.get())) {
            resultSlots.setItem(0, ItemStack.EMPTY);
            return;
        }
        TransferResult result = ExperienceTransferService.preview(
                inputSlots.getItem(SOURCE_SLOT), inputSlots.getItem(TARGET_SLOT));
        if (result instanceof TransferResult.Success success) {
            selectedTransfer = success;
            resultSlots.setItem(0, success.output());
        } else if (result instanceof TransferResult.Failure failure) {
            failureReason = failure.reason();
            resultSlots.setItem(0, ItemStack.EMPTY);
        }
    }

    @Nullable
    public FailureReason failureReason() {
        return failureReason;
    }

    @Override
    public int getSlotToQuickMoveTo(ItemStack stack) {
        if (stack.is(ModContent.LEGACY_TRANSFER_TEMPLATE.get())) {
            return TEMPLATE_SLOT;
        }
        var progress = stack.get(WeaponComponents.PROGRESS.get());
        if (progress != null && progress.totalXp() >= 2 && !getSlot(SOURCE_SLOT).hasItem()) {
            return SOURCE_SLOT;
        }
        return TARGET_SLOT;
    }

    @Override
    protected boolean canMoveIntoInputSlots(ItemStack stack) {
        return stack.is(ModContent.LEGACY_TRANSFER_TEMPLATE.get()) || LevelableItemEligibility.isEligible(stack);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultSlots && super.canTakeItemForPickAll(stack, slot);
    }

    private void shrinkInput(int slotIndex) {
        ItemStack stack = inputSlots.getItem(slotIndex);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            inputSlots.setItem(slotIndex, stack);
        }
    }

    private void forgetSourceStanding(Player player) {
        var progress = inputSlots.getItem(SOURCE_SLOT).get(WeaponComponents.PROGRESS.get());
        if (progress != null && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            dev.weaponleveling.server.WeaponLeaderboard.forget(serverPlayer.server, progress.instanceId());
        }
    }

    private void resetSourceProgress() {
        ItemStack source = inputSlots.getItem(SOURCE_SLOT);
        var progress = source.get(WeaponComponents.PROGRESS.get());
        if (progress != null) {
            source.set(WeaponComponents.PROGRESS.get(), progress.reset());
            // The source starts a new history; its chronicle is not part of the transferred XP.
            source.remove(WeaponComponents.CHRONICLE.get());
            inputSlots.setItem(SOURCE_SLOT, source);
        }
    }
}
