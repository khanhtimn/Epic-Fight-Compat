package dev.khanhtimn.efcompat.mixins.epicfight.client.events.engine;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.khanhtimn.efcompat.CombatHelper;
import dev.khanhtimn.efcompat.Config;
import dev.khanhtimn.efcompat.compat.punchy.AnimationBridge;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;

@Mixin(RenderEngine.class)
public abstract class RenderEngineMixin {

    @Inject(method = "epicfight$renderHand", at = @At("HEAD"), cancellable = true, remap = false)
    private void efcompat$filterFirstPersonRender(RenderHandEvent event, CallbackInfo ci) {
        if (!Config.VANILLA_FIRST_PERSON_FOR_NON_COMBAT.get()) return;

        var playerPatch = EpicFightCapabilities.getCachedLocalPlayerPatch();
        if (playerPatch == null || !playerPatch.isEpicFightMode()) return;
        if (playerPatch.getCurrentLivingMotion() == LivingMotions.INACTION) return;

        if (!CombatHelper.isCombatItem(
                playerPatch.getValidItemInHand(InteractionHand.MAIN_HAND))) {
            ci.cancel();
        }
    }

    @IfModLoaded("punchy")
    @Inject(method = "epicfight$renderHand", at = @At("HEAD"), cancellable = true, remap = false)
    private void efcompat$letPunchyRenderIdle(RenderHandEvent event, CallbackInfo ci) {
        if (AnimationBridge.shouldCancelEpicFightRender()) {
            ci.cancel();
        }
    }
}