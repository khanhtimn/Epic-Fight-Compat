package dev.khanhtimn.efcompat.mixins.punchy;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.khanhtimn.efcompat.compat.punchy.PunchyAnimationBridge;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import punchy.client.state.MovementStateMachine;

@IfModLoaded("punchy")
@Mixin(value = MovementStateMachine.class, remap = false)
public abstract class MovementStateMachineMixin {

    @WrapMethod(method = "tick")
    private void efcompat$suppressDuringEpicFight(Minecraft client, Operation<Void> original) {
        if (PunchyAnimationBridge.INSTANCE.shouldSuppressMovementStateMachine()) return;
        original.call(client);
    }
}
