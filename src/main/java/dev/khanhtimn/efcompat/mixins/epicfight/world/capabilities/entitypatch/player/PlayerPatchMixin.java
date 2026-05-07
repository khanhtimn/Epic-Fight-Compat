package dev.khanhtimn.efcompat.mixins.epicfight.world.capabilities.entitypatch.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.khanhtimn.efcompat.CombatHelper;
import dev.khanhtimn.efcompat.Config;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch.PlayerMode;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@Mixin(PlayerPatch.class)
public abstract class PlayerPatchMixin {

    @Shadow(remap = false)
    protected PlayerMode playerMode;

    @Shadow(remap = false)
    public abstract void toEpicFightMode(boolean synchronize);

    @Shadow(remap = false)
    public abstract void toVanillaMode(boolean synchronize);

    @WrapMethod(method = "toggleMode", remap = false)
    private void efcompat$enforceToggleMode(Operation<Void> original) {
        PlayerPatch<?> self = (PlayerPatch<?>) (Object) this;
        Player player = self.getOriginal();

        if (!Config.ENFORCE_MODE_ON_TOGGLE.get()
                || !EpicFightGameRules.CAN_SWITCH_PLAYER_MODE.getRuleValue(player.level())) {
            original.call();
            return;
        }

        boolean isCombat = CombatHelper.isCombatItem(player.getMainHandItem());

        if (isCombat && this.playerMode == PlayerMode.VANILLA) {
            this.toEpicFightMode(true);
        } else if (!isCombat && this.playerMode == PlayerMode.EPICFIGHT) {
            this.toVanillaMode(true);
        }
    }
}