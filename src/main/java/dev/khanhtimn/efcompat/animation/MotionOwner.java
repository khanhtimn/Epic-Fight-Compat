package dev.khanhtimn.efcompat.animation;

public enum MotionOwner {
    /** EF is authoritative; all compat movement layers must yield. */
    EPIC_FIGHT,

    /** Compat mod provides the first-person feel; EF body continues in 3rd person. */
    COMPAT,

    /**
     * Both systems coexist. Neither fully suppresses the other.
     * Compat adapters may still apply targeted suppression within this state.
     */
    SHARED,

    /**
     * Resolved at runtime: EPIC_FIGHT if the current weapon has a non-default moveset
     * animation registered for this motion, COMPAT otherwise.
     * Never returned from {@link AnimationPriorityEngine#resolve}; always collapsed first.
     */
    EPIC_FIGHT_IF_ARMED,

    /**
     * Resolved at runtime: EPIC_FIGHT when the main-hand item is two-handed
     * ({@code cap.canBePlacedOffhand() == false}), COMPAT otherwise.
     * Never returned from {@link AnimationPriorityEngine#resolve}; always collapsed first.
     */
    EPIC_FIGHT_IF_TWO_HANDED
}

