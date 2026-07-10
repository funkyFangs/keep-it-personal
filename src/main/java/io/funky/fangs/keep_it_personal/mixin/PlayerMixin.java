package io.funky.fangs.keep_it_personal.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static io.funky.fangs.keep_it_personal.utility.InventoryUtilities.*;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    public PlayerMixin(final Level level, final GameProfile ignoredGameProfile) {
        super(EntityTypes.PLAYER, level);
    }

    @WrapMethod(method = "isAlwaysExperienceDropper")
    public boolean wrapIsAlwaysExperienceDropper(Operation<Boolean> original) {
        return this instanceof DeathPreferenceContainer container
                ? !container.hasDeathPreference(DeathPreference.EXPERIENCE)
                : original.call();
    }

    @WrapWithCondition(
            method = "dropEquipment",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;destroyVanishingCursedItems()V")
    )
    public boolean shouldDestroyVanishingCurseItems(Player player) {
        return !(player instanceof DeathPreferenceContainer container
                && container.hasDeathPreference(DeathPreference.CURSED));
    }

    @WrapOperation(
            method = "dropEquipment",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V")
    )
    public void dropInventoryBasedOnPreferences(Inventory inventory, Operation<Void> original) {
        if (this instanceof DeathPreferenceContainer container) {
            final var deathPreferences = container.getDeathPreferences();
            final var itemPredicates = getItemPredicates(deathPreferences);

            // If there are no death preferences affecting which items are dropped, fall back to original method
            if (!itemPredicates.isEmpty()) {
                for (int i = 0; i < inventory.getContainerSize(); i += 1) {
                    final int slotId = i;
                    final var itemStack = inventory.getItem(slotId);

                    if (!itemStack.isEmpty() && itemPredicates.stream().noneMatch(predicate -> predicate.test(itemStack, slotId))) {
                        drop(itemStack, true, false);
                        inventory.removeItem(itemStack);
                    }
                }
                return;
            }
        }

        original.call(inventory);
    }
}
