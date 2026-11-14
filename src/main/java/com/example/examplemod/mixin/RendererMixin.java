package com.example.examplemod.mixin;
import com.example.examplemod.ModuleRuntime;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(GameRenderer.class)
public abstract class RendererMixin {
    @Inject(method="bobHurt",at=@At("HEAD"),cancellable=true)private void sobaka$hurt(CallbackInfo ci){if(ModuleRuntime.remove("hurt"))ci.cancel();}
    @Inject(method="bobView",at=@At("HEAD"),cancellable=true)private void sobaka$bob(CallbackInfo ci){if(ModuleRuntime.remove("bob"))ci.cancel();}
    @Inject(method="renderConfusionOverlay",at=@At("HEAD"),cancellable=true)private void sobaka$nausea(CallbackInfo ci){if(ModuleRuntime.remove("nausea"))ci.cancel();}
    @Redirect(method="renderLevel",at=@At(value="INVOKE",target="Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float sobaka$warp(float delta,float start,float end){return ModuleRuntime.remove("nausea")?0:Mth.lerp(delta,start,end);}
}
