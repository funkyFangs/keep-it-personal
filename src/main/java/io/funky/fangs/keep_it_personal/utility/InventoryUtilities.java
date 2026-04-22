package io.funky.fangs.keep_it_personal.utility;

import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import jakarta.annotation.Nonnull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.*;
import java.util.function.BiPredicate;

import static java.util.Collections.unmodifiableMap;
import static net.minecraft.world.entity.EquipmentSlot.*;
import static net.minecraft.world.entity.player.Inventory.*;

public final class InventoryUtilities {
    private InventoryUtilities() throws IllegalAccessException {
        throw new IllegalAccessException(getClass().getSimpleName() + " is a utility class and cannot be instantiated");
    }

    /**
     * This method is a {@link BiPredicate} for use in {@link #ITEM_PREDICATES} which determines if an item is in the
     * hotbar. Hotbar items have a slot ID in the interval [0, 9).
     * @param ignoredItemStack unused because only the slot ID is needed to make this determination
     * @param slotId the slot ID of the item
     * @return if the item is a hotbar item
     * @see #ITEM_PREDICATES
     */
    private static boolean isHotbarItem(ItemStack ignoredItemStack, Integer slotId) {
        return slotId >= 0 && slotId < SELECTION_SIZE;
    }

    /**
     * This method is a {@link BiPredicate} for use in {@link #ITEM_PREDICATES} which determines if an item is in the
     * inventory. Inventory items have a slot ID in the interval [9, 36).
     * @param ignoredItemStack unused because only the slot ID is needed to make this determination
     * @param slotId the slot ID of the item
     * @return if the item is an inventory item
     * @see #ITEM_PREDICATES
     */
    private static boolean isInventoryItem(ItemStack ignoredItemStack, Integer slotId) {
        return slotId >= SELECTION_SIZE && slotId < INVENTORY_SIZE;
    }

    /**
     * This method is a {@link BiPredicate} for use in {@link #ITEM_PREDICATES} which determines if an item is in the
     * armor. Armor items have a slot ID in the interval [36, 39].
     * @param ignoredItemStack unused because only the slot ID is needed to make this determination
     * @param slotId the slot ID of the item
     * @return if the item is an armor item
     * @see #ITEM_PREDICATES
     */
    private static boolean isArmorItem(ItemStack ignoredItemStack, Integer slotId) {
        return slotId >= FEET.getIndex(INVENTORY_SIZE) && slotId <= HEAD.getIndex(INVENTORY_SIZE);
    }

    /**
     * This method is a {@link BiPredicate} for use in {@link #ITEM_PREDICATES} which determines if an item is in the
     * offhand. Offhand items have a slot ID equal to 40.
     * @param ignoredItemStack unused because only the slot ID is needed to make this determination
     * @param slotId the slot ID of the item
     * @return if the item is an offhand item
     * @see #ITEM_PREDICATES
     */
    private static boolean isOffhandItem(ItemStack ignoredItemStack, Integer slotId) {
        return slotId == SLOT_OFFHAND;
    }

    /**
     * This method is a {@link BiPredicate} for use in {@link #ITEM_PREDICATES} which determines if an item is cursed.
     * Cursed items have the enchantment effect {@link Enchantments#VANISHING_CURSE}.
     * @param itemStack the {@link ItemStack} to check
     * @param ignoredSlotId unused because only the {@link ItemStack} is needed to make this determination
     * @return if the item is a cursed item
     * @see #ITEM_PREDICATES
     */
    private static boolean isCursedItem(ItemStack itemStack, Integer ignoredSlotId) {
        return itemStack.getEnchantments()
                .keySet()
                .stream()
                .anyMatch(enchantmentHolder -> enchantmentHolder.is(Enchantments.VANISHING_CURSE));
    }

    /**
     * This is an unmodifiable {@link EnumMap} from {@link DeathPreference}s to {@link BiPredicate}s. Each
     * {@link BiPredicate} matches {@link ItemStack}s and their slot IDs to the corresponding {@link DeathPreference}.
     * For example, {@link DeathPreference#OFFHAND} maps to {@link #isOffhandItem}.
     * @see #isHotbarItem
     * @see #isInventoryItem
     * @see #isArmorItem
     * @see #isOffhandItem
     * @see #isCursedItem
     * @see #getItemPredicates
     */
    private static final Map<DeathPreference, BiPredicate<ItemStack, Integer>> ITEM_PREDICATES = unmodifiableMap(new EnumMap<>(Map.of(
            DeathPreference.HOTBAR, InventoryUtilities::isHotbarItem,
            DeathPreference.INVENTORY, InventoryUtilities::isInventoryItem,
            DeathPreference.ARMOR, InventoryUtilities::isArmorItem,
            DeathPreference.OFFHAND, InventoryUtilities::isOffhandItem,
            DeathPreference.CURSED, InventoryUtilities::isCursedItem
    )));

    /**
     * This method creates a {@link Collection} of {@link BiPredicate}s which determine if a given {@link ItemStack}
     * and slot ID are affected by a respective {@link DeathPreference}.
     * @param deathPreferences the {@link DeathPreference}s to get {@link BiPredicate}s for
     * @return a {@link Collection} of {@link BiPredicate}s corresponding to each {@link DeathPreference}
     */
    @Nonnull
    public static Collection<BiPredicate<ItemStack, Integer>> getItemPredicates(@Nonnull final Set<DeathPreference> deathPreferences) {
        return deathPreferences.stream()
                .map(ITEM_PREDICATES::get)
                .filter(Objects::nonNull)
                .toList();
    }
}
