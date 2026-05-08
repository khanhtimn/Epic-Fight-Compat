package dev.khanhtimn.efcompat.animation;

import yesman.epicfight.api.animation.LivingMotion;

public record MotionContext(
        LivingMotion baseMotion,
        LivingMotion compositeMotion,
        boolean hasPovSettings,
        boolean hasWeaponPose,
        boolean isTwoHanded
) {}

