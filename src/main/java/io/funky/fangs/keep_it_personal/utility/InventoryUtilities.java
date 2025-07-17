package io.funky.fangs.keep_it_personal.utility;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public final class InventoryUtilities {
    private InventoryUtilities() throws IllegalAccessException {
        throw new IllegalAccessException(getClass().getSimpleName() + " is a utility class and cannot be instantiated");
    }

    public static final Set<EquipmentSlot> ARMOR_SLOTS = Stream.of(EquipmentSlot.values())
            .filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
            .collect(collectingAndThen(
                    toCollection(() -> EnumSet.noneOf(EquipmentSlot.class)),
                    Collections::unmodifiableSet
            ));

    public static boolean hasCurseOfVanishing(ItemStack itemStack) {
        return EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP);
    }
}
