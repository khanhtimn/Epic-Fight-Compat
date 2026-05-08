package dev.khanhtimn.efcompat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.EnumSet;

public final class CombatHelper {
    private CombatHelper() {}

    private static final EnumSet<WeaponCategories> NON_COMBAT_CATEGORIES = EnumSet.of(
            WeaponCategories.NOT_WEAPON
    );

    private static final EnumSet<LivingMotions> ACTIVE_COMBAT_MOTIONS = EnumSet.of(
            LivingMotions.INACTION, LivingMotions.DIGGING,
            LivingMotions.EAT, LivingMotions.DRINK,
            LivingMotions.AIM, LivingMotions.BLOCK,
            LivingMotions.BLOCK_SHIELD, LivingMotions.RELOAD,
            LivingMotions.SHOT, LivingMotions.SPELLCAST
    );

    public static boolean isCombatItem(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (ClientConfig.combatCategorizedItems.contains(stack.getItem())) return true;
        if (ClientConfig.miningCategorizedItems.contains(stack.getItem())) return false;
        if (!Config.USE_WEAPON_CATEGORY_DETECTION.get()) return false;

        CapabilityItem cap = EpicFightCapabilities.getItemStackCapability(stack);
        if (cap.isEmpty()) return false;

        WeaponCategory category = cap.getWeaponCategory();
        return !(category instanceof WeaponCategories wc && NON_COMBAT_CATEGORIES.contains(wc));
    }

    public static boolean isEpicFightIdle(LocalPlayerPatch playerPatch) {
        if (playerPatch.getPovSettings() != null) return false;

        LivingMotion compositeMotion = playerPatch.currentCompositeMotion;
        if (compositeMotion instanceof LivingMotions cm && ACTIVE_COMBAT_MOTIONS.contains(cm)) {
            return false;
        }

        LivingMotion motion = playerPatch.getCurrentLivingMotion();
        if (motion instanceof LivingMotions m) {
            return !ACTIVE_COMBAT_MOTIONS.contains(m);
        }
        return true;
    }

    public static boolean isOffhandValidForWeapon(LocalPlayerPatch playerPatch) {
        CapabilityItem cap = playerPatch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        if (cap.isEmpty()) return true;
        return cap.getStyle(playerPatch).canUseOffhand();
    }
}
