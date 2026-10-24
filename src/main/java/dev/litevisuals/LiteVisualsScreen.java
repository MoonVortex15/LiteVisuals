
package dev.litevisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class LiteVisualsScreen extends Screen {
    private int selectedTab = 0;

    public LiteVisualsScreen() {
        super(Text.literal("LiteVisuals"));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 120;
        int top = this.height / 2 - 90;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Visuals"),
            button -> {
                selectedTab = 0;
                clearAndInit();
            }
        ).dimensions(left, top, 76, 20).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal("HUD"),
            button -> {
                selectedTab = 1;
                clearAndInit();
            }
        ).dimensions(left + 82, top, 76, 20).build());

        addDrawableChild(ButtonWidget.builder(
            Text.literal("Close"),
            button -> close()
        ).dimensions(left + 164, top, 76, 20).build());

        if (selectedTab == 0) {
            addDrawableChild(ButtonWidget.builder(
                Text.literal("HitMarker: "
                    + (LiteVisualsClient.hitMarkerEnabled ? "ON" : "OFF")),
                button -> {
                    LiteVisualsClient.hitMarkerEnabled =
                        !LiteVisualsClient.hitMarkerEnabled;
                    clearAndInit();
                }
            ).dimensions(left, top + 42, 240, 22).build());

            addDrawableChild(ButtonWidget.builder(
                Text.literal("Accent: "
                    + colorName(LiteVisualsClient.accentColor)),
                button -> {
                    LiteVisualsClient.accentColor =
                        nextColor(LiteVisualsClient.accentColor);
                    clearAndInit();
                }
            ).dimensions(left, top + 70, 240, 22).build());
        } else {
            addDrawableChild(ButtonWidget.builder(
                Text.literal("HUD: "
                    + (LiteVisualsClient.hudEnabled ? "ON" : "OFF")),
                button -> {
                    LiteVisualsClient.hudEnabled =
                        !LiteVisualsClient.hudEnabled;
                    clearAndInit();
                }
            ).dimensions(left, top + 42, 240, 22).build());
        }
    }

    private static int nextColor(int color) {
        if (color == 0xFFB56BFF) return 0xFF62D9FF;
        if (color == 0xFF62D9FF) return 0xFF72FFAD;
        return 0xFFB56BFF;
    }

    private static String colorName(int color) {
        if (color == 0xFF62D9FF) return "Cyan";
        if (color == 0xFF72FFAD) return "Mint";
        return "Purple";
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        renderBackground(context, mouseX, mouseY, delta);

        int left = this.width / 2 - 130;
        int top = this.height / 2 - 100;

        context.fill(
            left, top, left + 260, top + 200, 0xF0101020
        );
        context.fill(
            left, top, left + 260, top + 3,
            LiteVisualsClient.accentColor
        );

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("LITEVISUALS"),
            this.width / 2,
            top + 12,
            LiteVisualsClient.accentColor
        );

        context.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Visual client  |  1.21.4"),
            this.width / 2,
            top + 25,
            0xFFB8B8D0
        );

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
