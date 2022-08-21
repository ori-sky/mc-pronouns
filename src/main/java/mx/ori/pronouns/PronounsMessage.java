package mx.ori.pronouns;

import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.Nullable;

public record PronounsMessage(String from, String pronouns, boolean shouldRespond) {
    public @Nullable
    static PronounsMessage create(Text text) {
        boolean shouldRespond;

        if(text.getContent() instanceof TranslatableTextContent content) {
            var key = content.getKey();
            var args = content.getArgs();
            String from;

            switch (key) {
                case "chat.type.text" -> {
                    from = ((LiteralTextContent)((Text)args[0]).getContent()).string();
                    shouldRespond = !from.equals(PronounsMod.getOwnName());
                }
                case "commands.message.display.incoming", "commands.message.display.outgoing" -> {
                    from = ((LiteralTextContent)((Text)args[0]).getContent()).string();
                    shouldRespond = false;
                }
                default -> {
                    return null;
                }
            }

            String line = null;
            if (args[1] instanceof String str) {
                line = str;
            } else if(args[1] instanceof Text t && t.getContent() instanceof LiteralTextContent lit) {
                line = lit.string();
            }

            if (line != null) {
                var words = line.split(" ");
                if (words[0].equals("#pronouns")) {
                    var pronouns = words.length >= 2 ? words[1] : null;
                    return new PronounsMessage(from, pronouns, shouldRespond);
                }
            }
        }

        return null;
    }
}
