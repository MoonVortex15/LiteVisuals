
package dev.litevisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class LiteVisualsScreen extends Screen {
    private int tab = 0;
    private boolean hitMarkerSettings = false;

    private static final int PURPLE = 0xFFB56BFF;
    private static final int WHITE = 0xFFF0E8FF;
    private static final int MUTED = 0xFFAAA8C2;
    private static final int PANEL = 0xFF181828;
    private static final int BACKGROUND = 0xFF080811;

    private static final int[] COLORS = {
            0xFFFF4040,
            0xFFB56BFF,
            0xFF62D9FF,
            0xFF65FF91,
            0xFFFFFF55,
            0xFFFFFFFF
    };

    private static final float[] SIZES = {
            4.0f, 6.0f, 8.0f, 10.0f, 14.0f, 18.0f
    };

    private static final int[] DURATIONS = {
            150, 250, 350, 450, 600, 800, 1000
    };

    public LiteVisualsScreen() {
        super(Text.literal("LiteVisuals"));
    }

    @Override
    public void renderBackground(
            DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, BACKGROUND);
    }

    @Override
    public void render(
            DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, BACKGROUND);

        int left = width / 2 - 175;
        int top = height / 2 - 115;
        int right = left + 350;
        int bottom = top + 230;

        ctx.fill(left, top, right, bottom, PANEL);
        ctx.fill(left, top, right, top + 3,
                LiteVisualsClient.accentColor);
        ctx.fill(left, top, left + 105, bottom, 0xFF10101D);
        ctx.fill(left + 105, top + 42, right, top + 43,
                0xFF353044);

        text(ctx, "LITE", left + 15, top + 13,
                LiteVisualsClient.accentColor);
        text(ctx, "VISUALS", left + 45, top + 13, WHITE);
        text(ctx, "CLIENT / 1.21.4", left + 15, top + 29, MUTED);

        drawTab(ctx, left, top, 0, "ВИЗУАЛЫ", mouseX, mouseY);
        drawTab(ctx, left, top, 1, "ИНТЕРФЕЙС", mouseX, mouseY);
        drawTab(ctx, left, top, 2, "О МОДЕ", mouseX, mouseY);

        if (hitMarkerSettings) {
            drawHitMarkerSettings(ctx, left, top, mouseX, mouseY);
        } else {
            String heading = tab == 0
                    ? "ВИЗУАЛЬНЫЕ МОДУЛИ"
                    : tab == 1 ? "НАСТРОЙКИ HUD" : "LITEVISUALS";

            text(ctx, heading, left + 120, top + 15, WHITE);
            text(ctx, "ЛКМ: настройки | ПКМ: вкл/выкл",
                    left + 120, top + 29, MUTED);

            if (tab == 0) {
                drawModule(ctx, left, top, 0,
                        "HitMarker",
                        "ЛКМ: параметры, ПКМ: переключить",
                        LiteVisualsClient.hitMarkerEnabled,
                        mouseX, mouseY);

                drawModule(ctx, left, top, 1,
                        "Акцентный цвет",
                        "Изменить цвет оформления",
                        true, mouseX, mouseY);

            } else if (tab == 1) {
                drawModule(ctx, left, top, 0,
                        "FPS",
                        "Показывает частоту кадров",
                        LiteVisualsClient.fpsEnabled,
                        mouseX, mouseY);

                drawModule(ctx, left, top, 1,
                        "Координаты",
                        "Показывает X, Y и Z",
                        LiteVisualsClient.coordsEnabled,
                        mouseX, mouseY);

                drawModule(ctx, left, top, 2,
                        "Направление",
                        "Показывает сторону света",
                        LiteVisualsClient.facingEnabled,
                        mouseX, mouseY);

                drawModule(ctx, left, top, 3,
                        "Цель под прицелом",
                        "Показывает имя сущности",
                        LiteVisualsClient.targetEnabled,
                        mouseX, mouseY);

                drawModule(ctx, left, top, 4,
                        "Весь HUD",
                        "Включает информационную панель",
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
        }

        text(ctx, "LITEVISUALS | ESC — закрыть",
                left + 120, bottom - 18, MUTED);
    }

    private void drawHitMarkerSettings(
            DrawContext ctx, int left, int top, int mx, int my) {
        text(ctx, "НАСТРОЙКИ HITMARKER",
                left + 120, top + 15, WHITE);
        text(ctx, "Нажимай ЛКМ по параметрам",
                left + 120, top + 29, MUTED);

        drawSetting(ctx, left, top, 0, "Цвет",
                getColorName(LiteVisualsClient.hitMarkerColor), mx, my);
        drawSetting(ctx, left, top, 1, "Размер",
                formatSize(LiteVisualsClient.hitMarkerSize), mx, my);
        drawSetting(ctx, left, top, 2, "Длительность",
                LiteVisualsClient.hitMarkerDurationMs + " мс", mx, my);

        int x = left + 117;
        int y = top + 158;
        int right = left + 337;

        boolean hover = mx >= x && mx <= right
                && my >= y && my <= y + 25;

        ctx.fill(x, y, right, y + 25,
                hover ? 0xFF40334F : 0xFF292338);

        text(ctx, "< НАЗАД", x + 9, y + 8,
                LiteVisualsClient.accentColor);
    }

    private void drawSetting(
            DrawContext ctx, int left, int top, int index,
            String name, String value, int mx, int my) {
        int x = left + 117;
        int y = top + 50 + index * 34;
        int right = left + 337;

        boolean hover = mx >= x && mx <= right
                && my >= y && my <= y + 29;

        ctx.fill(x, y, right, y + 29,
                hover ? 0xFF302B40 : 0xFF222131);

        text(ctx, name, x + 9, y + 4, WHITE);
        text(ctx, value, x + 9, y + 17,
                LiteVisualsClient.accentColor);
        text(ctx, ">", right - 17, y + 9, MUTED);
    }

    private void drawTab(
            DrawContext ctx, int left, int top, int id,
            String label, int mx, int my) {
        int y = top + 52 + id * 34;

        boolean selected = tab == id && !hitMarkerSettings;
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
        int left = width / 2 - 175;
        int top = height / 2 - 115;

        // ПКМ по HitMarker переключает его, даже если открыты настройки.
        if (!hitMarkerSettings && tab == 0 && button == 1) {
            int x = left + 117;
            int y = top + 50;

            if (inside(mouseX, mouseY, x, y, left + 337, y + 29)) {
                LiteVisualsClient.hitMarkerEnabled =
                        !LiteVisualsClient.hitMarkerEnabled;
                return true;
            }
        }

        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (hitMarkerSettings) {
            // Назад.
            int backY = top + 158;
            if (inside(mouseX, mouseY, left + 117, backY,
                    left + 337, backY + 25)) {
                hitMarkerSettings = false;
                return true;
            }

            // Цвет.
            for (int i = 0; i < COLORS.length; i++) {
                if (LiteVisualsClient.hitMarkerColor == COLORS[i]) {
                    LiteVisualsClient.hitMarkerColor =
                            COLORS[(i + 1) % COLORS.length];
                    return settingRowClicked(mouseX, mouseY, left, top, 0);
                }
            }

            if (inside(mouseX, mouseY, left + 117, top + 50,
                    left + 337, top + 79)) {
                LiteVisualsClient.hitMarkerColor = nextColor(
                        LiteVisualsClient.hitMarkerColor);
                return true;
            }

            // Размер.
            if (inside(mouseX, mouseY, left + 117, top + 84,
                    left + 337, top + 113)) {
                LiteVisualsClient.hitMarkerSize =
                        nextSize(LiteVisualsClient.hitMarkerSize);
                return true;
            }

            // Длительность.
            if (inside(mouseX, mouseY, left + 117, top + 118,
                    left + 337, top + 147)) {
                LiteVisualsClient.hitMarkerDurationMs =
                        nextDuration(LiteVisualsClient.hitMarkerDurationMs);
                return true;
            }

            return super.mouseClicked(mouseX, mouseY, button);
        }

        // Переключение вкладок.
        for (int i = 0; i < 3; i++) {
            int y = top + 52 + i * 34;

            if (inside(mouseX, mouseY, left + 7, y,
                    left + 99, y + 27)) {
                tab = i;
                return true;
            }
        }

        int count = tab == 0 ? 2 : tab == 1 ? 5 : 0;

        for (int i = 0; i < count; i++) {
            int x = left + 117;
            int y = top + 50 + i * 33;

            if (!inside(mouseX, mouseY, x, y, left + 337, y + 29)) {
                continue;
            }

            if (tab == 0) {
                if (i == 0) {
                    hitMarkerSettings = true;
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

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean settingRowClicked(
            double mouseX, double mouseY, int left, int top, int index) {
        return inside(mouseX, mouseY,
                left + 117, top + 50 + index * 34,
                left + 337, top + 79 + index * 34);
    }

    private int nextColor(int current) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i] == current) {
                return COLORS[(i + 1) % COLORS.length];
            }
        }
        return COLORS[0];
    }

    private float nextSize(float current) {
        for (int i = 0; i < SIZES.length; i++) {
            if (Math.abs(SIZES[i] - current) < 0.01f) {
                return SIZES[(i + 1) % SIZES.length];
            }
        }
        return SIZES[0];
    }

    private int nextDuration(int current) {
        for (int i = 0; i < DURATIONS.length; i++) {
            if (DURATIONS[i] == current) {
                return DURATIONS[(i + 1) % DURATIONS.length];
            }
        }
        return DURATIONS[0];
    }

    private String getColorName(int color) {
        if (color == 0xFFFF4040) return "Красный";
        if (color == 0xFFB56BFF) return "Фиолетовый";
        if (color == 0xFF62D9FF) return "Голубой";
        if (color == 0xFF65FF91) return "Зелёный";
        if (color == 0xFFFFFF55) return "Жёлтый";
        if (color == 0xFFFFFFFF) return "Белый";
        return "Свой";
    }

    private String formatSize(float size) {
        return String.format(java.util.Locale.ROOT, "%.0f px", size);
    }

    private boolean inside(
            double mx, double my,
            int x1, int y1, int x2, int y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
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
