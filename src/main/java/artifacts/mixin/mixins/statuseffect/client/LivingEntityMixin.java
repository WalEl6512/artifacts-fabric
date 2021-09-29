package artifacts.mixin.mixins.statuseffect.client;

import artifacts.item.curio.TrinketArtifactItem;
import artifacts.trinkets.TrinketsHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

	/**
	 * Show the effect as permanent, which normally only happens if the duration is >= 32767
	 * Doing it here makes sure it is set to permanent everytime the server sent an update packet
	 */
	@Inject(method = "forceAddEffect", at = @At("HEAD"))
	private void showStatusEffectPermanent(MobEffectInstance effect, CallbackInfo info) {
		//noinspection ConstantConditions
		if ((Object) this instanceof LivingEntity) {

			TrinketsHelper.getAllEquipped((LivingEntity) (Object) this).forEach(stack -> {
				MobEffectInstance trinketPermEffect = ((TrinketArtifactItem) stack.getItem()).getPermanentEffect();

				if (trinketPermEffect != null && trinketPermEffect.getEffect() == effect.getEffect()) {
					effect.setNoCounter(true);
				}
			});
		}
	}
}
