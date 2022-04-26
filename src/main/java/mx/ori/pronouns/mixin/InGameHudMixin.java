package mx.ori.pronouns.mixin;

import mx.ori.pronouns.MessageType;
import mx.ori.pronouns.PronounsMessage;
import mx.ori.pronouns.PronounsMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.UUID;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    private final Logger LOGGER = LoggerFactory.getLogger("pronouns");

    @Inject(at = @At("HEAD"), method = "addChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V", cancellable = true)
    private void addChatMessage(net.minecraft.network.MessageType type, Text message, UUID sender, CallbackInfo ci) {
        if (message instanceof TranslatableText text) {
            var pm = PronounsMessage.create(text);
            if (pm != null) {
                if (pm.shouldRespond()) {
                    assert client.player != null;
                    if (!pm.from().equals(client.player.getName().getString())) {
                        var pronouns = PronounsMod.CONFIG.pronouns;
                        String fmt = pronouns == null ? "/tell %s #pronouns" : "/tell %s #pronouns %s";
                        client.player.sendChatMessage(String.format(fmt, pm.from(), pronouns));
                    }
                }
                if (pm.pronouns() != null) {
                    PronounsMod.pronounsMap.put(pm.from(), pm.pronouns());
                    LOGGER.info(String.format("registered pronouns for %s: %s", pm.from(), pm.pronouns()));
                } else {
                    PronounsMod.pronounsMap.remove(pm.from());
                    LOGGER.info(String.format("unregistered pronouns for %s", pm.from()));
                }
                ci.cancel();
            } else if (text.getKey().equals("commands.message.display.outgoing")) {
                if (text.getArgs()[1] instanceof LiteralText lit) {
                    var words = lit.getString().split(" ");
                    if (words.length == 2 && words[0].equals("#pronouns")) {
                        ci.cancel();
                    }
                }
            }

            for (var node : toReplace(text)) {
                var pronouns = PronounsMod.pronounsMap.get(node.getString());
                if (pronouns != null) {
                    var tpronouns = new TranslatableText(pronouns);
                    var event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, tpronouns);
                    node.fillStyle(Style.EMPTY.withHoverEvent(event));
                }
            }
        }
    }

    private @NotNull ArrayList<LiteralText> toReplace(TranslatableText text) {
        var r = new ArrayList<LiteralText>();

        var key = text.getKey();
        var indices = MessageType.indexOf(key);
        if (!indices.isEmpty()) {
            var args = text.getArgs();
            for (var index : indices) {
                if (args[index] instanceof LiteralText node) {
                    r.add(node);
                }
            }
            if (key.equals("chat.type.admin")) {
                if (args[1] instanceof TranslatableText subtext) {
                    r.addAll(toReplace(subtext));
                }
            }
        } else {
            LOGGER.warn((new IllegalStateException("Unexpected key: " + key)).toString());
        }

        return r;
    }
}
