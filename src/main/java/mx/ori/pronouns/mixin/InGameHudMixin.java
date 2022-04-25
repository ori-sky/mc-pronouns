package mx.ori.pronouns.mixin;

import mx.ori.pronouns.MessageType;
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
    @Shadow @Final private MinecraftClient client;

    private final Logger LOGGER = LoggerFactory.getLogger("pronouns");

    @Inject(at = @At("HEAD"), method = "addChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V", cancellable = true)
    private void addChatMessage(net.minecraft.network.MessageType type, Text message, UUID sender, CallbackInfo ci) {
        if(message instanceof TranslatableText tmessage) {
            var key = tmessage.getKey();
            switch (key) {
                case "chat.type.text" -> {
                    String playerName = tryRegisterPronouns(tmessage);
                    if (playerName != null) {
                        if (client.player != null && !playerName.equals(client.player.getName().getString())) {
                            var pronouns = PronounsMod.CONFIG.pronouns;
                            String fmt = pronouns == null ? "/tell %s #pronouns" : "/tell %s #pronouns %s";
                            client.player.sendChatMessage(String.format(fmt, playerName, pronouns));
                        }
                        ci.cancel();
                    }
                }
                case "commands.message.display.incoming" -> {
                    String playerName = tryRegisterPronouns(tmessage);
                    if (playerName != null) {
                        ci.cancel();
                    }
                }
                case "commands.message.display.outgoing" -> {
                    if (tmessage.getArgs()[1] instanceof LiteralText lit) {
                        var words = lit.getString().split(" ");
                        if (words.length == 2 && words[0].equals("#pronouns")) {
                            ci.cancel();
                        }
                    }
                }
            }
        }

        for(var node : toReplace(message)) {
            var pronouns = PronounsMod.pronounsMap.get(node.getString());
            if(pronouns != null) {
                var tpronouns = new TranslatableText(pronouns);
                var event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, tpronouns);
                node.fillStyle(Style.EMPTY.withHoverEvent(event));
            }
        }
    }

    private String tryRegisterPronouns(TranslatableText tmessage) {
        var args = tmessage.getArgs();
        String name = null;
        if(args[0] instanceof LiteralText lit) {
            name = lit.getString();
        }

        String line = null;
        if(args[1] instanceof String str) {
            line = str;
        } else if(args[1] instanceof LiteralText lit) {
            line = lit.getString();
        }

        if(name != null && line != null) {
            var words = line.split(" ");
            if(words[0].equals("#pronouns")) {
                if(words.length >= 2) {
                    var pronouns = words[1];
                    PronounsMod.pronounsMap.put(name, pronouns);
                    LOGGER.info(String.format("registered pronouns for %s: %s", name, pronouns));
                } else {
                    PronounsMod.pronounsMap.remove(name);
                }
                return name;
            }
        }
        return null;
    }

    private @NotNull ArrayList<LiteralText> toReplace(Text message) {
        var r = new ArrayList<LiteralText>();

        if(message instanceof TranslatableText tmessage) {
            var key = tmessage.getKey();
            var indices = MessageType.indexOf(key);
            if(!indices.isEmpty()) {
                var args = tmessage.getArgs();
                for(var index : indices) {
                    if(args[index] instanceof LiteralText node) {
                        r.add(node);
                    }
                }
                if(key.equals("chat.type.admin")) {
                    if(args[1] instanceof TranslatableText subtext) {
                        r.addAll(toReplace(subtext));
                    }
                }
            } else {
                LOGGER.warn((new IllegalStateException("Unexpected key: " + key)).toString());
            }
        }

        return r;
    }
}
