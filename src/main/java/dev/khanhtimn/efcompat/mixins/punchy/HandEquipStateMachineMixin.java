package dev.khanhtimn.efcompat.mixins.punchy;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import dev.khanhtimn.efcompat.compat.punchy.PunchyAnimationBridge;
import dev.khanhtimn.efcompat.compat.punchy.PunchyCompatState;
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

    @Shadow private ItemStack copyStack(ItemStack stack) { throw new AssertionError(); }

    @WrapMethod(method = "tick")
    private void efcompat$wrapTick(Minecraft client, Operation<Void> original) {
        if (client == null || client.player == null) {
            original.call(client);
            return;
        }

        PunchyAnimationBridge bridge = PunchyAnimationBridge.INSTANCE;
        PunchyCompatState current  = bridge.computeState();
        PunchyCompatState previous = bridge.getPreviousState();
        boolean stateChanged       = current != previous;

        switch (current) {
            case EPICFIGHT -> {
                if (stateChanged) {
                    ((PoseHandlerAccessor) (Object) PunchyAnimationManager.POSE_HANDLER).invokeBeginBlendOut();
                    ((PoseHandlerAccessor) (Object) PunchyAnimationManager.SWIM_HANDLER).invokeBeginBlendOut();
                    InspectActionTracker.clear();
                }
                // EF owns this tick — do not call original.
            }
            case PUNCHY_SUPPRESSED -> {
                // Keep Punchy's item state current so it never sees stale items on handoff.
                this.renderedMain = copyStack(client.player.getMainHandItem());
                this.renderedOff  = copyStack(client.player.getOffhandItem());
            }
            case SHARED -> original.call(client);
            case PUNCHY -> {
                if (stateChanged && (previous == PunchyCompatState.EPICFIGHT
                        || previous == PunchyCompatState.PUNCHY_SUPPRESSED)) {
                    this.renderedMain = ItemStack.EMPTY;
                    this.renderedOff  = ItemStack.EMPTY;

                    if (bridge.wasLastEpicFightTwoHanded()) {
                        // Two-handed weapons don't render visible first-person hands in EF.
                        // Scoping raiseOnlyHandoff=true around this call makes
                        // wasItemBlacklisted(EMPTY) return true, routing through
                        // handleBlacklistToPunchyTransition → startRaising() directly,
                        // with no lower phase. Counter is NOT zeroed; Punchy's 4-tick
                        // countdown runs to completion and fires a single raise.
                        bridge.beginRaiseOnlyHandoff();
                        original.call(client);
                        bridge.endRaiseOnlyHandoff();
                    } else {
                        // Armed one-handed EPICFIGHT → Punchy: wasItemBlacklisted(EMPTY)=false,
                        // handleIdle detects mainChanged → startLowering → raise (standard swap).
                        original.call(client);
                    }
                } else {
                    original.call(client);
                }
            }
        }
    }

    // Routes two-handed handoff through Punchy's raise-only blacklist transition.
    @WrapMethod(method = "wasItemBlacklisted")
    private boolean efcompat$raiseOnlyBlacklist(ItemStack stack, Operation<Boolean> original) {
        if (PunchyAnimationBridge.INSTANCE.isRaiseOnlyHandoff()) return true;
        return original.call(stack);
    }
}
