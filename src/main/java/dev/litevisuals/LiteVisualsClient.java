package dev.litevisuals;

import com.google.gson.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import java.nio.file.*;
import java.util.*;
import java.io.*;

public class LiteVisualsClient implements ClientModInitializer {
 public static KeyBinding openMenuKey, toggleHudKey;
 public static boolean hudEnabled=true, fpsEnabled=true, coordsEnabled=true, facingEnabled=true, targetEnabled=true;
 public static boolean arrayListEnabled=true, keystrokesEnabled=true, cpsEnabled=true, healthEnabled=true, armorEnabled=true;
 public static boolean entityEspEnabled=false, nametagsEnabled=false, hitboxEnabled=false, fullbrightEnabled=false, noFogEnabled=false, weatherVisualsEnabled=false;
 public static int accentColor=0xFFB56BFF;
 public static int hudX=8, hudY=8, hudScale=100, keystrokesX=8, keystrokesY=0, cpsX=0, cpsY=0;
 public static boolean compactHud=false;
 private static final Gson GSON=new GsonBuilder().setPrettyPrinting().create();
 private static Path configPath;
 private static double oldGamma=1.0;
 private static boolean gammaSaved=false;
 private static final Deque<Long> leftClicks=new ArrayDeque<>(), rightClicks=new ArrayDeque<>();
 private static boolean lastLeft=false,lastRight=false;

 @Override public void onInitializeClient() {
  configPath=FabricLoader.getInstance().getConfigDir().resolve("litevisuals.json"); loadConfig();
  openMenuKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.litevisuals.open_menu",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_RIGHT_SHIFT,"category.litevisuals"));
  toggleHudKey=KeyBindingHelper.registerKeyBinding(new KeyBinding("key.litevisuals.toggle_hud",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_H,"category.litevisuals"));
  ClientTickEvents.END_CLIENT_TICK.register(c->{
   while(openMenuKey.wasPressed()) if(c.currentScreen==null)c.setScreen(new LiteVisualsScreen());
   while(toggleHudKey.wasPressed()){hudEnabled=!hudEnabled;saveConfig();}
   if(c.player!=null && fullbrightEnabled){if(!gammaSaved){oldGamma=c.options.getGamma().getValue();gammaSaved=true;} c.options.getGamma().setValue(16.0);}
   else if(gammaSaved){c.options.getGamma().setValue(oldGamma);gammaSaved=false;}
  });
  HudRenderCallback.EVENT.register((ctx,tick)->{MinecraftClient c=MinecraftClient.getInstance();if(c.player!=null&&c.world!=null&&hudEnabled)drawHud(ctx,c);});
 }
 private static void drawHud(DrawContext d,MinecraftClient c){
  int x=hudX,y=hudY,line=compactHud?10:12,w=compactHud?132:150;
  int rows=(fpsEnabled?1:0)+(coordsEnabled?1:0)+(facingEnabled?1:0)+(targetEnabled?1:0);
  if(rows>0){int h=22+rows*line+8;d.fill(x,y,x+w,y+h,0xD910101D);d.fill(x,y,x+w,y+2,accentColor);d.fill(x,y+2,x+2,y+h,accentColor);d.drawTextWithShadow(c.textRenderer,"LITEVISUALS",x+8,y+7,accentColor);d.drawTextWithShadow(c.textRenderer,"CLIENT",x+w-37,y+7,0xFFAAA8C2);int ty=y+22;
   if(fpsEnabled){row(d,"FPS",""+c.getCurrentFps(),x+8,ty);ty+=line;}
   if(coordsEnabled){row(d,"XYZ",String.format(Locale.ROOT,"%d %d %d",MathHelper.floor(c.player.getX()),MathHelper.floor(c.player.getY()),MathHelper.floor(c.player.getZ())),x+8,ty);ty+=line;}
   if(facingEnabled){row(d,"DIR",direction(c.player.getYaw()),x+8,ty);ty+=line;}
   if(targetEnabled){Entity e=getTargetEntity();if(e instanceof LivingEntity l){String n=l.getName().getString();if(n.length()>15)n=n.substring(0,15)+"...";row(d,"TARGET",n,x+8,ty);ty+=line;
     if(healthEnabled){int bw=w-16,fill=Math.round(bw*MathHelper.clamp(l.getHealth()/Math.max(1,l.getMaxHealth()),0,1));d.fill(x+8,ty-1,x+8+bw,ty+3,0xFF393344);if(fill>0)d.fill(x+8,ty-1,x+8+fill,ty+3,0xFFFF6B8A);ty+=5;}
     if(armorEnabled&&l instanceof PlayerEntity p)row(d,"ARMOR",""+p.getArmor(),x+8,ty);
    }else row(d,"TARGET","None",x+8,ty);}
  }
  if(arrayListEnabled){String[] a={cpsEnabled?"CPS":null,keystrokesEnabled?"Keystrokes":null,targetEnabled?"Target HUD":null,facingEnabled?"Direction":null,coordsEnabled?"Coordinates":null,fpsEnabled?"FPS":null,fullbrightEnabled?"Fullbright":null,noFogEnabled?"No Fog*":null,entityEspEnabled?"Entity ESP*":null,nametagsEnabled?"Nametags*":null,hitboxEnabled?"Hitbox*":null};int yy=8;for(String s:a)if(s!=null){int tw=c.textRenderer.getWidth(s),sx=c.getWindow().getScaledWidth()-tw-10;d.fill(sx-3,yy-2,sx+tw+5,yy+10,0xB910101D);d.fill(sx+tw+3,yy-2,sx+tw+5,yy+10,accentColor);d.drawTextWithShadow(c.textRenderer,s,sx,yy,0xFFF0E8FF);yy+=13;}}
  if(keystrokesEnabled){long win=c.getWindow().getHandle();int bx=keystrokesX,by=keystrokesY==0?c.getWindow().getScaledHeight()-61:keystrokesY;key(d,c,bx+20,by,"W",win,GLFW.GLFW_KEY_W);key(d,c,bx,by+20,"A",win,GLFW.GLFW_KEY_A);key(d,c,bx+20,by+20,"S",win,GLFW.GLFW_KEY_S);key(d,c,bx+40,by+20,"D",win,GLFW.GLFW_KEY_D);key(d,c,bx+64,by+20,"SP",win,GLFW.GLFW_KEY_SPACE);}
  if(cpsEnabled){long now=System.currentTimeMillis();trim(leftClicks,now);trim(rightClicks,now);long win=c.getWindow().getHandle();boolean l=GLFW.glfwGetMouseButton(win,0)==1,r=GLFW.glfwGetMouseButton(win,1)==1;if(l&&!lastLeft)leftClicks.addLast(now);if(r&&!lastRight)rightClicks.addLast(now);lastLeft=l;lastRight=r;String s="CPS "+leftClicks.size()+" | "+rightClicks.size();int sx=c.getWindow().getScaledWidth()-c.textRenderer.getWidth(s)-12+cpsX,sy=c.getWindow().getScaledHeight()-20+cpsY;d.fill(sx-4,sy-3,sx+c.textRenderer.getWidth(s)+5,sy+11,0xC910101D);d.drawTextWithShadow(c.textRenderer,s,sx,sy,accentColor);}
 }
 private static void row(DrawContext d,String a,String b,int x,int y){d.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,a,x,y,0xFFAAA8C2);d.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,b,x+48,y,0xFFF0E8FF);}
 private static void key(DrawContext d,MinecraftClient c,int x,int y,String s,long win,int code){boolean down=InputUtil.isKeyPressed(win,code);d.fill(x,y,x+18,y+18,down?0xFF68408D:0xC910101D);d.drawCenteredTextWithShadow(c.textRenderer,s,x+9,y+5,-1);}
 private static void trim(Deque<Long> q,long n){while(!q.isEmpty()&&n-q.peekFirst()>1000)q.removeFirst();}
 private static String direction(float yaw){int i=Math.floorMod(Math.round(MathHelper.floor(yaw/45f+0.5f)),8);return new String[]{"S","SW","W","NW","N","NE","E","SE"}[i];}
 public static Entity getTargetEntity(){MinecraftClient c=MinecraftClient.getInstance();return c.crosshairTarget instanceof EntityHitResult h?h.getEntity():null;}
 public static void saveConfig(){if(configPath==null)return;try{JsonObject j=new JsonObject();String[] keys={"hudEnabled","fpsEnabled","coordsEnabled","facingEnabled","targetEnabled","arrayListEnabled","keystrokesEnabled","cpsEnabled","healthEnabled","armorEnabled","entityEspEnabled","nametagsEnabled","hitboxEnabled","fullbrightEnabled","noFogEnabled","weatherVisualsEnabled","compactHud"};boolean[] vals={hudEnabled,fpsEnabled,coordsEnabled,facingEnabled,targetEnabled,arrayListEnabled,keystrokesEnabled,cpsEnabled,healthEnabled,armorEnabled,entityEspEnabled,nametagsEnabled,hitboxEnabled,fullbrightEnabled,noFogEnabled,weatherVisualsEnabled,compactHud};for(int i=0;i<keys.length;i++)j.addProperty(keys[i],vals[i]);j.addProperty("accentColor",accentColor);j.addProperty("hudX",hudX);j.addProperty("hudY",hudY);j.addProperty("keystrokesX",keystrokesX);j.addProperty("keystrokesY",keystrokesY);j.addProperty("cpsX",cpsX);j.addProperty("cpsY",cpsY);Files.createDirectories(configPath.getParent());try(Writer out=Files.newBufferedWriter(configPath)){GSON.toJson(j,out);}}catch(Exception e){System.err.println("[LiteVisuals] Save failed: "+e.getMessage());}}
 private static void loadConfig(){if(configPath==null||!Files.exists(configPath))return;try(Reader in=Files.newBufferedReader(configPath)){JsonObject j=GSON.fromJson(in,JsonObject.class);if(j==null)return;hudEnabled=b(j,"hudEnabled",hudEnabled);fpsEnabled=b(j,"fpsEnabled",fpsEnabled);coordsEnabled=b(j,"coordsEnabled",coordsEnabled);facingEnabled=b(j,"facingEnabled",facingEnabled);targetEnabled=b(j,"targetEnabled",targetEnabled);arrayListEnabled=b(j,"arrayListEnabled",arrayListEnabled);keystrokesEnabled=b(j,"keystrokesEnabled",keystrokesEnabled);cpsEnabled=b(j,"cpsEnabled",cpsEnabled);healthEnabled=b(j,"healthEnabled",healthEnabled);armorEnabled=b(j,"armorEnabled",armorEnabled);entityEspEnabled=b(j,"entityEspEnabled",entityEspEnabled);nametagsEnabled=b(j,"nametagsEnabled",nametagsEnabled);hitboxEnabled=b(j,"hitboxEnabled",hitboxEnabled);fullbrightEnabled=b(j,"fullbrightEnabled",fullbrightEnabled);noFogEnabled=b(j,"noFogEnabled",noFogEnabled);weatherVisualsEnabled=b(j,"weatherVisualsEnabled",weatherVisualsEnabled);compactHud=b(j,"compactHud",compactHud);accentColor=i(j,"accentColor",accentColor);hudX=i(j,"hudX",hudX);hudY=i(j,"hudY",hudY);keystrokesX=i(j,"keystrokesX",keystrokesX);keystrokesY=i(j,"keystrokesY",keystrokesY);cpsX=i(j,"cpsX",cpsX);cpsY=i(j,"cpsY",cpsY);}catch(Exception e){System.err.println("[LiteVisuals] Load failed: "+e.getMessage());}}
 private static boolean b(JsonObject j,String k,boolean d){return j.has(k)?j.get(k).getAsBoolean():d;}
 private static int i(JsonObject j,String k,int d){return j.has(k)?j.get(k).getAsInt():d;}
}
