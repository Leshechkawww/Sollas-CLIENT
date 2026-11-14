package com.example.examplemod.mixin;
import com.example.examplemod.ModuleRuntime;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ParticleEngine.class)
public abstract class ParticlesMixin {
    @Inject(method="render(Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;Ljava/util/function/Predicate;)V",at=@At("HEAD"),cancellable=true)
    private void sobaka$particles(CallbackInfo ci){if(ModuleRuntime.remove("particles"))ci.cancel();}
}
