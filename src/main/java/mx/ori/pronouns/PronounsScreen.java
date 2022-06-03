package mx.ori.pronouns;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.HashMap;

public class PronounsScreen extends GameOptionsScreen {
    private static final String[] pronounsCollection = {
        "any",
        "ae/aer",
        "e/em",
        "fae/faer",
        "he/him",
        "he/she",
        "he/they",
        "it/its",
        "per/per",
        "she/her",
        "she/they",
        "they/them",
        "ve/ver",
        "xe/xem",
        "zie/hir",
    };

    private PronounsSelectionListWidget pronounsSelectionList;
    private TextFieldWidget pronounsField;

    public PronounsScreen(Screen parent, GameOptions gameOptions) {
        super(parent, gameOptions, new LiteralText("Pronouns"));
    }

    private void saveAndClose() {
        String pronouns = pronounsField.getText();
        PronounsMod.CONFIG.pronouns = pronouns.isEmpty() ? null : pronouns;
        PronounsMod.CONFIG.save();
        close();
    }

    @Override
    protected void init() {
        pronounsSelectionList = new PronounsSelectionListWidget(client);
        // there's 61 units between the bottom of the selection list and the bottom of the screen,
        // so we can split it up as 7 20 7 20 7
        pronounsField = new TextFieldWidget(textRenderer, width / 2 - 75, height - 54, 150, 20, Text.of("")) {
            { setChangedListener(pronounsSelectionList::selectPronouns); }
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == InputUtil.GLFW_KEY_ENTER) {
                    saveAndClose();
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };
        pronounsField.setText(PronounsMod.CONFIG.pronouns);
        addSelectableChild(pronounsSelectionList);
        addDrawableChild(pronounsField);
        addDrawableChild(new ButtonWidget(width / 2 - 75, height - 27, 150, 20, ScreenTexts.DONE, (button) -> {
            saveAndClose();
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
        private PronounsEntry nullPronouns;
        private HashMap<String, PronounsEntry> entriesMap = new HashMap<>();

        public PronounsSelectionListWidget(MinecraftClient client) {
            super(client, PronounsScreen.this.width, PronounsScreen.this.height, 32, PronounsScreen.this.height - 65 + 4, 18);

            nullPronouns = new PronounsEntry();
            addEntry(nullPronouns);
            for (String pronouns : PronounsScreen.pronounsCollection) {
                var entry = new PronounsEntry(pronouns);
                addEntry(entry);
                entriesMap.put(pronouns, entry);
            }
        }

        public void selectPronouns(String pronouns) {
            var selected = pronouns.isEmpty() ? nullPronouns : entriesMap.get(pronouns);
            setSelected(selected);
            centerScrollOn(selected);
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



        public class PronounsEntry extends AlwaysSelectedEntryListWidget.Entry<PronounsEntry> {
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
                PronounsScreen.this.pronounsField.setText(pronouns != null ? pronouns : "");
            }

            @Override
            public Text getNarration() {
                return new LiteralText(pronouns);
            }
        }
    }
}
