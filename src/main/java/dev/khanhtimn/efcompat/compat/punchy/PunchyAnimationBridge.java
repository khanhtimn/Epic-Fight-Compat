package dev.khanhtimn.efcompat.compat.punchy;

import dev.khanhtimn.efcompat.CombatHelper;
import dev.khanhtimn.efcompat.Config;
import dev.khanhtimn.efcompat.animation.AnimationPriorityEngine;
import dev.khanhtimn.efcompat.animation.CompatAnimationAdapter;
import dev.khanhtimn.efcompat.animation.EpicFightAnimationState;
import dev.khanhtimn.efcompat.animation.MotionContext;
import dev.khanhtimn.efcompat.animation.MotionOwner;
import net.minecraft.client.Minecraft;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

public final class PunchyAnimationBridge implements CompatAnimationAdapter {

    public static final PunchyAnimationBridge INSTANCE = new PunchyAnimationBridge();

    private PunchyAnimationBridge() {
    }

    private PunchyCompatState currentState = PunchyCompatState.PUNCHY;
    private PunchyCompatState previousState = PunchyCompatState.PUNCHY;
    private LivingMotion currentBaseMotion = LivingMotions.IDLE;
    private boolean active = false;
    private boolean raiseOnlyHandoff = false;
    private boolean lastTwoHandedInEF = false;

    @Override
    public void onMotionResolved(MotionOwner owner, MotionContext ctx) {
        currentBaseMotion = ctx.baseMotion();
        previousState = currentState;
        currentState = toState(owner, ctx);
    }

    @Override
    public void onEpicFightDisabled() {
        active = false;
        previousState = currentState;
        currentState = PunchyCompatState.PUNCHY;
    }

    public PunchyCompatState computeState() {
        if (!Config.PUNCHY_IDLE_OVERRIDE.get()) {
            active = false;
            previousState = currentState;
            currentState = PunchyCompatState.PUNCHY;
            return currentState;
        }

        LocalPlayerPatch patch = resolveLocalPatch();
        if (patch == null || !patch.isEpicFightMode()) {
            if (active) {
                onEpicFightDisabled();
            } else {
                previousState = currentState;
            }
            return currentState;
        }

        active = true;
        MotionContext ctx = EpicFightAnimationState.snapshot(patch);
        // snapshot() only returns null when !isEpicFightMode(), already guarded above
        assert ctx != null;
        previousState = currentState;
        currentState = toState(determineOwner(ctx), ctx);
        currentBaseMotion = ctx.baseMotion();
        if (currentState == PunchyCompatState.EPICFIGHT) {
            lastTwoHandedInEF = ctx.isTwoHanded();
        }
        return currentState;
    }

    public boolean shouldCancelPunchyRender() {
        return currentState == PunchyCompatState.EPICFIGHT;
    }

    /**
     * True when EF's own first-person hand render should yield to Punchy.
     */
    public boolean shouldCancelEpicFightRender() {
        return active && currentState != PunchyCompatState.EPICFIGHT;
    }

    public boolean shouldSuppressMovementStateMachine() {
        return currentState == PunchyCompatState.EPICFIGHT;
    }

    public boolean shouldSuppressSwimHandler() {
        if (currentState == PunchyCompatState.EPICFIGHT) {
            return true;
        }
        return currentState == PunchyCompatState.SHARED && currentBaseMotion == LivingMotions.SWIM;
    }

    public boolean shouldSuppressOffhand() {
        return currentState == PunchyCompatState.PUNCHY_SUPPRESSED;
    }

    public PunchyCompatState getState() {
        return currentState;
    }

    public PunchyCompatState getPreviousState() {
        return previousState;
    }

    public boolean isRaiseOnlyHandoff() {
        return raiseOnlyHandoff;
    }

    public void beginRaiseOnlyHandoff() {
        raiseOnlyHandoff = true;
    }

    public void endRaiseOnlyHandoff() {
        raiseOnlyHandoff = false;
    }

    public boolean wasLastEpicFightTwoHanded() {
        return lastTwoHandedInEF;
    }

    private static MotionOwner determineOwner(MotionContext ctx) {
        // Delegate to the engine for lookup — engine resolves EPIC_FIGHT_IF_ARMED as well.
        // We call resolve() directly here rather than resolveAndDispatch() to avoid
        // double-dispatching (computeState() is not called via the adapter pathway).
        return AnimationPriorityEngine.resolve(ctx);
    }

    private PunchyCompatState toState(MotionOwner owner, MotionContext ctx) {
        return switch (owner) {
            case EPIC_FIGHT ->
                PunchyCompatState.EPICFIGHT;
            case SHARED ->
                PunchyCompatState.SHARED;
            case COMPAT, EPIC_FIGHT_IF_ARMED, EPIC_FIGHT_IF_TWO_HANDED ->
                resolveOffhandState();
        };
    }

    private PunchyCompatState resolveOffhandState() {
        LocalPlayerPatch patch = resolveLocalPatch();
        if (patch != null && !CombatHelper.isOffhandValidForWeapon(patch)) {
            return PunchyCompatState.PUNCHY_SUPPRESSED;
        }
        return PunchyCompatState.PUNCHY;
    }

    private static LocalPlayerPatch resolveLocalPatch() {
        var ref = new Object() {
            LocalPlayerPatch patch = null;
        };
        EpicFightCapabilities.getUnparameterizedEntityPatch(
                Minecraft.getInstance().player,
                LocalPlayerPatch.class
        ).ifPresent(p -> ref.patch = p);
        return ref.patch;
    }
}
