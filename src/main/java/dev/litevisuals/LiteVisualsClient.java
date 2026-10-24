
package dev.litevisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class LiteVisualsClient implements ClientModInitializer {

    public static boolean hudEnabled = true;
    public static boolean fpsEnabled = true;
    public static boolean coordsEnabled = true;
    public static boolean facingEnabled = true;
    public static boolean targetEnabled = true;
    public static boolean hitMarkerEnabled = true;

    public static int accentColor = 0xFFB56BFF;

    // Настройки HitMarker
    public static int hitMarkerColor = 0xFFFF4040;
    public static float hitMarkerSize = 0.12f;
    public static int hitMarkerDurationMs = 450;

    private static KeyBinding toggleHudKey;
    private static KeyBinding openMenuKey;

    private static Entity pendingTarget;
    private static Vec3d pendingPosition;
    private static int previousHurtTime;
    private static long pendingTime;

    private static final List<HitMark> HIT_MARKS = new ArrayList<>();

    private static class HitMark {
        final Vec3d position;
        final long createdAt;

        HitMark(Vec3d position, long createdAt) {
            this.position = position;
            this.createdAt = createdAt;
        }
    }

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

            long now = System.currentTimeMillis();

            HIT_MARKS.removeIf(mark ->
                now - mark.createdAt >= Math.max(50, hitMarkerDurationMs)
            );

            if (client.world == null || client.player == null) {
                HIT_MARKS.clear();
                pendingTarget = null;
                pendingPosition = null;
                return;
            }

            if (pendingTarget != null) {
                if (now - pendingTime > 800 || !pendingTarget.isAlive()) {
                    pendingTarget = null;
                    pendingPosition = null;
                } else if (pendingTarget instanceof LivingEntity living
                        && living.hurtTime > previousHurtTime) {

                    if (hitMarkerEnabled && pendingPosition != null) {
                        HIT_MARKS.add(new HitMark(pendingPosition, now));
                    }

                    pendingTarget = null;
                    pendingPosition = null;
                }
            }
        });

        AttackEntityCallback.EVENT.register(
            (player, world, hand, entity, hitResult) -> {
                MinecraftClient client = MinecraftClient.getInstance();

                if (world.isClient
                        && player == client.player
                        && hitMarkerEnabled
                        && entity instanceof LivingEntity living
                        && hitResult != null) {

                    pendingTarget = living;
                    pendingPosition = hitResult.getPos();
                    previousHurtTime = living.hurtTime;
                    pendingTime = System.currentTimeMillis();
                }

                return ActionResult.PASS;
            }
        );

        HudRenderCallback.EVENT.register(
            (context, tickCounter) -> renderHud(context)
        );

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (!hitMarkerEnabled
                    || client.world == null
                    || client.player == null
                    || HIT_MARKS.isEmpty()) {
                return;
            }

            var matrices = context.matrixStack();
            var consumers = context.consumers();

            if (matrices == null || consumers == null) {
                return;
            }

            Vec3d camera = context.camera().getPos();

            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);

            VertexConsumer lines =
                consumers.getBuffer(RenderLayer.getLines());

            Matrix4f matrix = matrices.peek().getPositionMatrix();
            long now = System.currentTimeMillis();

            for (HitMark mark : HIT_MARKS) {
                float progress = (now - mark.createdAt)
                    / (float) Math.max(50, hitMarkerDurationMs);

                if (progress >= 1.0f) {
                    continue;
                }

                Vec3d p = mark.position;
                float size = Math.max(0.02f, hitMarkerSize);

                int color = hitMarkerColor;
                float red = ((color >> 16) & 255) / 255.0f;
                float green = ((color >> 8) & 255) / 255.0f;
                float blue = (color & 255) / 255.0f;
                float alpha = 1.0f - progress;

                drawLine(lines, matrix,
                    p.add(-size, 0, 0), p.add(size, 0, 0),
                    red, green, blue, alpha);

                drawLine(lines, matrix,
                    p.add(0, -size, 0), p.add(0, size, 0),
                    red, green, blue, alpha);

                drawLine(lines, matrix,
                    p.add(0, 0, -size), p.add(0, 0, size),
                    red, green, blue, alpha);
            }

            matrices.pop();
        });
    }

    private static void drawLine(
            VertexConsumer vertices,
            Matrix4f matrix,
            Vec3d from,
            Vec3d to,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        vertices.vertex(matrix,
                (float) from.x, (float) from.y, (float) from.z)
            .color(red, green, blue, alpha)
            .normal(0.0f, 1.0f, 0.0f);

        vertices.vertex(matrix,
                (float) to.x, (float) to.y, (float) to.z)
            .color(red, green, blue, alpha)
            .normal(0.0f, 1.0f, 0.0f);
    }

    private static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player == null || client.world == null
                || client.options.hudHidden || !hudEnabled) {
            return;
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

        context.fill(x - 5, y - 5, x + 160, y + panelHeight,
            0xC9101020);
        context.fill(x - 5, y - 5, x + 160, y - 2, accentColor);

        context.drawText(client.textRenderer, "LITEVISUALS",
            x, y, accentColor, false);
        y += 14;

        if (fpsEnabled) {
            context.drawText(client.textRenderer,
                "FPS: " + client.getCurrentFps(),
                x, y, white, false);
            y += 12;
        }

        if (coordsEnabled) {
            context.drawText(client.textRenderer,
                String.format(Locale.ROOT,
                    "Коорд.: %.1f / %.1f / %.1f",
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
