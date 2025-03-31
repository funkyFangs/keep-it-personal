package io.funky.fangs.keep_it_personal.mixin;

import com.mojang.authlib.GameProfile;
import io.funky.fangs.keep_it_personal.configuration.KeepItPersonalConfiguration;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import jakarta.annotation.Nonnull;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements DeathPreferenceContainer {
    @Unique
    private static final String DEATH_PREFERENCES_KEY = "death_preferences";

    @Unique
    private static EnumSet<DeathPreference> getInitialDeathPreferences() {
        final var enabled = KeepItPersonalConfiguration.getInstance().preferences().enabled();
        return enabled.isEmpty() ? EnumSet.noneOf(DeathPreference.class) : EnumSet.copyOf(enabled);
    }

    private ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Shadow
    @Nullable
    public abstract ItemEntity dropItem(ItemStack stack, boolean throwRandomly, boolean retainOwnership);

    @Shadow
    public abstract ServerWorld getServerWorld();

    @Shadow
    public abstract boolean damage(ServerWorld world, DamageSource source, float amount);

    @Unique
    public final EnumSet<DeathPreference> deathPreferences = getInitialDeathPreferences();

    /**
     * This method overrides the default behavior for dropping the {@link ServerPlayerEntity}'s {@link PlayerInventory}.
     * Items in the inventory without {@link Enchantments#VANISHING_CURSE} are dropped based on the player's selected
     * {@link DeathPreference}s. This method will not modify the inventory if {@link GameRules#KEEP_INVENTORY} is
     * enabled.
     */
    @Override
    public void dropInventory(ServerWorld world) {
        if (!world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
            if (!deathPreferences.contains(DeathPreference.CURSED)) {
                this.vanishCursedItems();
            }

            final var inventory = getInventory();

            final Stream.Builder<ItemStack> itemsToDrop = Stream.builder();

            if (!deathPreferences.contains(DeathPreference.ARMOR)) {
                inventory.armor.forEach(itemsToDrop::add);
                inventory.armor.clear();
            }

            if (!deathPreferences.contains(DeathPreference.OFFHAND)) {
                inventory.offHand.forEach(itemsToDrop::add);
                inventory.offHand.clear();
            }

            if (!deathPreferences.contains(DeathPreference.HOTBAR)) {
                for (int i = 0; i < PlayerInventory.getHotbarSize(); ++i) {
                    final var itemStack = inventory.getStack(i);
                    itemsToDrop.add(itemStack);
                    inventory.setStack(i, ItemStack.EMPTY);
                }
            }

            if (!deathPreferences.contains(DeathPreference.INVENTORY)) {
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
        if (oldPlayer instanceof DeathPreferenceContainer container) {
            deathPreferences.addAll(container.getDeathPreferences());
        }

        if (!alive && shouldDropInventory()) {
            if (deathPreferences.contains(DeathPreference.EXPERIENCE)) {
                experienceLevel = oldPlayer.experienceLevel;
                totalExperience = oldPlayer.totalExperience;
                experienceProgress = oldPlayer.experienceProgress;
            }

            final var oldInventory = oldPlayer.getInventory();
            final var inventory = getInventory();

            if (deathPreferences.contains(DeathPreference.OFFHAND)) {
                for (int i = 0; i < inventory.offHand.size(); ++i) {
                    inventory.offHand.set(i, oldInventory.offHand.get(i));
                }
            }

            if (deathPreferences.contains(DeathPreference.ARMOR)) {
                for (int i = 0; i < inventory.armor.size(); ++i) {
                    inventory.armor.set(i, oldInventory.armor.get(i));
                }
            }

            if (deathPreferences.contains(DeathPreference.HOTBAR)) {
                for (int i = 0; i < PlayerInventory.HOTBAR_SIZE; ++i) {
                    inventory.main.set(i, oldInventory.main.get(i));
                }
                inventory.setSelectedSlot(oldInventory.selectedSlot);
            }

            if (deathPreferences.contains(DeathPreference.INVENTORY)) {
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
        return deathPreferences.contains(DeathPreference.EXPERIENCE) || super.isExperienceDroppingDisabled();
    }

    /**
     * @return true if {@link GameRules#KEEP_INVENTORY} is enabled or the player is in {@link GameMode#SPECTATOR} mode
     */
    @Unique
    private boolean shouldDropInventory() {
        return !(getServerWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || isSpectator());
    }

    /**
     * Reads the {@link #deathPreferences} from the {@link NbtCompound}
     */
    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    public void afterReadCustomDataFromNbt(NbtCompound nbt, CallbackInfo callbackInfo) {
        if (nbt.contains(DEATH_PREFERENCES_KEY, NbtElement.INT_ARRAY_TYPE)) {
            deathPreferences.clear();

            final var preferences = KeepItPersonalConfiguration.getInstance().preferences();
            final var enabled = preferences.enabled();
            final var disabled = preferences.disabled();

            Stream.concat(
                    Arrays.stream(nbt.getIntArray(DEATH_PREFERENCES_KEY))
                            .mapToObj(DeathPreference::fromOrdinal)
                            .filter(not(disabled::contains)),
                    enabled.stream()
            )
                    .distinct()
                    .forEach(deathPreferences::add);
        }
    }

    /**
     * Writes the {@link #deathPreferences} to the {@link NbtCompound}
     */
    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    public void afterWriteCustomDataToNbt(NbtCompound nbt, CallbackInfo callbackInfo) {
        final var preferences = KeepItPersonalConfiguration.getInstance().preferences();
        final var enabled = preferences.enabled();
        final var disabled = preferences.disabled();

        final var preferenceOrdinals = Stream.concat(
                deathPreferences.stream().filter(not(disabled::contains)),
                enabled.stream()
        )
                .distinct()
                .mapToInt(DeathPreference::ordinal)
                .toArray();

        nbt.putIntArray(DEATH_PREFERENCES_KEY, preferenceOrdinals);
    }

    @Nonnull
    @Override
    public Set<DeathPreference> getDeathPreferences() {
        return unmodifiableSet(deathPreferences);
    }

    @Override
    public boolean hasDeathPreference(@Nonnull DeathPreference deathPreference) {
        return this.deathPreferences.contains(deathPreference);
    }

    @Override
    public boolean addDeathPreference(@Nonnull DeathPreference deathPreference) {
        return !KeepItPersonalConfiguration.getInstance().preferences().disabled().contains(deathPreference)
                && this.deathPreferences.add(deathPreference);
    }

    @Override
    public boolean removeDeathPreference(@Nonnull DeathPreference deathPreference) {
        return !KeepItPersonalConfiguration.getInstance().preferences().enabled().contains(deathPreference)
                && deathPreferences.remove(deathPreference);
    }

    @Override
    public void clearDeathPreferences() {
        deathPreferences.clear();
        deathPreferences.addAll(KeepItPersonalConfiguration.getInstance().preferences().enabled());
    }

    @Override
    public void fillDeathPreferences() {
        deathPreferences.addAll(EnumSet.allOf(DeathPreference.class));
        deathPreferences.removeAll(KeepItPersonalConfiguration.getInstance().preferences().disabled());
    }
}
