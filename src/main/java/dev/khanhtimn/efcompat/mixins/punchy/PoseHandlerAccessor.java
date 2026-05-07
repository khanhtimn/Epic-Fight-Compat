package dev.khanhtimn.efcompat.mixins.punchy;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import punchy.client.animation.PoseHandler;

@IfModLoaded("punchy")
@Mixin(value = PoseHandler.class, remap = false)
public interface PoseHandlerAccessor {

    @Invoker("beginBlendOut")
    void invokeBeginBlendOut();
}
