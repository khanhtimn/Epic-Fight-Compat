package dev.khanhtimn.efcompat.animation;

import dev.khanhtimn.efcompat.mixins.epicfight.client.ClientAnimatorAccessor;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Builds {@link MotionContext} snapshots from the current EF local player state.
 */
public final class EpicFightAnimationState {

    private EpicFightAnimationState() {}

    @Nullable
    public static MotionContext snapshot(LocalPlayerPatch patch) {
        if (patch == null || !patch.isEpicFightMode()) return null;

        LivingMotion base = patch.getCurrentLivingMotion();
        CapabilityItem cap = patch.getHoldingItemCapability(InteractionHand.MAIN_HAND);
        boolean twoHanded = !cap.isEmpty() && !cap.canBePlacedOffhand();

        return new MotionContext(
                base,
                patch.currentCompositeMotion,
                patch.getPovSettings() != null,
                hasWeaponPoseFor(patch, base),
                twoHanded
        );
    }

    /**
     * True when the equipped weapon moveset has overridden the EF default animation
     * for {@code motion} — i.e. the weapon has a dedicated pose for this state.
     * The heuristic compares object identity: EF registers a single default
     * {@link AssetAccessor} per motion at startup. A weapon moveset swaps it for a
     * different accessor. Identity divergence means a weapon-specific override is active.
     */
    public static boolean hasWeaponPoseFor(LocalPlayerPatch patch, LivingMotion motion) {
        ClientAnimator animator = patch.getClientAnimator();
        ClientAnimatorAccessor accessor = (ClientAnimatorAccessor) (Object) animator;

        Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> live     = accessor.getCompositeLivingAnimations();
        Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> defaults = accessor.getDefaultLivingAnimations();

        AssetAccessor<? extends StaticAnimation> current = live.get(motion);
        AssetAccessor<? extends StaticAnimation> def     = defaults.get(motion);

        return current != null && current != def;
    }
}

