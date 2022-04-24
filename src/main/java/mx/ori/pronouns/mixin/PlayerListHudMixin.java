package mx.ori.pronouns.mixin;

import mx.ori.pronouns.PronounsMod;
import net.minecraft.client.gui.hud.PlayerListHud;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    private final Logger LOGGER = LoggerFactory.getLogger("pronouns");

    @ModifyArg(method = "getPlayerName(Lnet/minecraft/client/network/PlayerListEntry;)Lnet/minecraft/text/Text;", at = @At(value = "INVOKE", target = "Lnet/minecraft/text/LiteralText;<init>(Ljava/lang/String;)V"), index = 0)
    private String init(String name) {
        var pronouns = PronounsMod.pronounsMap.get(name);
        if(pronouns != null) {
            return String.format("%s (%s)", name, pronouns);
        } else {
            return name;
        }
    }
}
