package mx.ori.pronouns;

import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.Nullable;

public record PronounsMessage(String from, String pronouns, boolean shouldRespond) {
    public @Nullable
    static PronounsMessage create(TranslatableText text) {
        boolean shouldRespond;

        var key = text.getKey();
        switch (key) {
            case "chat.type.text" -> shouldRespond = true;
            case "commands.message.display.incoming" -> shouldRespond = false;
            default -> {
                return null;
            }
        }

        var args = text.getArgs();

        String line = null;
        if (args[1] instanceof String str) {
            line = str;
        } else if (args[1] instanceof LiteralText lit) {
            line = lit.getString();
        }

        if (line != null) {
            var words = line.split(" ");
            if (words[0].equals("#pronouns") && args[0] instanceof LiteralText lit) {
                String name = lit.getString();
                String pronouns = words.length >= 2 ? words[1] : null;
                return new PronounsMessage(name, pronouns, shouldRespond);
            }
        }

        return null;
    }
}
