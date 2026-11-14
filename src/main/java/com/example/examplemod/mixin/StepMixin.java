package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LivingEntity.class)
public abstract class StepMixin {
 @Inject(method="maxUpStep",at=@At("HEAD"),cancellable=true)
 private void sollas$step(CallbackInfoReturnable<Float> ci){if(ModuleSettings.on("step")&&(Object)this==Minecraft.getInstance().player)ci.setReturnValue((float)ModuleSettings.v("step","height"));}
}
