package com.example.examplemod.mixin;
import com.example.examplemod.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(InventoryScreen.class)
public abstract class GlassPlayerInventoryMixin {
 @Redirect(method="renderBg",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
 private void sollas$background(GuiGraphics g,ResourceLocation texture,int x,int y,int u,int v,int w,int h){
  if(ModuleSettings.on("glassinv"))GlassInventory.draw((AbstractContainerScreen<?>)(Object)this,g,true);else g.blit(texture,x,y,u,v,w,h);
 }
}
