
package dev.litevisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public class LiteVisualsClient implements ClientModInitializer {
    public static boolean hudEnabled = true;
    public static boolean fpsEnabled = true;
    public static boolean coordsEnabled = true;
    public static boolean facingEnabled = true;
    public static boolean targetEnabled = true;
    public static boolean hitMarkerEnabled = true;

    public static int accentColor = 0xFFB56BFF;
    public static long hitMarkerUntilMs = 0L;

    private static KeyBinding toggleHudKey;
    private static KeyBinding openMenuKey;

    @Override
    public void onInitializeClient() {
        toggleHudKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.litevisuals.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.litevisuals"
            )
        );

        openMenuKey = KeyBindingHelper.registerKeyBinding(
            new KeyBinding(
                "key.litevisuals.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.litevisuals"
            )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHudKey.wasPressed()) {
                hudEnabled = !hudEnabled;
            }

            while (openMenuKey.wasPressed()) {
                if (client.currentScreen instanceof LiteVisualsScreen) {
                    client.setScreen(null);
                } else if (client.currentScreen == null) {
                    client.setScreen(new LiteVisualsScreen());
                }
            }
        });

        AttackEntityCallback.EVENT.register(
            (player, world, hand, entity, hitResult) -> {
                if (world.isClient && hitMarkerEnabled) {
                    hitMarkerUntilMs = System.currentTimeMillis() + 180L;
                }
                return ActionResult.PASS;
            }
        );

        HudRenderCallback.EVENT.register(
            (context, tickCounter) -> renderHud(context)
        );
    }

    private static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null
                || client.options.hudHidden || !hudEnabled) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        if (hitMarkerEnabled
                && System.currentTimeMillis() < hitMarkerUntilMs) {
            int cx = width / 2;
            int cy = height / 2;
            context.fill(cx - 7, cy - 1, cx - 3, cy + 1, accentColor);
            context.fill(cx + 3, cy - 1, cx + 7, cy + 1, accentColor);
            context.fill(cx - 1, cy - 7, cx + 1, cy - 3, accentColor);
            context.fill(cx - 1, cy + 3, cx + 1, cy + 7, accentColor);
        }

        int x = 8;
        int y = 8;
        int white = 0xFFF0E8FF;
        int lines = (fpsEnabled ? 1 : 0)
                  + (coordsEnabled ? 1 : 0)
                  + (facingEnabled ? 1 : 0)
                  + (targetEnabled && client.targetedEntity != null ? 1 : 0);

        if (lines == 0) return;

        int panelHeight = 20 + lines * 12;
        context.fill(x - 5, y - 5, x + 160, y + panelHeight, 0xC9101020);
        context.fill(x - 5, y - 5, x + 160, y - 2, accentColor);

        context.drawText(client.textRenderer, "LITEVISUALS",
            x, y, accentColor, false);
        y += 14;

        if (fpsEnabled) {
            context.drawText(client.textRenderer,
                "FPS: " + client.getCurrentFps(), x, y, white, false);
            y += 12;
        }

        if (coordsEnabled) {
            context.drawText(client.textRenderer,
                String.format(Locale.ROOT, "Коорд.: %.1f / %.1f / %.1f",
                    client.player.getX(),
                    client.player.getY(),
                    client.player.getZ()),
                x, y, white, false);
            y += 12;
        }

        if (facingEnabled) {
            context.drawText(client.textRenderer,
                "Направление: " +
                    client.player.getHorizontalFacing().asString(),
                x, y, white, false);
            y += 12;
        }

        Entity target = client.targetedEntity;
        if (targetEnabled && target != null) {
            context.drawText(client.textRenderer,
                "Цель: " + target.getName().getString(),
                x, y, 0xFFFFD166, false);
        }
    }
}
