package com.example.examplemod.mixin;
import com.example.examplemod.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(CreativeModeInventoryScreen.class)
public abstract class GlassCreativeInventoryMixin {
 @Redirect(method="renderBg",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
 private void sollas$background(GuiGraphics g,ResourceLocation texture,int x,int y,int u,int v,int w,int h){
  if(ModuleSettings.on("glassinv"))GlassInventory.draw((AbstractContainerScreen<?>)(Object)this,g,false);else g.blit(texture,x,y,u,v,w,h);
 }
 @Redirect(method="renderTabButton",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
 private void sollas$tab(GuiGraphics g,ResourceLocation texture,int x,int y,int w,int h){
  if(ModuleSettings.on("glassinv"))LiquidUi.flat(g,x+1,y+2,w-2,h-3,7,0xC3253451);else g.blitSprite(texture,x,y,w,h);
 }
 @Redirect(method="renderLabels",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"))
 private int sollas$label(GuiGraphics g,Font font,Component text,int x,int y,int color,boolean shadow){
  return g.drawString(font,text,x,y,ModuleSettings.on("glassinv")?0xFFE6EDFF:color,shadow);
 }
}
