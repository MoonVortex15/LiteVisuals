
package dev.litevisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
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
    public static boolean coordinatesEnabled = true;
    public static boolean directionEnabled = true;
    public static boolean targetInfoEnabled = true;

    public static boolean hitMarkerEnabled = true;
    public static int hitMarkerColor = 0xFFFF4040;
    public static float hitMarkerSize = 0.14f;
    public static int hitMarkerDurationMs = 450;

    private static long hitMarkerUntilMs = 0L;

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

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient && hitMarkerEnabled && entity instanceof LivingEntity) {
                Vec3d position;

                if (hitResult instanceof EntityHitResult entityHit) {
                    position = entityHit.getPos();
                } else {
                    position = entity.getBoundingBox().getCenter();
                }

                long now = System.currentTimeMillis();
                hitMarkerUntilMs = now + Math.max(50, hitMarkerDurationMs);

                HIT_MARKS.add(new HitMark(
                        position,
                        now + Math.max(50, hitMarkerDurationMs),
                        hitMarkerColor,
                        Math.max(0.02f, hitMarkerSize)
                ));

                if (HIT_MARKS.size() > 30) {
                    HIT_MARKS.remove(0);
                }
            }

            return ActionResult.PASS;
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.world == null || client.player == null || !hitMarkerEnabled) {
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

                // HitMarker positions are stored in world coordinates.
                // The renderer is handled by the screen overlay below.
            }
        });
    }

    public static boolean isHitMarkerVisible() {
        return hitMarkerEnabled && System.currentTimeMillis() < hitMarkerUntilMs;
    }

    public static long getHitMarkerRemainingMs() {
        return Math.max(0L, hitMarkerUntilMs - System.currentTimeMillis());
    }

    public static void clearHitMarkers() {
        HIT_MARKS.clear();
        hitMarkerUntilMs = 0L;
    }

    public static boolean isTargetingLivingEntity() {
        MinecraftClient client = MinecraftClient.getInstance();
        return client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity;
    }

    public static Entity getTargetEntity() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.crosshairTarget instanceof EntityHitResult hit) {
            return hit.getEntity();
        }

        return null;
    }
}
