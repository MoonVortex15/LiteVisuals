
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
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    private static final List<HitMark> HIT_MARKS = new ArrayList<>();

    private static class HitMark {
        final Vec3d position;
        final long expiresAt;
        final int color;
        final float size;

        HitMark(Vec3d position, long expiresAt, int color, float size) {
            this.position = position;
            this.expiresAt = expiresAt;
            this.color = color;
            this.size = size;
        }
    }

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

            long now = System.currentTimeMillis();
            HIT_MARKS.removeIf(mark -> now >= mark.expiresAt);

            if (!hitMarkerEnabled) {
                HIT_MARKS.clear();
            }
        });

        AttackEntityCallback.EVENT.register(
                (player, world, hand, entity, hitResult) -> {
                    if (world.isClient
                            && hitMarkerEnabled
                            && entity instanceof LivingEntity living
                            && living.isAlive()) {

                        Vec3d hitPosition = hitResult != null
                                ? hitResult.getPos()
                                : living.getBoundingBox().getCenter();

                        long now = System.currentTimeMillis();
                        int duration = Math.max(50, hitMarkerDurationMs);

                        HIT_MARKS.add(new HitMark(
                                hitPosition,
                                now + duration,
                                hitMarkerColor,
                                MathHelper.clamp(hitMarkerSize, 2.0f, 30.0f)
                        ));

                        if (HIT_MARKS.size() > 30) {
                            HIT_MARKS.remove(0);
                        }
                    }

                    return ActionResult.PASS;
                }
        );

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null || client.world == null) {
                return;
            }

            drawWorldHitMarkers(drawContext);

            if (hudEnabled) {
                drawInfoHud(drawContext, client);
            }
        });
    }

    private static void drawInfoHud(
            DrawContext ctx, MinecraftClient client) {

        int x = 8;
        int y = 8;
        int panelWidth = 132;
        int lineHeight = 12;
        int rows = 0;

        if (fpsEnabled) rows++;
        if (coordsEnabled) rows++;
        if (facingEnabled) rows++;
        if (targetEnabled) rows++;

        if (rows == 0) {
            return;
        }

        int panelHeight = 23 + rows * lineHeight;
        int accent = accentColor;

        ctx.fill(x, y, x + panelWidth, y + panelHeight, 0xD910101D);
        ctx.fill(x, y, x + panelWidth, y + 2, accent);
        ctx.fill(x, y + 2, x + 2, y + panelHeight, accent);

        ctx.drawTextWithShadow(
                client.textRenderer,
                "LITEVISUALS",
                x + 8, y + 7, accent
        );

        ctx.drawTextWithShadow(
                client.textRenderer,
                "CLIENT",
                x + panelWidth - 37, y + 7, 0xFFAAA8C2
        );

        int textY = y + 22;
        int textX = x + 8;

        if (fpsEnabled) {
            drawHudRow(ctx, "FPS", String.valueOf(client.getCurrentFps()),
                    textX, textY, 0xFFF0E8FF);
            textY += lineHeight;
        }

        if (coordsEnabled) {
            String coords = String.format(
                    java.util.Locale.ROOT,
                    "%d %d %d",
                    MathHelper.floor(client.player.getX()),
                    MathHelper.floor(client.player.getY()),
                    MathHelper.floor(client.player.getZ())
            );

            drawHudRow(ctx, "XYZ", coords, textX, textY, 0xFFF0E8FF);
            textY += lineHeight;
        }

        if (facingEnabled) {
            drawHudRow(ctx, "DIR", getDirection(client.player.getYaw()),
                    textX, textY, 0xFFF0E8FF);
            textY += lineHeight;
        }

        if (targetEnabled) {
            Entity target = getTargetEntity();

            if (target instanceof LivingEntity living) {
                String name = living.getName().getString();
                if (name.length() > 11) {
                    name = name.substring(0, 11) + "...";
                }

                drawHudRow(ctx, "TARGET", name, textX, textY, 0xFFFF9A9A);
                textY += lineHeight;

                float health = Math.max(0, living.getHealth());
                float maxHealth = Math.max(1, living.getMaxHealth());
                int barX = textX;
                int barY = textY - 2;
                int barWidth = panelWidth - 16;
                int filled = Math.round(barWidth * Math.min(1, health / maxHealth));

                ctx.fill(barX, barY, barX + barWidth, barY + 3, 0xFF393344);
                if (filled > 0) {
                    ctx.fill(barX, barY, barX + filled, barY + 3, 0xFFFF6B8A);
                }
            } else {
                drawHudRow(ctx, "TARGET", "None", textX, textY, 0xFFAAA8C2);
            }
        }
    }

    private static void drawHudRow(
            DrawContext ctx, String label, String value,
            int x, int y, int valueColor) {

        ctx.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                label, x, y, 0xFFAAA8C2
        );

        int valueX = x + 43;

        ctx.drawTextWithShadow(
                MinecraftClient.getInstance().textRenderer,
                value, valueX, y, valueColor
        );
    }

    private static void drawWorldHitMarkers(DrawContext ctx) {
        if (!hitMarkerEnabled || HIT_MARKS.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();

        if (client.gameRenderer == null || client.player == null) {
            return;
        }

        long now = System.currentTimeMillis();
        Iterator<HitMark> iterator = HIT_MARKS.iterator();

        while (iterator.hasNext()) {
            HitMark mark = iterator.next();

            if (now >= mark.expiresAt) {
                iterator.remove();
                continue;
            }

            Vec3d cameraPos = client.gameRenderer.getCamera().getPos();
            Vec3d relative = mark.position.subtract(cameraPos);

            double yaw = Math.toRadians(client.gameRenderer.getCamera().getYaw());
            double pitch = Math.toRadians(client.gameRenderer.getCamera().getPitch());

            double sinYaw = Math.sin(yaw);
            double cosYaw = Math.cos(yaw);
            double sinPitch = Math.sin(pitch);
            double cosPitch = Math.cos(pitch);

            double cameraX = relative.x * cosYaw + relative.z * sinYaw;
            double cameraY = relative.x * sinYaw * sinPitch
                    - relative.y * cosPitch
                    - relative.z * cosYaw * sinPitch;
            double cameraZ = -relative.x * sinYaw * cosPitch
                    - relative.y * sinPitch
                    + relative.z * cosYaw * cosPitch;

            if (cameraZ >= -0.05) {
                continue;
            }

            double depth = -cameraZ;
            double fovRadians = Math.toRadians(70.0);
            double focalLength = client.getWindow().getScaledHeight()
                    / (2.0 * Math.tan(fovRadians / 2.0));

            int screenX = (int) Math.round(
                    client.getWindow().getScaledWidth() / 2.0
                            + cameraX * focalLength / depth
            );

            int screenY = (int) Math.round(
                    client.getWindow().getScaledHeight() / 2.0
                            + cameraY * focalLength / depth
            );

            if (screenX < -50 || screenX > client.getWindow().getScaledWidth() + 50
                    || screenY < -50
                    || screenY > client.getWindow().getScaledHeight() + 50) {
                continue;
            }

            float remaining = (float) (mark.expiresAt - now)
                    / Math.max(1, hitMarkerDurationMs);
            int alpha = MathHelper.clamp((int) (remaining * 255), 0, 255);
            int color = (mark.color & 0x00FFFFFF) | (alpha << 24);

            int size = MathHelper.clamp(Math.round(mark.size), 2, 30);
            int gap = Math.max(2, size / 3);
            int thickness = Math.max(1, size / 6);

            // Четыре коротких штриха вокруг точки попадания.
            ctx.fill(screenX - gap - thickness, screenY - gap - thickness,
                    screenX - gap + thickness, screenY - gap + thickness, color);

            ctx.fill(screenX + gap - thickness, screenY - gap - thickness,
                    screenX + gap + thickness, screenY - gap + thickness, color);

            ctx.fill(screenX - gap - thickness, screenY + gap - thickness,
                    screenX - gap + thickness, screenY + gap + thickness, color);

            ctx.fill(screenX + gap - thickness, screenY + gap - thickness,
                    screenX + gap + thickness, screenY + gap + thickness, color);
        }
    }

    private static String getDirection(float yaw) {
        float normalized = MathHelper.wrapDegrees(yaw);

        if (normalized >= -45 && normalized < 45) return "S";
        if (normalized >= 45 && normalized < 135) return "W";
        if (normalized >= -135 && normalized < -45) return "E";
        return "N";
    }

    public static Entity getTargetEntity() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.crosshairTarget instanceof EntityHitResult hit) {
            return hit.getEntity();
        }

        return null;
    }

    public static void clearHitMarker() {
        HIT_MARKS.clear();
    }
}
