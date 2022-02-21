package artifacts.common.item.curio;

import artifacts.Artifacts;
import artifacts.common.events.PlayHurtSoundCallback;
import artifacts.common.item.ArtifactItem;
import artifacts.common.trinkets.TrinketsHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class TrinketArtifactItem extends ArtifactItem implements Trinket {

	public TrinketArtifactItem() {
		// DispenserBlock.registerBehavior(this, TrinketItem.TRINKET_DISPENSER_BEHAVIOR); TODO: bug, missing in trinkets rewrite
		PlayHurtSoundCallback.EVENT.register(this::playExtraHurtSound);
		TrinketsApi.registerTrinket(this, this);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
		// Cycle artifact status when sneak right-clicking
		if (user.isShiftKeyDown()) {
			ItemStack stack = user.getItemInHand(hand);
			CompoundTag tag = stack.getOrCreateTagElement("Artifacts");
			tag.putByte("Status", (byte) ArtifactStatus.nextIndex(tag.getByte("Status")));
			stack.addTagElement("Artifacts", tag);

			if (level.isClientSide()) {
				// Show enabled/disabled message above hotbar
				ChatFormatting enabledColor = TrinketsHelper.areEffectsEnabled(stack) ? ChatFormatting.GREEN : ChatFormatting.RED;
				Component enabledText = new TranslatableComponent(getEffectsEnabledLanguageKey(stack)).withStyle(enabledColor);
				Minecraft.getInstance().gui.setOverlayMessage(enabledText, false);
			}

			return InteractionResultHolder.success(stack);
		}

		ItemStack stack = user.getItemInHand(hand);
		if (TrinketItem.equipItem(user, stack)) {
			// Play right click equip sound
			SoundInfo sound = this.getEquipSoundInfo();
			user.playSound(sound.soundEvent(), sound.volume(), sound.pitch());

			return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
		}

		return super.use(level, user, hand);
	}

	@Override
	public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
		if (TrinketsHelper.areEffectsEnabled(stack)) {
			curioTick(entity, stack);
		}
	}

	protected void curioTick(LivingEntity livingEntity, ItemStack stack) {
	}

	@Override
	public final Multimap<Attribute, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
		if (TrinketsHelper.areEffectsEnabled(stack)) {
			return this.applyModifiers(stack, slot, entity, uuid);
		}
		return HashMultimap.create();
	}

	protected Multimap<Attribute, AttributeModifier> applyModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, UUID uuid) {
		return HashMultimap.create();
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flags) {
		super.appendHoverText(stack, world, tooltip, flags);
		MutableComponent enabled = new TranslatableComponent(getEffectsEnabledLanguageKey(stack)).withStyle(ChatFormatting.GOLD);
		Component togglekeybind = new TranslatableComponent("artifacts.trinket.togglekeybind").withStyle(ChatFormatting.GRAY);
		tooltip.add(enabled.append(" ").append(togglekeybind));
	}

	/**
	 * @return The {@link SoundInfo} to play when the artifact is right-click equipped
	 */
	protected SoundInfo getEquipSoundInfo() {
		return new SoundInfo(SoundEvents.ARMOR_EQUIP_GENERIC);
	}

	/**
	 * @return An extra {@link SoundEvent} to play when an entity wearing this artifact is hurt
	 */
	protected SoundEvent getExtraHurtSound() {
		return null;
	}

	/**
	 * Used to give a Trinket a permanent status effect while wearing it.
	 * The StatusEffectInstance is applied every 15 ticks so a duration greater than that is required.
	 *
	 * @return The {@link MobEffectInstance} to be applied while wearing this artifact
	 */
	public MobEffectInstance getPermanentEffect() {
		return null;
	}

	private void playExtraHurtSound(LivingEntity entity, float volume, float pitch) {
		if (Artifacts.CONFIG.general.playExtraHurtSounds) {
			SoundEvent hurtSound = getExtraHurtSound();

			if (hurtSound != null && TrinketsHelper.isEquipped(this, entity, true)) {
				entity.playSound(hurtSound, volume, pitch);
			}
		}
	}

	public static void addModifier(AttributeInstance instance, AttributeModifier modifier) {
		if (!instance.hasModifier(modifier)) {
			instance.addTransientModifier(modifier);
		}
	}

	public static void removeModifier(AttributeInstance instance, AttributeModifier modifier) {
		if (instance.hasModifier(modifier)) {
			instance.removeModifier(modifier);
		}
	}

	private static String getEffectsEnabledLanguageKey(ItemStack stack) {
		return TrinketsHelper.areEffectsEnabled(stack) ? "artifacts.trinket.effectsenabled" : "artifacts.trinket.effectsdisabled";
	}

	public enum ArtifactStatus {
		ALL_ENABLED(true, true),
		COSMETIC_ONLY(false, true);
		// EFFECTS_ONLY(true, false);

		private final boolean hasEffects;
		private final boolean hasCosmetics;

		ArtifactStatus(boolean hasEffects, boolean hasCosmetics) {
			this.hasEffects = hasEffects;
			this.hasCosmetics = hasCosmetics;
		}

		public boolean hasEffects() {
			return hasEffects;
		}

		public boolean hasCosmetics() {
			return hasCosmetics;
		}

		public static int nextIndex(int index) {
			return index >= values().length - 1 ? 0 : index + 1;
		}
	}

	protected record SoundInfo(SoundEvent soundEvent, float volume, float pitch) {

		// Changes access modifier to public
		@SuppressWarnings({"RedundantRecordConstructor", "RedundantSuppression"})
		public SoundInfo {}

		public SoundInfo(SoundEvent soundEvent) {
			this(soundEvent, 1f, 1f);
		}
	}
}