
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
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class LiteVisualsClient implements ClientModInitializer {
    public static KeyBinding openMenuKey;
    public static KeyBinding toggleHudKey;

    public static boolean hudEnabled = true;
    public static boolean fpsEnabled = true;
    public static boolean coordsEnabled = true;
    public static boolean facingEnabled = true;
    public static boolean targetEnabled = true;

    public static boolean hitMarkerEnabled = true;

    public static int accentColor = 0xFFB56BFF;
    public static int hitMarkerColor = 0xFFFF4040;
    public static float hitMarkerSize = 8.0f;
    public static int hitMarkerDurationMs = 450;

    private static long hitMarkerUntilMs = 0L;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.litevisuals.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.litevisuals"
        ));

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.litevisuals.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.litevisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new LiteVisualsScreen());
                }
            }

            while (toggleHudKey.wasPressed()) {
                hudEnabled = !hudEnabled;
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && hitMarkerEnabled && entity instanceof LivingEntity) {
                hitMarkerUntilMs = System.currentTimeMillis()
                        + Math.max(50, hitMarkerDurationMs);
            }

            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null || client.world == null) {
                return;
            }

            drawHitMarker(drawContext);

            if (!hudEnabled) {
                return;
            }

            int x = 8;
            int y = 8;
            int white = 0xFFFFFFFF;

            if (fpsEnabled) {
                drawText(drawContext, "FPS: " + client.getCurrentFps(),
                        x, y, white);
                y += 12;
            }

            if (coordsEnabled) {
                String coordinates = String.format(
                        java.util.Locale.ROOT,
                        "XYZ: %d %d %d",
                        MathHelper.floor(client.player.getX()),
                        MathHelper.floor(client.player.getY()),
                        MathHelper.floor(client.player.getZ())
                );

                drawText(drawContext, coordinates, x, y, white);
                y += 12;
            }

            if (facingEnabled) {
                drawText(drawContext,
                        "Facing: " + getDirection(client.player.getYaw()),
                        x, y, white);
                y += 12;
            }

            if (targetEnabled) {
                Entity target = getTargetEntity();

                if (target instanceof LivingEntity livingTarget) {
                    String targetText = "Target: "
                            + livingTarget.getName().getString()
                            + " | HP: "
                            + String.format(
                                    java.util.Locale.ROOT,
                                    "%.1f",
                                    livingTarget.getHealth()
                            );

                    drawText(drawContext, targetText, x, y, 0xFFFF7777);
                }
            }
        });
    }

    private static void drawText(
            DrawContext context, String value, int x, int y, int color) {
        context.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                value, x, y, color
        );
    }

    private static void drawHitMarker(DrawContext context) {
        if (!hitMarkerEnabled) {
            return;
        }

        if (System.currentTimeMillis() >= hitMarkerUntilMs) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;

        int size = MathHelper.clamp(
                Math.round(hitMarkerSize), 2, 30
        );
        int gap = Math.max(3, size / 2);
        int thickness = Math.max(1, size / 5);
        int color = hitMarkerColor;

        // Верхний левый штрих.
        context.fill(
                centerX - gap - size / 2,
                centerY - gap - size / 2,
                centerX - gap + thickness,
                centerY - gap + thickness,
                color
        );

        // Верхний правый штрих.
        context.fill(
                centerX + gap - thickness,
                centerY - gap - size / 2,
                centerX + gap + size / 2,
                centerY - gap + thickness,
                color
        );

        // Нижний левый штрих.
        context.fill(
                centerX - gap - size / 2,
                centerY + gap - thickness,
                centerX - gap + thickness,
                centerY + gap + size / 2,
                color
        );

        // Нижний правый штрих.
        context.fill(
                centerX + gap - thickness,
                centerY + gap - thickness,
                centerX + gap + size / 2,
                centerY + gap + size / 2,
                color
        );
    }

    private static String getDirection(float yaw) {
        float normalized = MathHelper.wrapDegrees(yaw);

        if (normalized >= -45 && normalized < 45) {
            return "South";
        } else if (normalized >= 45 && normalized < 135) {
            return "West";
        } else if (normalized >= -135 && normalized < -45) {
            return "East";
        } else {
            return "North";
        }
    }

    public static Entity getTargetEntity() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.crosshairTarget instanceof EntityHitResult hit) {
            return hit.getEntity();
        }

        return null;
    }

    public static void clearHitMarker() {
        hitMarkerUntilMs = 0L;
    }
}
