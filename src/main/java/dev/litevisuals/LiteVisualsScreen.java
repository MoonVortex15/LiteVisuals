package dev.litevisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LiteVisualsScreen extends Screen {
    private int tab = 0;

    private static final int PURPLE = 0xFFB56BFF;
    private static final int WHITE = 0xFFF0E8FF;
    private static final int MUTED = 0xFFAAA8C2;
    private static final int PANEL = 0xFF181828;
    private static final int BACKGROUND = 0xFF080811;

    public LiteVisualsScreen() {
        super(Text.literal("LiteVisuals"));
    }

    @Override
    public void renderBackground(
            DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Непрозрачный фон без стандартного размытия.
        ctx.fill(0, 0, width, height, BACKGROUND);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Очищаем фон перед отрисовкой меню.
        ctx.fill(0, 0, width, height, BACKGROUND);

        int left = width / 2 - 175;
        int top = height / 2 - 115;
        int right = left + 350;
        int bottom = top + 230;

        // Основная панель.
        ctx.fill(left, top, right, bottom, PANEL);
        ctx.fill(left, top, right, top + 3, LiteVisualsClient.accentColor);
        ctx.fill(left, top, left + 105, bottom, 0xFF10101D);
        ctx.fill(left + 105, top + 42, right, top + 43, 0xFF353044);

        text(ctx, "LITE", left + 15, top + 13,
                LiteVisualsClient.accentColor);
        text(ctx, "VISUALS", left + 45, top + 13, WHITE);
        text(ctx, "CLIENT / 1.21.4", left + 15, top + 29, MUTED);

        drawTab(ctx, left, top, 0, "ВИЗУАЛЫ", mouseX, mouseY);
        drawTab(ctx, left, top, 1, "ИНТЕРФЕЙС", mouseX, mouseY);
        drawTab(ctx, left, top, 2, "О МОДЕ", mouseX, mouseY);

        String heading = tab == 0
                ? "ВИЗУАЛЬНЫЕ МОДУЛИ"
                : tab == 1 ? "НАСТРОЙКИ HUD" : "LITEVISUALS";

        text(ctx, heading, left + 120, top + 15, WHITE);
        text(ctx, "Нажми на карточку, чтобы переключить",
                left + 120, top + 29, MUTED);

        if (tab == 0) {
            drawModule(ctx, left, top, 0,
                    "HitMarker",
                    "Метка при попытке атаки.",
                    LiteVisualsClient.hitMarkerEnabled,
                    mouseX, mouseY);

            drawModule(ctx, left, top, 1,
                    "Акцентный цвет",
                    "Изменяет цвет оформления.",
                    true, mouseX, mouseY);

        } else if (tab == 1) {
            drawModule(ctx, left, top, 0,
                    "FPS",
                    "Показывает частоту кадров.",
                    LiteVisualsClient.fpsEnabled,
                    mouseX, mouseY);

            drawModule(ctx, left, top, 1,
                    "Координаты",
                    "Показывает X, Y и Z.",
                    LiteVisualsClient.coordsEnabled,
                    mouseX, mouseY);

            drawModule(ctx, left, top, 2,
                    "Направление",
                    "Показывает сторону света.",
                    LiteVisualsClient.facingEnabled,
                    mouseX, mouseY);

            drawModule(ctx, left, top, 3,
                    "Цель под прицелом",
                    "Показывает имя сущности.",
                    LiteVisualsClient.targetEnabled,
                    mouseX, mouseY);

            drawModule(ctx, left, top, 4,
                    "Весь HUD",
                    "Включает информационную панель.",
                    LiteVisualsClient.hudEnabled,
                    mouseX, mouseY);

        } else {
            text(ctx, "Собственный визуальный клиент",
                    left + 120, top + 70, WHITE);
            text(ctx, "Right Shift — открыть меню",
                    left + 120, top + 91, MUTED);
            text(ctx, "H — переключить HUD",
                    left + 120, top + 107, MUTED);
            text(ctx, "LiteVisuals Client",
                    left + 120, top + 135,
                    LiteVisualsClient.accentColor);
        }

        text(ctx, "LITEVISUALS  |  ESC — закрыть",
                left + 120, bottom - 18, MUTED);

        // Не вызываем super.render(): все элементы меню рисуются вручную.
    }

    private void drawTab(
            DrawContext ctx, int left, int top, int id,
            String label, int mx, int my) {

        int y = top + 52 + id * 34;

        boolean selected = tab == id;
        boolean hover = mx >= left + 8 && mx <= left + 98
                && my >= y && my <= y + 27;

        if (selected) {
            ctx.fill(left + 7, y, left + 99, y + 27, 0xFF30213F);
            ctx.fill(left + 7, y, left + 10, y + 27,
                    LiteVisualsClient.accentColor);
        } else if (hover) {
            ctx.fill(left + 7, y, left + 99, y + 27, 0xFF242333);
        }

        text(ctx, label, left + 17, y + 9,
                selected ? LiteVisualsClient.accentColor : WHITE);
    }

    private void drawModule(
            DrawContext ctx, int left, int top, int index,
            String name, String description, boolean enabled,
            int mx, int my) {

        int x = left + 117;
        int y = top + 50 + index * 33;
        int right = left + 337;

        boolean hover = mx >= x && mx <= right
                && my >= y && my <= y + 29;

        ctx.fill(x, y, right, y + 29,
                hover ? 0xFF302B40 : 0xFF222131);

        ctx.fill(x, y, x + 2, y + 29,
                enabled ? LiteVisualsClient.accentColor : 0xFF514D60);

        text(ctx, name, x + 9, y + 4, WHITE);
        text(ctx, description, x + 9, y + 17, MUTED);

        int sx = right - 34;
        int sy = y + 9;

        ctx.fill(sx, sy, sx + 23, sy + 11,
                enabled ? LiteVisualsClient.accentColor : 0xFF555363);

        if (enabled) {
            ctx.fill(sx + 13, sy + 2, sx + 21, sy + 9, WHITE);
        } else {
            ctx.fill(sx + 2, sy + 2, sx + 10, sy + 9, WHITE);
        }
    }

    @Override
    public boolean mouseClicked(
            double mouseX, double mouseY, int button) {

        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int left = width / 2 - 175;
        int top = height / 2 - 115;

        // Переключение вкладок.
        for (int i = 0; i < 3; i++) {
            int y = top + 52 + i * 34;

            if (mouseX >= left + 7 && mouseX <= left + 99
                    && mouseY >= y && mouseY <= y + 27) {
                tab = i;
                return true;
            }
        }

        int count = tab == 0 ? 2 : tab == 1 ? 5 : 0;

        for (int i = 0; i < count; i++) {
            int x = left + 117;
            int y = top + 50 + i * 33;

            if (mouseX >= x && mouseX <= left + 337
                    && mouseY >= y && mouseY <= y + 29) {

                if (tab == 0) {
                    if (i == 0) {
                        LiteVisualsClient.hitMarkerEnabled =
                                !LiteVisualsClient.hitMarkerEnabled;
                    } else {
                        LiteVisualsClient.accentColor =
                                LiteVisualsClient.accentColor == PURPLE
                                        ? 0xFF62D9FF : PURPLE;
                    }
                } else {
                    switch (i) {
                        case 0 -> LiteVisualsClient.fpsEnabled =
                                !LiteVisualsClient.fpsEnabled;
                        case 1 -> LiteVisualsClient.coordsEnabled =
                                !LiteVisualsClient.coordsEnabled;
                        case 2 -> LiteVisualsClient.facingEnabled =
                                !LiteVisualsClient.facingEnabled;
                        case 3 -> LiteVisualsClient.targetEnabled =
                                !LiteVisualsClient.targetEnabled;
                        case 4 -> LiteVisualsClient.hudEnabled =
                                !LiteVisualsClient.hudEnabled;
                    }
                }

                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void text(
            DrawContext ctx, String value, int x, int y, int color) {
        ctx.drawTextWithShadow(
                textRenderer, Text.literal(value), x, y, color);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
