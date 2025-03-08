package io.funky.fangs.keep_it_personal.mixin;

import com.mojang.authlib.GameProfile;
import io.funky.fangs.keep_it_personal.command.KeepCommandState;
import io.funky.fangs.keep_it_personal.command.DeathPreference;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow
    @Nullable
    public abstract ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership);

    @Shadow
    public abstract ServerWorld getServerWorld();

    /**
     * This method overrides the default behavior for dropping the {@link ServerPlayerEntity}'s {@link PlayerInventory}.
     * Items in the inventory without {@link Enchantments#VANISHING_CURSE} are dropped based on the player's selected
     * {@link DeathPreference}s. This method will not modify the inventory if {@link GameRules#KEEP_INVENTORY} is
     * enabled.
     */
    @Override
    public void dropInventory(ServerWorld world) {
        if (!world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            this.vanishCursedItems();

            final var preferences = getDeathPreferences();
            final var inventory = getInventory();

            final Stream.Builder<ItemStack> itemsToDrop = Stream.builder();

            if (!preferences.contains(DeathPreference.ARMOR)) {
                inventory.armor.forEach(itemsToDrop::add);
                inventory.armor.clear();
            }

            if (!preferences.contains(DeathPreference.OFFHAND)) {
                inventory.offHand.forEach(itemsToDrop::add);
                inventory.offHand.clear();
            }

            if (!preferences.contains(DeathPreference.HOTBAR)) {
                for (int i = 0; i < PlayerInventory.getHotbarSize(); ++i) {
                    final var itemStack = inventory.getStack(i);
                    itemsToDrop.add(itemStack);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }

            if (!preferences.contains(DeathPreference.INVENTORY)) {
                for (int i = PlayerInventory.HOTBAR_SIZE; i < inventory.main.size(); ++i) {
                    final var itemStack = inventory.getStack(i);
                    itemsToDrop.add(itemStack);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }

            itemsToDrop.build()
                    .filter(not(ItemStack::isEmpty))
                    .forEach(item -> dropItem(item, true, false));
        }
    }

    /**
     * This method copies over the inventory and experience from the old {@link ServerPlayerEntity} based on the
     * selected {@link DeathPreference}s.
     */
    @Inject(method = "copyFrom", at = @At("TAIL"))
    public void afterCopyFrom(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo callbackInfo) {
        if (!alive && shouldDropInventory()) {
            final var preferences = getDeathPreferences();

            if (preferences.contains(DeathPreference.EXPERIENCE)) {
                experienceLevel = oldPlayer.experienceLevel;
                totalExperience = oldPlayer.totalExperience;
                experienceProgress = oldPlayer.experienceProgress;
            }

            final var oldInventory = oldPlayer.getInventory();
            final var inventory = getInventory();

            if (preferences.contains(DeathPreference.OFFHAND)) {
                for (int i = 0; i < inventory.offHand.size(); ++i) {
                    inventory.offHand.set(i, oldInventory.offHand.get(i));
                }
            }

            if (preferences.contains(DeathPreference.ARMOR)) {
                for (int i = 0; i < inventory.armor.size(); ++i) {
                    inventory.armor.set(i, oldInventory.armor.get(i));
                }
            }

            if (preferences.contains(DeathPreference.HOTBAR)) {
                for (int i = 0; i < PlayerInventory.HOTBAR_SIZE; ++i) {
                    inventory.main.set(i, oldInventory.main.get(i));
                }
                inventory.setSelectedSlot(oldInventory.selectedSlot);
            }

            if (preferences.contains(DeathPreference.INVENTORY)) {
                for (int i = PlayerInventory.HOTBAR_SIZE; i < inventory.main.size(); ++i) {
                    inventory.main.set(i, oldInventory.main.get(i));
                }
            }
        }
    }

    /**
     * @return true if keeping {@link DeathPreference#EXPERIENCE} or if experience dropping was already disabled
     */
    @Override
    public boolean isExperienceDroppingDisabled() {
        return getDeathPreferences().contains(DeathPreference.EXPERIENCE) || super.isExperienceDroppingDisabled();
    }

    /**
     * @return an unmodifiable {@link Set} of the player's {@link DeathPreference}s.
     */
    @Unique
    public Set<DeathPreference> getDeathPreferences() {
        return KeepCommandState.Companion.getCurrentState(getWorld().getRegistryKey(), getServer())
                .getPlayerPreferences(uuid);
    }

    /**
     * @return true if {@link GameRules#KEEP_INVENTORY} is enabled or the player is in {@link GameMode#SPECTATOR} mode
     */
    @Unique
    private boolean shouldDropInventory() {
        return !(getServerWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || isSpectator());
    }
}
