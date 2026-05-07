package dev.khanhtimn.efcompat.mixins.punchy;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.khanhtimn.efcompat.compat.punchy.AnimationBridge;
import dev.khanhtimn.efcompat.compat.punchy.AnimationBridge.BridgeState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import punchy.client.animation.PunchyAnimationManager;
import punchy.client.state.HandEquipStateMachine;
import punchy.client.state.InspectActionTracker;

@IfModLoaded("punchy")
@Mixin(value = HandEquipStateMachine.class, remap = false)
public abstract class HandEquipStateMachineMixin {

    @Shadow private ItemStack renderedMain;
    @Shadow private ItemStack renderedOff;
    @Shadow private static boolean syncRequested;
    @Shadow private static volatile int vanillaHandOutMainTicks;
    @Shadow private static volatile int vanillaHandOutOffTicks;

    @Shadow private ItemStack copyStack(ItemStack stack) { throw new AssertionError(); }

    @WrapMethod(method = "tick")
    private void efcompat$wrapTick(Minecraft client, Operation<Void> original) {
        if (client == null || client.player == null) {
            original.call(client);
            return;
        }

        BridgeState current = AnimationBridge.computeState();
        BridgeState previous = AnimationBridge.getPreviousState();
        boolean stateChanged = current != previous;

        if (stateChanged) {
            AnimationBridge.setPreviousState(current);
        }

        switch (current) {
            case EPICFIGHT -> {
                if (stateChanged) {
                    ((PoseHandlerAccessor) (Object) PunchyAnimationManager.POSE_HANDLER).invokeBeginBlendOut();
                    ((PoseHandlerAccessor) (Object) PunchyAnimationManager.SWIM_HANDLER).invokeBeginBlendOut();
                    InspectActionTracker.clear();
                }
            }
            case PUNCHY_SUPPRESSED -> {
                this.renderedMain = copyStack(client.player.getMainHandItem());
                this.renderedOff = copyStack(client.player.getOffhandItem());
            }
            case PUNCHY -> {
                if (stateChanged && (previous == BridgeState.EPICFIGHT || previous == BridgeState.PUNCHY_SUPPRESSED)) {
                    syncRequested = false;
                    this.renderedMain = ItemStack.EMPTY;
                    this.renderedOff = ItemStack.EMPTY;
                    AnimationBridge.setTransitionInProgress(true);
                    original.call(client);
                    AnimationBridge.setTransitionInProgress(false);
                    vanillaHandOutMainTicks = 0;
                    vanillaHandOutOffTicks = 0;
                } else {
                    original.call(client);
                }
            }
        }
    }

    @WrapMethod(method = "wasItemBlacklisted")
    private boolean efcompat$treatSuppressedAsBlacklisted(ItemStack stack, Operation<Boolean> original) {
        if (AnimationBridge.isTransitionInProgress()) {
            return true;
        }
        return original.call(stack);
    }
}
