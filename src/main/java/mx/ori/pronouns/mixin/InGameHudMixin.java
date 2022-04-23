package mx.ori.pronouns.mixin;

import mx.ori.pronouns.PronounsMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.network.MessageType;
import net.minecraft.text.*;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
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
import java.util.HashMap;
import java.util.UUID;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    private static final HashSetValuedHashMap<String, Integer> keyIndices = new HashSetValuedHashMap<>() {{
        put("chat.type.admin",                                 0);
        put("chat.type.advancement.challenge",                 0);
        put("chat.type.advancement.goal",                      0);
        put("chat.type.advancement.task",                      0);
        put("chat.type.emote",                                 0);
        put("chat.type.text",                                  0);
        put("commands.advancement.grant.many.to.one.success",  1);
        put("commands.advancement.grant.one.to.one.success",   1);
        put("commands.advancement.revoke.many.to.one.success", 1);
        put("commands.advancement.revoke.one.to.one.success",  1);
        put("commands.attribute.base_value.get.success",       1);
        put("commands.attribute.base_value.set.success",       1);
        put("commands.attribute.modifier.add.success",         1);
        put("commands.attribute.modifier.remove.success",      1);
        put("commands.attribute.modifier.value.get.success",   1);
        put("commands.attribute.value.get.success",            1);
        put("commands.clear.success.single",                   1);
        put("commands.effect.give.success.single",             1);
        put("commands.effect.clear.everything.success.single", 0);
        put("commands.effect.clear.specific.success.single",   1);
        put("commands.enchant.success.single",                 1);
        put("commands.experience.add.levels.success.single",   1);
        put("commands.experience.add.points.success.single",   1);
        put("commands.experience.query.levels",                0);
        put("commands.experience.query.points",                0);
        put("commands.experience.set.levels.success.single",   1);
        put("commands.experience.set.points.success.single",   1);
        put("commands.gamemode.success.other",                 0);
        put("commands.give.success.single",                    2);
        put("commands.kill.success.single",                    0);
        put("commands.message.display.incoming",               0);
        put("commands.message.display.outgoing",               0);
        put("commands.teleport.success.entity.single",         0);
        put("commands.teleport.success.entity.single",         1);
        put("death.attack.outOfWorld",                         0);
        put("multiplayer.player.joined",                       0);
        put("multiplayer.player.left",                         0);
    }};

    private final Logger LOGGER = LoggerFactory.getLogger("pronouns");

    private final HashMap<String, String> pronounsMap = new HashMap<>();

    @Inject(at = @At("HEAD"), method = "addChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V", cancellable = true)
    private void addChatMessage(MessageType type, Text message, UUID sender, CallbackInfo ci) {
        if(message instanceof TranslatableText tmessage) {
            var key = tmessage.getKey();
            switch (key) {
                case "chat.type.text" -> {
                    String playerName = tryRegisterPronouns(tmessage);
                    if (playerName != null) {
                        if (client.player != null && !playerName.equals(client.player.getName().getString())) {
                            client.player.sendChatMessage(String.format("/tell %s #pronouns %s", playerName, PronounsMod.myPronouns));
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
            var pronouns = pronounsMap.get(node.getString());
            if(pronouns != null) {
                var tpronouns = new TranslatableText(pronouns);
                var event = new HoverEvent(HoverEvent.Action.SHOW_TEXT, tpronouns);
                node.fillStyle(Style.EMPTY.withHoverEvent(event));
            }
        }
    }

    private String tryRegisterPronouns(TranslatableText tmessage) {
        var args = tmessage.getArgs();
        if(args[0] instanceof LiteralText lname && args[1] instanceof String line) {
            var name = lname.getString();
            var pronouns = parsePronouns(line);
            if (pronouns != null) {
                pronounsMap.put(name, pronouns);
                LOGGER.info(String.format("registered pronouns for %s: %s", name, pronouns));
                return name;
            }
        }
        return null;
    }

    private String parsePronouns(String line) {
        var words = line.split(" ");
        if(words.length == 2 && words[0].equals("#pronouns")) {
            return words[1];
        } else {
            return null;
        }
    }

    private @NotNull ArrayList<LiteralText> toReplace(Text message) {
        var r = new ArrayList<LiteralText>();

        if(message instanceof TranslatableText tmessage) {
            var key = tmessage.getKey();
            var indices = keyIndices.get(key);
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
