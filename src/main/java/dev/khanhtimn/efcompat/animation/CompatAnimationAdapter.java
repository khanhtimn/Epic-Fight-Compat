package dev.khanhtimn.efcompat.animation;

public interface CompatAnimationAdapter {

    /**
     * Called once per client tick after the engine resolves the current motion ownership.
     * Implementations update their suppression state here.
     */
    void onMotionResolved(MotionOwner owner, MotionContext ctx);

    /**
     * Called when the player leaves Epic Fight mode or the integration is disabled.
     * Implementations must release all suppression and restore their default state.
     */
    void onEpicFightDisabled();
}
