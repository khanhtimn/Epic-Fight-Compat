package dev.khanhtimn.efcompat.mixins.punchy;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.khanhtimn.efcompat.compat.punchy.PunchyAnimationBridge;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.render.PunchyArmRenderer;

@IfModLoaded("punchy")
@Mixin(value = PunchyArmRenderer.class, remap = false)
public abstract class PunchyArmRendererMixin {

    @WrapMethod(method = "renderFirstPerson")
    private static void efcompat$wrapRender(
            ItemInHandRenderer handRenderer, LocalPlayer player,
            float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int combinedLight,
            Operation<Void> original) {

        PunchyAnimationBridge bridge = PunchyAnimationBridge.INSTANCE;

        if (bridge.shouldCancelPunchyRender()) return;

        if (bridge.shouldSuppressSwimHandler()) {
            PunchyAnimationManager.SWIM_HANDLER.stopImmediateWithBlendOut();
        }

        original.call(handRenderer, player, partialTicks, poseStack, buffer, combinedLight);
    }
}
