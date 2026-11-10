package dev.weaponleveling.integration;

import dev.weaponleveling.config.ServerConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/** Determines whether an item is a levelable weapon without requiring hard dependencies on other mods. */
public final class WeaponEligibility {
    private static final List<Predicate<ItemStack>> EXTERNAL_WEAPON_SOURCES = new CopyOnWriteArrayList<>();

    private WeaponEligibility() {
    }

    /**
     * Lets a companion mod mark items as weapons (for example everything Better Combat gives a weapon
     * preset). Checked on both sides after the tags and regardless of auto-detection; excluded items and
     * levelable tools stay out. Register once, during mod construction.
     */
    public static void registerWeaponSource(Predicate<ItemStack> source) {
        EXTERNAL_WEAPON_SOURCES.add(source);
    }

    /** Default client-side compatibility: standard tags plus automatic external-weapon detection. */
    public static boolean isEligible(ItemStack stack) {
        return isEligible(stack, true, List.of(), List.of());
    }

    /** Uses the authoritative server compatibility configuration for progression and commands. */
    public static boolean isEligibleOnServer(ItemStack stack) {
        return isEligible(stack, ServerConfig.AUTO_DETECT_EXTERNAL_WEAPONS.get(),
                ServerConfig.ADDITIONAL_WEAPON_ITEMS.get(), ServerConfig.EXCLUDED_WEAPON_ITEMS.get());
    }

    /** Client-side variant using the automatic-detection setting received from the server. */
    public static boolean isEligible(ItemStack stack, boolean autoDetectExternalWeapons) {
        return isEligible(stack, autoDetectExternalWeapons, List.of(), List.of());
    }

    private static boolean isEligible(ItemStack stack, boolean autoDetectExternalWeapons,
                                      Collection<? extends String> additionalItems,
                                      Collection<? extends String> excludedItems) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(WeaponTags.EXCLUDED_WEAPONS)) {
            return false;
        }
        // This runs per inventory slot every rendered frame (item decorations, tooltips), and the
        // additional/excluded config lists are empty by default, so the registry-id string is only
        // resolved when one of them actually has entries to check against.
        ResourceLocation itemId = null;
        if (!excludedItems.isEmpty() || !additionalItems.isEmpty()) {
            itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            String idString = itemId.toString();
            if (excludedItems.contains(idString)) {
                return false;
            }
            if (additionalItems.contains(idString)) {
                return true;
            }
        }
        if (stack.is(WeaponTags.LEVELABLE_WEAPONS)
                || stack.is(WeaponTags.LEVELABLE_SWORDS)
                || stack.is(WeaponTags.LEVELABLE_AXES)
                || stack.is(WeaponTags.LEVELABLE_RANGED_WEAPONS)
                || stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.getItem() instanceof ProjectileWeaponItem) {
            return true;
        }
        if (isFromWeaponSource(stack)) {
            return true;
        }
        if (!autoDetectExternalWeapons) {
            return false;
        }
        return isExternalAttackWeapon(stack, itemId != null ? itemId : BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static WeaponType typeOf(ItemStack stack) {
        if (stack.is(WeaponTags.LEVELABLE_SWORDS) || stack.is(ItemTags.SWORDS)) {
            return WeaponType.SWORD;
        }
        if (stack.is(WeaponTags.LEVELABLE_AXES) || stack.is(ItemTags.AXES)) {
            return WeaponType.AXE;
        }
        return WeaponType.GENERIC;
    }

    public static boolean isRanged(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(WeaponTags.LEVELABLE_RANGED_WEAPONS)
                || stack.getItem() instanceof ProjectileWeaponItem
                // Lets a config-added spear mod opt in without a hard dependency or a custom tag.
                || stack.getUseAnimation() == UseAnim.SPEAR);
    }

    private static boolean isFromWeaponSource(ItemStack stack) {
        if (EXTERNAL_WEAPON_SOURCES.isEmpty() || ToolEligibility.isEligible(stack)) {
            return false;
        }
        for (Predicate<ItemStack> source : EXTERNAL_WEAPON_SOURCES) {
            if (source.test(stack)) {
                return true;
            }
        }
        return false;
    }

    /** Dependency-free fallback for third-party melee weapons with non-standard item classes or tags. */
    private static boolean isExternalAttackWeapon(ItemStack stack, ResourceLocation itemId) {
        return !itemId.getNamespace().equals("minecraft")
                && stack.isDamageableItem()
                && !stack.is(ItemTags.PICKAXES)
                && !stack.is(ItemTags.SHOVELS)
                && !stack.is(ItemTags.HOES)
                && WeaponDamage.baseAttackDamage(stack) > 0;
    }
}
