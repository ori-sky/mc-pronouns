package mx.ori.pronouns;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class PronounsScreen extends GameOptionsScreen {
    private static final ArrayList<String> pronounsCollection = new ArrayList<>() {{
        add("any");
        add("ae/aer");
        add("e/em");
        add("fae/faer");
        add("he/him");
        add("he/she");
        add("he/they");
        add("it/its");
        add("per/per");
        add("she/her");
        add("she/they");
        add("they/them");
        add("ve/ver");
        add("xe/xem");
        add("zie/hir");
        add("other");
    }};

    private PronounsSelectionListWidget pronounsSelectionList;

    public PronounsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, new LiteralText("Pronouns"));
    }

    @Override
    protected void init() {
        pronounsSelectionList = new PronounsSelectionListWidget(client);
        addSelectableChild(pronounsSelectionList);
        addDrawableChild(new ButtonWidget(width / 2 - 75, height - 38, 150, 20, ScreenTexts.DONE, (button) -> {
            var entry = pronounsSelectionList.getSelectedOrNull();
            if(entry != null) {
                PronounsMod.CONFIG.pronouns = entry.pronouns;
                PronounsMod.CONFIG.save();
            }
            close();
        }));
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        pronounsSelectionList.render(matrices, mouseX, mouseY, delta);
        drawCenteredText(matrices, textRenderer, title, width / 2, 16, 16777215);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private class PronounsSelectionListWidget extends AlwaysSelectedEntryListWidget<PronounsSelectionListWidget.PronounsEntry> {
        public PronounsSelectionListWidget(MinecraftClient client) {
            super(client, PronounsScreen.this.width, PronounsScreen.this.height, 32, PronounsScreen.this.height - 65 + 4, 18);

            addEntry(new PronounsEntry());
            for (String pronouns : PronounsScreen.pronounsCollection) {
                var entry = new PronounsEntry(pronouns);
                addEntry(entry);
                if(pronouns.equals(PronounsMod.CONFIG.pronouns)) {
                    this.setSelected(entry);
                }
            }

            if (getSelectedOrNull() != null) {
                centerScrollOn(getSelectedOrNull());
            }
        }

        @Override
        protected int getScrollbarPositionX() {
            return super.getScrollbarPositionX() + 20;
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() - 80;
        }

        @Override
        protected void renderBackground(MatrixStack matrices) {
            PronounsScreen.this.renderBackground(matrices);
        }

        @Override
        protected boolean isFocused() {
            return PronounsScreen.this.getFocused() == this;
        }



        public class PronounsEntry extends Entry<PronounsEntry> {
            final String pronouns;

            public PronounsEntry() {
                pronouns = null;
            }

            public PronounsEntry(String pronouns) {
                this.pronouns = pronouns;
            }

            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                var str = pronouns == null ? "(none)" : pronouns;
                PronounsScreen.this.textRenderer.drawWithShadow(matrices, str, (float)(PronounsSelectionListWidget.this.width / 2 - PronounsScreen.this.textRenderer.getWidth(str) / 2), (float)(y + 1), 16777215, true);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    this.onPressed();
                    return true;
                } else {
                    return false;
                }
            }

            private void onPressed() {
                PronounsSelectionListWidget.this.setSelected(this);
            }

            @Override
            public Text getNarration() {
                return new LiteralText(pronouns);
            }
        }
    }
}
