package dev.khanhtimn.efcompat.compat.punchy;

import dev.khanhtimn.efcompat.CombatHelper;
import dev.khanhtimn.efcompat.Config;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

// TODO: Custom two-handed idle animations can be achieved via Punchy's resource pack system.
//  A pack could define CustomAnimationEntry entries with `dual_handed` effects for greatsword-type
//  items (matched via `items` lists against Epic Fight's weapon categories). This would allow
//  two-handed weapons to show a custom Punchy idle pose instead of full suppression.
public final class AnimationBridge {

    private AnimationBridge() {}

    public enum State {
        PUNCHY,
        PUNCHY_SUPPRESSED,
        EPICFIGHT
    }

    private static State previousState = State.PUNCHY;
    private static State currentState = State.PUNCHY;
    private static boolean bridgeActive = false;
    private static boolean transitionInProgress = false;

    public static State computeState() {
        if (!Config.PUNCHY_IDLE_OVERRIDE.get()) {
            bridgeActive = false;
            currentState = State.PUNCHY;
            return currentState;
        }

        LocalPlayerPatch patch = EpicFightCapabilities.getCachedLocalPlayerPatch();
        if (patch == null || !patch.isEpicFightMode()) {
            bridgeActive = false;
            currentState = State.PUNCHY;
            return currentState;
        }

        bridgeActive = true;

        if (!CombatHelper.isEpicFightIdle(patch)) {
            currentState = State.EPICFIGHT;
        } else if (!CombatHelper.isOffhandValidForWeapon(patch)) {
            currentState = State.PUNCHY_SUPPRESSED;
        } else {
            currentState = State.PUNCHY;
        }

        return currentState;
    }

    public static boolean shouldCancelEpicFightRender() {
        return bridgeActive && currentState != State.EPICFIGHT;
    }

    public static boolean shouldCancelPunchyRender() {
        return bridgeActive && currentState != State.PUNCHY;
    }

    public static State getPreviousState() {
        return previousState;
    }

    public static void setPreviousState(State state) {
        previousState = state;
    }

    public static boolean isTransitionInProgress() {
        return transitionInProgress;
    }

    public static void setTransitionInProgress(boolean value) {
        transitionInProgress = value;
    }
}
