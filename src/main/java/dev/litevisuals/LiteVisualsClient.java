
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
    public static boolean coordinatesEnabled = true;
    public static boolean directionEnabled = true;
    public static boolean targetInfoEnabled = true;

    public static boolean hitMarkerEnabled = true;
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
                drawContext.drawText(
                        client.textRenderer,
                        "FPS: " + client.getCurrentFps(),
                        x, y, white, true
                );
                y += 12;
            }

            if (coordinatesEnabled) {
                String coords = String.format(
                        "XYZ: %d %d %d",
                        MathHelper.floor(client.player.getX()),
                        MathHelper.floor(client.player.getY()),
                        MathHelper.floor(client.player.getZ())
                );

                drawContext.drawText(
                        client.textRenderer, coords, x, y, white, true
                );
                y += 12;
            }

            if (directionEnabled) {
                String direction = getDirection(client.player.getYaw());

                drawContext.drawText(
                        client.textRenderer,
                        "Facing: " + direction,
                        x, y, white, true
                );
                y += 12;
            }

            if (targetInfoEnabled) {
                Entity target = getTargetEntity();

                if (target instanceof LivingEntity livingTarget) {
                    String targetText = "Target: " + livingTarget.getName().getString()
                            + " | HP: " + String.format(
                                    "%.1f", livingTarget.getHealth()
                            );

                    drawContext.drawText(
                            client.textRenderer,
                            targetText,
                            x, y, 0xFFFF7777, true
                    );
                }
            }
        });
    }

    private static void drawHitMarker(DrawContext context) {
        if (!hitMarkerEnabled) {
            return;
        }

        long remaining = hitMarkerUntilMs - System.currentTimeMillis();

        if (remaining <= 0) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;

        float size = Math.max(2.0f, Math.min(hitMarkerSize, 30.0f));
        int color = hitMarkerColor;

        int thickness = Math.max(1, Math.round(size / 5.0f));
        int gap = Math.max(2, Math.round(size / 2.5f));

        // Four short diagonal strokes around the crosshair.
        drawStroke(context, centerX - gap - thickness, centerY - gap - thickness,
                centerX - gap + size / 3, centerY - gap + size / 3, thickness, color);

        drawStroke(context, centerX + gap - size / 3, centerY - gap + size / 3,
                centerX + gap + thickness, centerY - gap - thickness, thickness, color);

        drawStroke(context, centerX - gap - thickness, centerY + gap + thickness,
                centerX - gap + size / 3, centerY + gap - size / 3, thickness, color);

        drawStroke(context, centerX + gap - size / 3, centerY + gap - size / 3,
                centerX + gap + thickness, centerY + gap + thickness, thickness, color);
    }

    private static void drawStroke(
            DrawContext context,
            float x1, float y1,
            float x2, float y2,
            int thickness,
            int color
    ) {
        int left = Math.round(Math.min(x1, x2));
        int top = Math.round(Math.min(y1, y2));
        int right = Math.round(Math.max(x1, x2)) + thickness;
        int bottom = Math.round(Math.max(y1, y2)) + thickness;

        context.fill(left, top, right, bottom, color);
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
