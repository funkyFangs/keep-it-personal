package io.funky.fangs.keep_it_personal.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static io.funky.fangs.keep_it_personal.utility.InventoryUtilities.*;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    public PlayerEntityMixin(World world, GameProfile ignoredProfile) {
        super(EntityType.PLAYER, world);
    }

    @WrapWithCondition(
            method = "dropInventory",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;vanishCursedItems()V")
    )
    public boolean shouldVanishCursedItems(PlayerEntity instance) {
        return !(instance instanceof DeathPreferenceContainer container
                && container.hasDeathPreference(DeathPreference.CURSED));
    }

    @WrapOperation(
            method = "dropInventory",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;dropAll()V")
    )
    public void dropInventoryBasedOnPreferences(PlayerInventory inventory, Operation<Void> original) {
        if (this instanceof DeathPreferenceContainer container) {
            final var deathPreferences = container.getDeathPreferences();
            final var itemPredicates = getItemPredicates(deathPreferences);

            // If there are no death preferences affecting which items are dropped, fall back to original method
            if (!itemPredicates.isEmpty()) {
                for (int i = 0; i < inventory.size(); i += 1) {
                    final int slotId = i;
                    final var itemStack = inventory.getStack(slotId);

                    if (!itemStack.isEmpty() && itemPredicates.stream().noneMatch(predicate -> predicate.test(itemStack, slotId))) {
                        inventory.removeStack(slotId);
                        dropItem(itemStack, true, false);
                    }
                }
                return;
            }
        }

        original.call(inventory);
    }
}
