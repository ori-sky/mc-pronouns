package mx.ori.pronouns;

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
	public static final HashMap<String, String> pronounsMap = new HashMap<>();
	public static String myPronouns = null;

	@Override
	public void onInitializeClient() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if(screen instanceof TitleScreen) {
				var buttons = Screens.getButtons(screen);

				buttons.get(0).y -= 12;
				buttons.get(1).y -= 12;
				buttons.get(2).y -= 12;

				final int yOffset = 6;
				for(int i = 0; i < 7; ++i) {
					buttons.get(i).y += yOffset;
				}

				final int width = 200;
				var lit = new LiteralText("Pronouns").fillStyle(Style.EMPTY.withColor(0xFF7DBE));
				var button = new ButtonWidget(scaledWidth / 2 - width / 2, buttons.get(2).y + 24, width, 20, lit, ignore -> client.setScreen(new PronounsScreen(screen, client.options)));
				Screens.getButtons(screen).add(button);
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.player != null) {
				String fmt = myPronouns == null ? "#pronouns" : "#pronouns %s";
				client.player.sendChatMessage(String.format(fmt, myPronouns));
			}
		});
	}
}
