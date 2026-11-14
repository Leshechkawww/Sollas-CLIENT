package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(AbstractContainerScreen.class)
public abstract class GlassLabelsMixin {
 @Redirect(method="renderLabels",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;IIIZ)I"))
 private int sollas$label(GuiGraphics g,Font font,Component text,int x,int y,int color,boolean shadow){
  boolean use=ModuleSettings.on("glassinv")&&((Object)this instanceof InventoryScreen||(Object)this instanceof ContainerScreen);
  return g.drawString(font,text,x,y,use?0xFFE6EDFF:color,shadow);
 }
}
