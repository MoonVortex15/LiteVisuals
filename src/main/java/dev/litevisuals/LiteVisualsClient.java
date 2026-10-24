package dev.litevisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class LiteVisualsClient implements ClientModInitializer {
    private static boolean hudEnabled = true;
    private static long hitMarkerUntilMs = 0L;
    private static KeyBinding toggleHudKey;

    @Override
    public void onInitializeClient() {
        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.litevisuals.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.litevisuals"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleHudKey.wasPressed()) {
                hudEnabled = !hudEnabled;
                if (client.player != null) {
                    client.player.sendMessage(
                            Text.literal("LiteVisuals HUD: " + (hudEnabled ? "ON" : "OFF")),
                            true
                    );
                }
            }
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) {
                hitMarkerUntilMs = System.currentTimeMillis() + 180L;
            }
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> renderHud(drawContext));
    }

    private static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }

        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        // Minimal hit marker. Drawn only briefly after an attack attempt.
        if (System.currentTimeMillis() < hitMarkerUntilMs) {
            int cx = width / 2;
            int cy = height / 2;
            int color = 0xFFFFFFFF;
            context.drawText(client.textRenderer, "+", cx - 3, cy - 4, color, false);
        }

        if (!hudEnabled) return;

        int x = 6;
        int y = 6;
        int color = 0xFFE6F7FF;
        context.drawText(client.textRenderer, "LiteVisuals", x, y, 0xFF65D6FF, false);
        y += 11;
        context.drawText(client.textRenderer, "FPS: " + MinecraftClient.getInstance().getCurrentFps(), x, y, color, false);
        y += 11;

        double px = client.player.getX();
        double py = client.player.getY();
        double pz = client.player.getZ();
        context.drawText(client.textRenderer,
                String.format(java.util.Locale.ROOT, "XYZ: %.1f / %.1f / %.1f", px, py, pz),
                x, y, color, false);
        y += 11;

        context.drawText(client.textRenderer, "Facing: " + client.player.getHorizontalFacing().asString(), x, y, color, false);
        y += 11;

        Entity target = client.targetedEntity;
        if (target != null) {
            context.drawText(client.textRenderer, "Target: " + target.getName().getString(), x, y, 0xFFFFD166, false);
        }
    }
}
