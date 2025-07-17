package io.funky.fangs.keep_it_personal.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.domain.DeathPreferenceContainer;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.minecraft.world.waypoint.ServerWaypoint;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable, ServerWaypoint {
    protected LivingEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @WrapMethod(method = "isExperienceDroppingDisabled")
    public boolean isExperienceDroppingDisabledWrapper(Operation<Boolean> original) {
        return this instanceof DeathPreferenceContainer container
                && container.hasDeathPreference(DeathPreference.EXPERIENCE)
                || original.call();
    }
}
