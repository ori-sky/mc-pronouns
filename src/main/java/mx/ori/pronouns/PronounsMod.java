package mx.ori.pronouns;

import draylar.omegaconfig.OmegaConfig;
import mx.ori.pronouns.mixin.ChatHudAccessor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

public class PronounsMod implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("pronouns");
	public static final PronounsConfig CONFIG = OmegaConfig.register(PronounsConfig.class);

	public static final HashMap<String, String> pronounsMap = new HashMap<>();

	private static MinecraftClient client = null;

	@Override
	public void onInitializeClient() {
		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof TitleScreen) {
				var buttons = Screens.getButtons(screen);

				buttons.get(3).y += 12;
				buttons.get(4).y += 12;
				buttons.get(5).y += 12;
				buttons.get(6).y += 12;

				final int width = 200;
				var lit = Text.literal("Pronouns").fillStyle(Style.EMPTY.withColor(0xFF7DBE));
				var button = new ButtonWidget(scaledWidth / 2 - width / 2, buttons.get(2).y + 24, width, 20, lit, ignore -> client.setScreen(new PronounsScreen(screen, client.options)));
				Screens.getButtons(screen).add(button);
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, cli) -> {
			client = cli;
			broadcast();
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, cli) -> pronounsMap.clear());
	}

	public static String getOwnName() {
		assert client.player != null;
		return client.player.getName().getString();
	}

	public static void broadcast() {
		assert client.player != null;
		String fmt = CONFIG.pronouns == null ? "#pronouns" : "#pronouns %s";
		client.player.sendChatMessage(String.format(fmt, CONFIG.pronouns), null);
	}

	public static void whisper(String to) {
		assert client.player != null;
		if(!to.equals(getOwnName())) {
			String fmt = CONFIG.pronouns == null ? "tell %s #pronouns" : "tell %s #pronouns %s";
			client.player.sendCommand(String.format(fmt, to, CONFIG.pronouns), null);
		}
	}

	public static void register(PronounsMessage pm) {
		if (pm.pronouns() != null) {
			pronounsMap.put(pm.from(), pm.pronouns());
			LOGGER.info(String.format("registered pronouns for %s: %s", pm.from(), pm.pronouns()));
		} else {
			pronounsMap.remove(pm.from());
			LOGGER.info(String.format("unregistered pronouns for %s", pm.from()));
		}
		replaceBacklog();
	}

	public static void replaceBacklog() {
		var chatHud = client.inGameHud.getChatHud();
		for (var line : ((ChatHudAccessor) chatHud).getMessages()) {
			replace(line.content());
		}
		chatHud.reset();
	}

	public static void replace(Text text) {
		for (var node : toReplace(text)) {
			var pronouns = pronounsMap.get(node.getString());
			if (pronouns != null) {
				var tpronouns = Text.translatable(pronouns);
				var event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, tpronouns);
				node.fillStyle(Style.EMPTY.withHoverEvent(event));
			}
		}
	}

	private static @NotNull ArrayList<MutableText> toReplace(Text text) {
		var r = new ArrayList<MutableText>();

		if(text.getContent() instanceof TranslatableTextContent content) {
			var args = content.getArgs();

			var key = content.getKey();
			if (key.equals("chat.type.admin")) {
				if (args[1] instanceof Text subtext) {
					r.addAll(toReplace(subtext));
				}
			}

			var indices = MessageType.indexOf(key);
			for (var index : indices) {
				if (args[index] instanceof MutableText node) {
					r.add(node);
				}
			}

			if (indices.isEmpty()) {
				LOGGER.warn(String.format("Unexpected key: %s", key));
			}
		}

		return r;
	}
}
