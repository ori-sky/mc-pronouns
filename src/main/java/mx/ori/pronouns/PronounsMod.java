package mx.ori.pronouns;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class PronounsMod implements ModInitializer {
	public static String myPronouns = "they/them";
	@Override
	public void onInitialize() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			client.player.sendChatMessage(String.format("#pronouns %s", myPronouns));
		});
	}
}
