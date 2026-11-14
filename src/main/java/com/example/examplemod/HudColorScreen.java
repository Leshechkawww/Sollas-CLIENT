package com.example.examplemod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class HudColorScreen extends Screen {
 private final Screen parent;
 public HudColorScreen(Screen parent){super(Component.literal("HUD color"));this.parent=parent;}
 @Override protected void init(){int x=width/2-110,y=height/2-45;
  addRenderableWidget(new AbstractSliderButton(x,y+35,220,24,Component.literal("HUD hue"),ClientSettings.INSTANCE.hudHue){
   protected void updateMessage(){setMessage(Component.literal("HUD color  "+(int)(value*360)+"°"));}
   protected void applyValue(){ClientSettings.INSTANCE.hudHue=(float)value;}
   public void onRelease(double x,double y){super.onRelease(x,y);ClientSettings.INSTANCE.save();}
   public boolean keyPressed(int k,int s,int m){boolean r=super.keyPressed(k,s,m);if(r)ClientSettings.INSTANCE.save();return r;}
  });
  addRenderableWidget(new LiquidButton(x,y+68,220,30,19,()->"Done","",this::onClose,()->false,false));
 }
 @Override public void render(GuiGraphics g,int x,int y,float dt){if(minecraft.level==null)renderPanorama(g,dt);GlassRenderer.begin(g);g.fill(0,0,width,height,0x88081120);LiquidUi.surface(g,width/2-125,height/2-65,250,125,16,0xCC162338);LiquidUi.text(g,"HUD COLOR",width/2-110,height/2-49,13,ClientSettings.INSTANCE.hudColor(),true);super.render(g,x,y,dt);}
 @Override public void renderBackground(GuiGraphics g,int x,int y,float dt){}
 @Override public void onClose(){ClientSettings.INSTANCE.save();minecraft.setScreen(parent);}
 @Override public boolean isPauseScreen(){return false;}
}
