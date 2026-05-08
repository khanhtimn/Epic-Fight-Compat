package dev.khanhtimn.efcompat.animation;

import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;

public final class AnimationPriorityEngine {

    private AnimationPriorityEngine() {}

    private static final List<CompatAnimationAdapter> ADAPTERS = new ArrayList<>();

    private static final EnumSet<LivingMotions> ACTIVE_COMBAT_MOTIONS = EnumSet.of(
            LivingMotions.INACTION, LivingMotions.DIGGING,
            LivingMotions.EAT,      LivingMotions.DRINK,
            LivingMotions.AIM,      LivingMotions.BLOCK,
            LivingMotions.BLOCK_SHIELD, LivingMotions.RELOAD,
            LivingMotions.SHOT,     LivingMotions.SPELLCAST
    );

    private static final EnumMap<LivingMotions, MotionOwner> OWNERSHIP_TABLE = buildTable();


    private static EnumMap<LivingMotions, MotionOwner> buildTable() {
        EnumMap<LivingMotions, MotionOwner> t = new EnumMap<>(LivingMotions.class);

        t.put(LivingMotions.IDLE,             MotionOwner.EPIC_FIGHT_IF_TWO_HANDED);
        t.put(LivingMotions.WALK,             MotionOwner.EPIC_FIGHT_IF_ARMED);
        t.put(LivingMotions.RUN,              MotionOwner.EPIC_FIGHT_IF_ARMED);
        t.put(LivingMotions.SNEAK,            MotionOwner.EPIC_FIGHT_IF_ARMED);
        t.put(LivingMotions.CHASE,            MotionOwner.EPIC_FIGHT_IF_ARMED);
        t.put(LivingMotions.JUMP,             MotionOwner.SHARED);
        t.put(LivingMotions.FALL,             MotionOwner.EPIC_FIGHT_IF_TWO_HANDED);
        t.put(LivingMotions.FLOAT,            MotionOwner.EPIC_FIGHT_IF_TWO_HANDED);
        t.put(LivingMotions.SWIM,             MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.FLY,             MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.CREATIVE_FLY,     MotionOwner.SHARED);
        t.put(LivingMotions.CREATIVE_IDLE,    MotionOwner.EPIC_FIGHT_IF_TWO_HANDED);
        t.put(LivingMotions.LANDING_RECOVERY, MotionOwner.SHARED);
        t.put(LivingMotions.KNEEL,            MotionOwner.EPIC_FIGHT_IF_TWO_HANDED);
        t.put(LivingMotions.CLIMB,            MotionOwner.EPIC_FIGHT_IF_TWO_HANDED);
        t.put(LivingMotions.MOUNT,            MotionOwner.EPIC_FIGHT_IF_TWO_HANDED);

        t.put(LivingMotions.CONFRONT,  MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.ANGRY,     MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.SIT,       MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.DEATH,     MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.SLEEP,     MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.CELEBRATE, MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.ADMIRE,    MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.SPECTATE,  MotionOwner.EPIC_FIGHT);

        for (LivingMotions m : ACTIVE_COMBAT_MOTIONS) {
            t.put(m, MotionOwner.EPIC_FIGHT);
        }

        t.put(LivingMotions.ALL,  MotionOwner.EPIC_FIGHT);
        t.put(LivingMotions.NONE, MotionOwner.COMPAT);

        return t;
    }

    /**
     * Registers a compat adapter to receive motion ownership updates.
     * Call once at client setup; adapters are never unregistered.
     */
    public static void registerAdapter(CompatAnimationAdapter adapter) {
        ADAPTERS.add(adapter);
    }

    /**
     * Resolves which system owns the given motion snapshot and notifies all registered adapters.
     * POV settings and active composite combat motions always return {@link MotionOwner#EPIC_FIGHT}.
     * Custom (non-enum) {@link LivingMotion} implementations default to {@link MotionOwner#COMPAT}.
     */
    public static MotionOwner resolve(MotionContext ctx) {
        if (ctx.hasPovSettings()) return MotionOwner.EPIC_FIGHT;

        if (ctx.compositeMotion() instanceof LivingMotions cm && ACTIVE_COMBAT_MOTIONS.contains(cm)) {
            return MotionOwner.EPIC_FIGHT;
        }

        if (!(ctx.baseMotion() instanceof LivingMotions base)) {
            return MotionOwner.COMPAT;
        }

        MotionOwner owner = OWNERSHIP_TABLE.getOrDefault(base, MotionOwner.COMPAT);

        if (owner == MotionOwner.EPIC_FIGHT_IF_ARMED) {
            owner = ctx.hasWeaponPose() ? MotionOwner.EPIC_FIGHT : MotionOwner.COMPAT;
        } else if (owner == MotionOwner.EPIC_FIGHT_IF_TWO_HANDED) {
            owner = ctx.isTwoHanded() ? MotionOwner.EPIC_FIGHT : MotionOwner.COMPAT;
        }

        return owner;
    }

    /**
     * Resolves and dispatches to all registered adapters in one call.
     * Used by the tick entry point in {@code EFCompatClient}.
     */
    public static MotionOwner resolveAndDispatch(MotionContext ctx) {
        MotionOwner owner = resolve(ctx);
        for (CompatAnimationAdapter adapter : ADAPTERS) {
            adapter.onMotionResolved(owner, ctx);
        }
        return owner;
    }

    public static void dispatchDisabled() {
        for (CompatAnimationAdapter adapter : ADAPTERS) {
            adapter.onEpicFightDisabled();
        }
    }

    public static boolean isActiveCombatMotion(LivingMotion motion) {
        return motion instanceof LivingMotions m && ACTIVE_COMBAT_MOTIONS.contains(m);
    }
}
