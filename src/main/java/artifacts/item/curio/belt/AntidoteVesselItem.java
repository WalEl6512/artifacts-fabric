package artifacts.item.curio.belt;

import artifacts.item.curio.TrinketArtifactItem;
import artifacts.mixin.extensions.MobEffectInstanceExtensions;
import artifacts.mixin.mixins.accessors.MobEffectAccessor;
import artifacts.trinkets.Slot;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AntidoteVesselItem extends TrinketArtifactItem {

    public AntidoteVesselItem() {
        super(Slot.BELT);
    }

    @Override
	protected void curioTick(LivingEntity livingEntity, ItemStack stack) {
		// Reduce duration of all negative status effects to 80
		livingEntity.getActiveEffectsMap().forEach((effect, instance) -> {
			if (!effect.isInstantenous() && ((MobEffectAccessor) effect).getCategory() != MobEffectCategory.BENEFICIAL && instance.getDuration() > 80) {
				((MobEffectInstanceExtensions) instance).artifacts$setDuration(80);
			}
		});
	}

	@Override
	public SoundInfo getEquipSound() {
		return new SoundInfo(SoundEvents.BOTTLE_FILL);
	}
}
