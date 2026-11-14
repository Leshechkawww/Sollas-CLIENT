package com.example.examplemod.mixin;
import com.example.examplemod.*;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LightTexture.class)
public abstract class LightMixin {
    @ModifyArg(method="updateLightTexture",at=@At(value="INVOKE",target="Lcom/mojang/blaze3d/platform/NativeImage;setPixelRGBA(III)V"),index=2)
    private int sobaka$bright(int abgr){if(!ModuleSettings.on("bright"))return abgr;double a=ModuleSettings.v("bright","strength");int out=0xFF000000;for(int s=0;s<24;s+=8)out|=(int)(((abgr>>s)&255)*(1-a)+255*a)<<s;return out;}
    @Inject(method="getDarknessGamma",at=@At("HEAD"),cancellable=true)
    private void sobaka$dark(float partial,CallbackInfoReturnable<Float> ci){if(ModuleRuntime.remove("dark"))ci.setReturnValue(0f);}
}
