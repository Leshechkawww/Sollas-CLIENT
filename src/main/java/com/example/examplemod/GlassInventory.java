package com.example.examplemod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/** One shared background capture, then batched vanilla-coordinate slot surfaces. */
public final class GlassInventory {
 public static void draw(AbstractContainerScreen<?> screen,GuiGraphics g,boolean inventory){
  int x=screen.getGuiLeft(),y=screen.getGuiTop(),w=screen.getXSize(),h=screen.getYSize();
  var settings=ClientSettings.INSTANCE;int prior=settings.glass;
  try { settings.glass=2;GlassRenderer.begin(g);int alpha=(int)(ModuleSettings.v("glassinv","tint")*255);GlassRenderer.panel(g,x,y,w,h,9,(alpha<<24)|0x18233A,true); }
  finally { settings.glass=prior; }
  int a=(int)(ModuleSettings.v("glassinv","slots")*255);
  for(var slot:screen.getMenu().slots){if(!slot.isActive())continue;int sx=x+slot.x,sy=y+slot.y;
   g.fill(sx-1,sy-1,sx+17,sy+17,(Math.min(255,a+22)<<24)|0xAABBE0);
   g.fill(sx,sy,sx+16,sy+16,(a<<24)|0x101A2C);
  }
  if(inventory){g.fill(x+76,y+35,x+88,y+37,0x99D8D9FF);g.fill(x+85,y+32,x+87,y+40,0x99D8D9FF);}
 }
}
