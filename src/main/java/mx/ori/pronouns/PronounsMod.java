package mx.ori.pronouns;

import draylar.omegaconfig.OmegaConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;

import java.util.HashMap;

public class PronounsMod implements ClientModInitializer {
	public static final PronounsConfig CONFIG = OmegaConfig.register(PronounsConfig.class);

	public static final HashMap<String, String> pronounsMap = new HashMap<>();

	@Override
	public void onInitializeClient() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if(screen instanceof TitleScreen) {
				var buttons = Screens.getButtons(screen);

				buttons.get(3).y += 12;
				buttons.get(4).y += 12;
				buttons.get(5).y += 12;
				buttons.get(6).y += 12;

				final int width = 200;
				var lit = new LiteralText("Pronouns").fillStyle(Style.EMPTY.withColor(0xFF7DBE));
				var button = new ButtonWidget(scaledWidth / 2 - width / 2, buttons.get(2).y + 24, width, 20, lit, ignore -> client.setScreen(new PronounsScreen(screen, client.options)));
				Screens.getButtons(screen).add(button);
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.player != null) {
				String fmt = PronounsMod.CONFIG.pronouns == null ? "#pronouns" : "#pronouns %s";
				client.player.sendChatMessage(String.format(fmt, PronounsMod.CONFIG.pronouns));
			}
		});
	}
}
