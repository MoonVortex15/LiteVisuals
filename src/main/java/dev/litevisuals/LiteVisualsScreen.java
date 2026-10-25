package dev.litevisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import java.util.function.Consumer;

public class LiteVisualsScreen extends Screen {
 private String tab="VISUALS";
 public LiteVisualsScreen(){super(Text.literal("LiteVisuals"));}
 @Override protected void init(){
  clearChildren();int l=width/2-154,t=height/2-119;
  addDrawableChild(btn("VISUALS",l,t,98,()->{tab="VISUALS";init();}));
  addDrawableChild(btn("HUD",l+103,t,98,()->{tab="HUD";init();}));
  addDrawableChild(btn("SETTINGS",l+206,t,98,()->{tab="SETTINGS";init();}));
  int y=t+29;
  if(tab.equals("VISUALS")){
   toggle(l,y,"Fullbright",LiteVisualsClient.fullbrightEnabled,v->LiteVisualsClient.fullbrightEnabled=v);y+=22;
   toggle(l,y,"No Fog (not implemented)",LiteVisualsClient.noFogEnabled,v->LiteVisualsClient.noFogEnabled=v);y+=22;
   toggle(l,y,"Entity ESP (not implemented)",LiteVisualsClient.entityEspEnabled,v->LiteVisualsClient.entityEspEnabled=v);y+=22;
   toggle(l,y,"Nametags (not implemented)",LiteVisualsClient.nametagsEnabled,v->LiteVisualsClient.nametagsEnabled=v);y+=22;
   toggle(l,y,"Hitbox (not implemented)",LiteVisualsClient.hitboxEnabled,v->LiteVisualsClient.hitboxEnabled=v);y+=22;
   toggle(l,y,"Target health bar",LiteVisualsClient.healthEnabled,v->LiteVisualsClient.healthEnabled=v);y+=22;
   toggle(l,y,"Target armor value",LiteVisualsClient.armorEnabled,v->LiteVisualsClient.armorEnabled=v);y+=22;
   toggle(l,y,"Weather visual (not implemented)",LiteVisualsClient.weatherVisualsEnabled,v->LiteVisualsClient.weatherVisualsEnabled=v);
  }else if(tab.equals("HUD")){
   toggle(l,y,"Master HUD (H)",LiteVisualsClient.hudEnabled,v->LiteVisualsClient.hudEnabled=v);y+=22;
   toggle(l,y,"FPS counter",LiteVisualsClient.fpsEnabled,v->LiteVisualsClient.fpsEnabled=v);y+=22;
   toggle(l,y,"Coordinates XYZ",LiteVisualsClient.coordsEnabled,v->LiteVisualsClient.coordsEnabled=v);y+=22;
   toggle(l,y,"Direction",LiteVisualsClient.facingEnabled,v->LiteVisualsClient.facingEnabled=v);y+=22;
   toggle(l,y,"Target HUD",LiteVisualsClient.targetEnabled,v->LiteVisualsClient.targetEnabled=v);y+=22;
   toggle(l,y,"ArrayList",LiteVisualsClient.arrayListEnabled,v->LiteVisualsClient.arrayListEnabled=v);y+=22;
   toggle(l,y,"Keystrokes",LiteVisualsClient.keystrokesEnabled,v->LiteVisualsClient.keystrokesEnabled=v);y+=22;
   toggle(l,y,"CPS counter",LiteVisualsClient.cpsEnabled,v->LiteVisualsClient.cpsEnabled=v);
  }else{
   toggle(l,y,"Compact HUD",LiteVisualsClient.compactHud,v->LiteVisualsClient.compactHud=v);y+=22;
   addDrawableChild(btn("HUD X: "+LiteVisualsClient.hudX,l,y,148,()->{LiteVisualsClient.hudX=(LiteVisualsClient.hudX+8)%240;init();}));y+=22;
   addDrawableChild(btn("HUD Y: "+LiteVisualsClient.hudY,l+156,y,148,()->{LiteVisualsClient.hudY=(LiteVisualsClient.hudY+8)%160;init();}));y+=22;
   addDrawableChild(btn("Keystrokes X: "+LiteVisualsClient.keystrokesX,l,y,148,()->{LiteVisualsClient.keystrokesX=(LiteVisualsClient.keystrokesX+8)%240;init();}));y+=22;
   addDrawableChild(btn("Keystrokes Y: "+(LiteVisualsClient.keystrokesY==0?"Auto":LiteVisualsClient.keystrokesY),l+156,y,148,()->{LiteVisualsClient.keystrokesY=LiteVisualsClient.keystrokesY==0?120:0;init();}));y+=22;
   addDrawableChild(btn("CPS X: "+LiteVisualsClient.cpsX,l,y,148,()->{LiteVisualsClient.cpsX=LiteVisualsClient.cpsX>=0?-40:0;init();}));y+=22;
   addDrawableChild(btn("CPS Y: "+LiteVisualsClient.cpsY,l+156,y,148,()->{LiteVisualsClient.cpsY=LiteVisualsClient.cpsY>=0?-20:0;init();}));y+=22;
   addDrawableChild(btn("Accent color: purple / pink",l,y,304,()->{LiteVisualsClient.accentColor=LiteVisualsClient.accentColor==0xFFB56BFF?0xFFFF78C8:0xFFB56BFF;init();}));y+=22;
   addDrawableChild(btn("Save config now",l,y,304,LiteVisualsClient::saveConfig));
  }
  addDrawableChild(btn("CLOSE",l+206,t+218,98,()->close()));
 }
 private ButtonWidget btn(String label,int x,int y,int w,Runnable action){return ButtonWidget.builder(Text.literal(label),b->action.run()).dimensions(x,y,w,20).build();}
 private void toggle(int x,int y,String name,boolean value,Consumer<Boolean> set){addDrawableChild(btn(name+": "+(value?"ON":"OFF"),x,y,304,()->{set.accept(!value);LiteVisualsClient.saveConfig();init();}));}
 @Override public void render(DrawContext ctx,int mx,int my,float delta){renderBackground(ctx,mx,my,delta);int x=width/2-164,y=height/2-132;ctx.fill(x,y,x+328,y+270,0xF20D0B16);ctx.fill(x,y,x+328,y+3,LiteVisualsClient.accentColor);ctx.drawTextWithShadow(textRenderer,"LITEVISUALS",x+12,y+10,LiteVisualsClient.accentColor);ctx.drawTextWithShadow(textRenderer,"FABRIC 1.21.4",x+222,y+10,0xFFAAA8C2);super.render(ctx,mx,my,delta);}
 @Override public boolean shouldPause(){return false;}
}
