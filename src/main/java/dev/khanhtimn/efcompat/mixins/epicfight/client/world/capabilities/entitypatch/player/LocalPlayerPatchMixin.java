package dev.khanhtimn.efcompat.mixins.epicfight.client.world.capabilities.entitypatch.player;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.khanhtimn.efcompat.CombatHelper;
import dev.khanhtimn.efcompat.Config;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.gamerule.EpicFightGameRules;

@Mixin(LocalPlayerPatch.class)
public abstract class LocalPlayerPatchMixin {

    @WrapMethod(method = "updateHeldItem", remap = false)
    private void efcompat$autoSwitchOnItemChange(CapabilityItem mainHandCap,
                                                  CapabilityItem offHandCap,
                                                  Operation<Void> original) {
        original.call(mainHandCap, offHandCap);

        if (!Config.AUTO_SWITCH_ENABLED.get()) return;

        PlayerPatch<?> self = (PlayerPatch<?>) (Object) this;
        Player player = self.getOriginal();

        if (!EpicFightGameRules.CAN_SWITCH_PLAYER_MODE.getRuleValue(player.level())) return;

        boolean isCombat = CombatHelper.isCombatItem(player.getMainHandItem());
        boolean wasEFMode = self.isEpicFightMode();

        if (isCombat && !wasEFMode) {
            self.toEpicFightMode(true);
        } else if (!isCombat && wasEFMode) {
            self.toVanillaMode(true);
        }
    }
}
