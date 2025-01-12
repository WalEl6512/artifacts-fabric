package artifacts.mixin.item.everlastingfood;

import artifacts.Artifacts;
import artifacts.common.item.EverlastingFoodItem;
import artifacts.extensions.AnimalExtensions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalMixin implements AnimalExtensions {

	@Shadow
	public abstract boolean isFood(ItemStack stack);

	@Redirect(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Animal;isFood(Lnet/minecraft/world/item/ItemStack;)Z"))
	private boolean cooldownBreedingItem(Animal animalEntity, ItemStack stack, Player player) {
		return this.artifacts$isBreedingItemWithCooldown(stack, player);
	}

	@Inject(method = "usePlayerItem", at = @At("HEAD"), cancellable = true)
	private void cancelEat(Player player, InteractionHand interactionHand, ItemStack stack, CallbackInfo info) {
		Item item = stack.getItem();

		if (item instanceof EverlastingFoodItem) {
			player.getCooldowns().addCooldown(item, Artifacts.CONFIG.general.everlastingFoodCooldown);
			info.cancel();
		}
	}

	@Override
	@Unique
	public boolean artifacts$isBreedingItemWithCooldown(ItemStack stack, Player player) {
		Item item = stack.getItem();
		boolean original = this.isFood(stack);

		if (original && item instanceof EverlastingFoodItem) {
			return !player.getCooldowns().isOnCooldown(item);
		}

		return original;
	}
}
