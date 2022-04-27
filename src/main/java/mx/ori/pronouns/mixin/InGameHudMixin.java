package mx.ori.pronouns.mixin;

import mx.ori.pronouns.PronounsMessage;
import mx.ori.pronouns.PronounsMod;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(at = @At("HEAD"), method = "addChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V", cancellable = true)
    private void addChatMessage(net.minecraft.network.MessageType type, Text message, UUID sender, CallbackInfo ci) {
        if (message instanceof TranslatableText text) {
            var pm = PronounsMessage.create(text);
            if (pm != null) {
                // respond via whisper
                if (pm.shouldRespond()) {
                    PronounsMod.whisper(pm.from());
                }

                PronounsMod.register(pm);

                ci.cancel();
            } else if (text.getKey().equals("commands.message.display.outgoing")) {
                if (text.getArgs()[1] instanceof LiteralText lit) {
                    var words = lit.getString().split(" ");
                    if (words[0].equals("#pronouns")) {
                        ci.cancel();
                    }
                }
            }

            PronounsMod.replace(text);
        }
    }
}
