package io.funky.fangs.keep_it_personal.mixin;

import com.mojang.authlib.GameProfile;
import io.funky.fangs.keep_it_personal.configuration.KeepItPersonalConfiguration;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import jakarta.annotation.Nonnull;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static io.funky.fangs.keep_it_personal.utility.InventoryUtilities.getItemPredicates;
import static java.util.Collections.unmodifiableSet;
import static java.util.function.Predicate.not;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements DeathPreferenceContainer {
    @Unique
    private static final String DEATH_PREFERENCES_KEY = "death_preferences";

    @Unique
    private static EnumSet<DeathPreference> getInitialDeathPreferences() {
        final var enabled = KeepItPersonalConfiguration.getInstance().preferences().enabled();
        return enabled.isEmpty() ? EnumSet.noneOf(DeathPreference.class) : EnumSet.copyOf(enabled);
    }

    public ServerPlayerMixin(final MinecraftServer ignoredServer, final ServerLevel level, final GameProfile gameProfile, final ClientInformation ignoredClientInformation) {
        super(level, gameProfile);
    }

    @Shadow
    public abstract @NonNull ServerLevel level();

    @Unique
    public final EnumSet<DeathPreference> deathPreferences = getInitialDeathPreferences();

    @Inject(method = "restoreFrom", at = @At("HEAD"))
    public void beforeRestoreFrom(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        if (oldPlayer instanceof DeathPreferenceContainer container) {
            clearDeathPreferences();
            deathPreferences.addAll(container.getDeathPreferences());
        }
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    public void afterRestoreFrom(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        if (shouldDropInventory()) {
            final var currentInventory = getInventory();
            final var oldInventory = oldPlayer.getInventory();
            final var itemPredicates = getItemPredicates(deathPreferences);

            if (!itemPredicates.isEmpty()) {
                final var matchPredicate = itemPredicates.stream().reduce(BiPredicate::or).get();

                for (int slotId = 0; slotId < oldInventory.getContainerSize(); slotId += 1) {
                    final var itemStack = oldInventory.getItem(slotId);

                    if (matchPredicate.test(itemStack, slotId) && !itemStack.isEmpty()) {
                        currentInventory.setItem(slotId, itemStack);
                    }
                }

                if (deathPreferences.contains(DeathPreference.HOTBAR)) {
                    currentInventory.setSelectedSlot(oldInventory.getSelectedSlot());
                }
            }

            if (hasDeathPreference(DeathPreference.EXPERIENCE)) {
                experienceProgress = oldPlayer.experienceProgress;
                experienceLevel = oldPlayer.experienceLevel;
                totalExperience = oldPlayer.totalExperience;
            }
        }
    }

    /**
     * @return true if {@link GameRules#KEEP_INVENTORY} is enabled or the player is in {@link GameType#SPECTATOR} mode
     */
    @Unique
    private boolean shouldDropInventory() {
        return !(level().getGameRules().get(GameRules.KEEP_INVENTORY) || isSpectator());
    }

    /**
     * Reads the {@link #deathPreferences} from the {@link ValueInput}
     */
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    protected void afterReadAdditionalSaveData(final ValueInput input, CallbackInfo callbackInfo) {
        input.getIntArray(DEATH_PREFERENCES_KEY)
                .ifPresent(ordinals -> {
                    deathPreferences.clear();

                    final var preferences = KeepItPersonalConfiguration.getInstance().preferences();
                    final var enabled = preferences.enabled();
                    final var disabled = preferences.disabled();

                    Stream.concat(
                            Arrays.stream(ordinals)
                                    .mapToObj(DeathPreference::fromOrdinal)
                                    .filter(not(disabled::contains)),
                                    enabled.stream()
                            )
                            .distinct()
                            .forEach(deathPreferences::add);
                });
    }

    /**
     * Writes the {@link #deathPreferences} to the {@link ValueOutput}
     */
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void afterAddAdditionalSaveData(ValueOutput output, CallbackInfo callbackInfo) {
        final var preferences = KeepItPersonalConfiguration.getInstance().preferences();
        final var enabled = preferences.enabled();
        final var disabled = preferences.disabled();

        final var ordinals = Stream.concat(
                deathPreferences.stream().filter(not(disabled::contains)),
                enabled.stream()
        )
                .distinct()
                .mapToInt(DeathPreference::ordinal)
                .toArray();

        output.putIntArray(DEATH_PREFERENCES_KEY, ordinals);
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
