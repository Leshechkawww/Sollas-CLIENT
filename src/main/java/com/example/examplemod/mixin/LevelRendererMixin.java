package com.example.examplemod.mixin;
import com.example.examplemod.ModuleRuntime;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Inject(method="renderSky",at=@At("HEAD"),cancellable=true)private void sobaka$sky(CallbackInfo ci){if(ModuleRuntime.remove("sky"))ci.cancel();}
    @Inject(method="renderClouds",at=@At("HEAD"),cancellable=true)private void sobaka$clouds(CallbackInfo ci){if(ModuleRuntime.remove("clouds"))ci.cancel();}
    @Inject(method="doesMobEffectBlockSky",at=@At("HEAD"),cancellable=true)private void sobaka$dark(CallbackInfoReturnable<Boolean> ci){if(ModuleRuntime.remove("dark"))ci.setReturnValue(false);}
}
