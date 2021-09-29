package artifacts.item.curio.belt;

import artifacts.Artifacts;
import artifacts.components.SwimAbilityComponent;
import artifacts.init.Components;
import artifacts.init.Slot;
import artifacts.init.SoundEvents;
import artifacts.item.curio.TrinketArtifactItem;
import be.florens.expandability.api.fabric.PlayerSwimCallback;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.Collections;
import java.util.List;


public class HeliumFlamingoItem extends TrinketArtifactItem {

	public static final ResourceLocation C2S_AIR_SWIMMING_ID = Artifacts.id("c2s_air_swimming");

	// TODO: config
	public static final int MAX_FLIGHT_TIME = 150;
	public static final int RECHARGE_TIME = 300;

	public HeliumFlamingoItem() {
		super(Slot.BELT);
		PlayerSwimCallback.EVENT.register(HeliumFlamingoItem::onPlayerSwim);
		ServerPlayNetworking.registerGlobalReceiver(C2S_AIR_SWIMMING_ID, HeliumFlamingoItem::handleAirSwimmingPacket);
	}

	private static InteractionResult onPlayerSwim(Player player) {
		return Components.SWIM_ABILITIES.maybeGet(player)
				.filter(SwimAbilityComponent::isSwimming)
				.map(swimAbilities -> InteractionResult.SUCCESS)
				.orElse(InteractionResult.PASS);
	}

	private static void handleAirSwimmingPacket(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender packetSender) {
		boolean shouldSwim = buf.readBoolean();
		server.execute(() -> Components.SWIM_ABILITIES.maybeGet(player)
				.ifPresent(swimAbilities -> swimAbilities.setSwimming(shouldSwim)));
	}

	@Override
	protected SoundInfo getEquipSound() {
		return new SoundInfo(SoundEvents.POP, 1f, 0.7f);
	}

	@Override
	protected List<String> getTooltipDescriptionArguments() {
		String translationKey = Minecraft.getInstance().options.keySprint.saveString();
		return Collections.singletonList(Language.getInstance().getOrDefault(translationKey));
	}
}
