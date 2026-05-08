package dev.khanhtimn.efcompat.mixins.epicfight.client;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.asset.AssetAccessor;

import java.util.Map;

@IfModLoaded("epicfight")
@Mixin(value = ClientAnimator.class, remap = false)
public interface ClientAnimatorAccessor {

    @Accessor("compositeLivingAnimations")
    Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> getCompositeLivingAnimations();

    @Accessor("defaultLivingAnimations")
    Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> getDefaultLivingAnimations();
}
