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
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;

import static io.funky.fangs.keep_it_personal.utility.InventoryUtilities.ARMOR_SLOTS;
import static io.funky.fangs.keep_it_personal.utility.InventoryUtilities.hasCurseOfVanishing;
import static net.minecraft.entity.player.PlayerInventory.MAIN_SIZE;
import static net.minecraft.entity.player.PlayerInventory.OFF_HAND_SLOT;

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
            final Stream.Builder<ItemStack> itemsToDrop = Stream.builder();
            final boolean keepCursedItems = container.hasDeathPreference(DeathPreference.CURSED);

            if (!container.hasDeathPreference(DeathPreference.ARMOR)) {
                for (var slot : ARMOR_SLOTS) {
                    final var slotId = slot.getOffsetEntitySlotId(MAIN_SIZE);
                    final var itemStack = inventory.getStack(slotId);

                    if (!(itemStack.isEmpty() || keepCursedItems && hasCurseOfVanishing(itemStack))) {
                        itemsToDrop.add(inventory.getStack(slotId));
                        inventory.removeStack(slotId);
                    }
                }
            }

            if (!container.hasDeathPreference(DeathPreference.OFFHAND)) {
                final var itemStack = inventory.getStack(OFF_HAND_SLOT);
                if (!(itemStack.isEmpty() || keepCursedItems && hasCurseOfVanishing(itemStack))) {
                    itemsToDrop.add(itemStack);
                    inventory.removeStack(OFF_HAND_SLOT);
                }
            }

            if (!container.hasDeathPreference(DeathPreference.HOTBAR)) {
                for (int i = 0; i < PlayerInventory.getHotbarSize(); ++i) {
                    final var itemStack = inventory.getStack(i);
                    if (!(itemStack.isEmpty() || keepCursedItems && hasCurseOfVanishing(itemStack))) {
                        itemsToDrop.add(itemStack);
                        inventory.removeStack(i);
                    }
                }
            }

            if (!container.hasDeathPreference(DeathPreference.INVENTORY)) {
                for (int i = PlayerInventory.HOTBAR_SIZE; i < MAIN_SIZE; ++i) {
                    final var itemStack = inventory.getStack(i);
                    if (!(itemStack.isEmpty() || keepCursedItems && hasCurseOfVanishing(itemStack))) {
                        itemsToDrop.add(itemStack);
                        inventory.removeStack(i);
                    }
                }
            }

            itemsToDrop.build().forEach(item -> dropItem(item, true, false));
        }
        else {
            original.call(inventory);
        }
    }
}
