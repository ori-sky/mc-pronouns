package mx.ori.pronouns.mixin;

import mx.ori.pronouns.PronounsMessage;
import mx.ori.pronouns.PronounsMod;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudInjector {
    private static final Logger LOGGER = LoggerFactory.getLogger("pronouns");

    @Inject(at = @At("HEAD"), method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V", cancellable = true)
    private void addMessage(Text text, MessageSignatureData signature, int ticks, MessageIndicator indicator, boolean refresh, CallbackInfo ci) {
        if(!refresh) {
            if(text.getContent() instanceof TranslatableTextContent content) {
                LOGGER.info(content.getKey());
                var pm = PronounsMessage.create(text);
                if(pm != null) {
                    // respond via whisper
                    if (pm.shouldRespond()) {
                        PronounsMod.whisper(pm.from());
                    }

                    PronounsMod.register(pm);

                    ci.cancel();
                }
            }
        }
        PronounsMod.replace(text);
    }
}
