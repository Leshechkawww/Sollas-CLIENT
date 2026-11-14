package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ClientPacketListener.class)
public abstract class VelocityMixin {
 @Redirect(method="handleSetEntityMotion",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;lerpMotion(DDD)V"))
 private void sollas$velocity(Entity entity,double x,double y,double z){if(ModuleSettings.on("velocity")&&entity==Minecraft.getInstance().player){double h=ModuleSettings.v("velocity","horizontal"),v=ModuleSettings.v("velocity","vertical");entity.lerpMotion(x*h,y*v,z*h);}else entity.lerpMotion(x,y,z);}
}
