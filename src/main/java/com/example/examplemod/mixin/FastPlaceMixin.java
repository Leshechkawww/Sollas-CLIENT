package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
public abstract class FastPlaceMixin {
 @Inject(method="startUseItem",at=@At("RETURN"))
 private void sollas$delay(CallbackInfo ci){if(ModuleSettings.on("fastplace"))((MinecraftAccessor)(Object)this).sollas$setDelay((int)ModuleSettings.v("fastplace","delay"));}
}
