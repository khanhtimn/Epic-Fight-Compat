package dev.khanhtimn.efcompat.mixins.punchy;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.khanhtimn.efcompat.compat.punchy.AnimationBridge;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import punchy.client.render.PunchyArmRenderer;

@IfModLoaded("punchy")
@Mixin(value = PunchyArmRenderer.class, remap = false)
public abstract class PunchyArmRendererMixin {

    @Inject(method = "renderFirstPerson", at = @At("HEAD"), cancellable = true)
    private static void efcompat$suppressDuringEpicFightCombat(
            ItemInHandRenderer handRenderer, LocalPlayer player,
            float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int combinedLight,
            CallbackInfo ci) {

        if (AnimationBridge.shouldCancelPunchyRender()) {
            ci.cancel();
        }
    }
}
