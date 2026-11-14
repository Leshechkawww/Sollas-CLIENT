package com.example.examplemod.mixin;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FontTexture.class)
public abstract class FontTextureMixin extends AbstractTexture {
    @Inject(method="<init>", at=@At("RETURN"))
    private void sobaka$smooth(CallbackInfo ci){setFilter(true,false);}
}
