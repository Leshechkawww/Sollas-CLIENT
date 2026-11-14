package com.example.examplemod.mixin;
import com.example.examplemod.ModuleRuntime;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityDispatcherMixin {
    @Inject(method="renderShadow",at=@At("HEAD"),cancellable=true)private static void sobaka$shadow(CallbackInfo ci){if(ModuleRuntime.remove("shadows"))ci.cancel();}
}
