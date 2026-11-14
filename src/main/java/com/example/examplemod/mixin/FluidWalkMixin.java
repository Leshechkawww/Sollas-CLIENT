package com.example.examplemod.mixin;
import com.example.examplemod.ModuleSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(LiquidBlock.class)
public abstract class FluidWalkMixin {
 @Inject(method="getCollisionShape",at=@At("HEAD"),cancellable=true)
 private void sollas$surface(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context,CallbackInfoReturnable<VoxelShape> ci){
  if(!(context instanceof EntityCollisionContext entity))return;var mc=Minecraft.getInstance();if(mc==null||mc.player==null||entity.getEntity()!=mc.player)return;var player=mc.player;if(!ModuleSettings.on("jesus")||player.isShiftKeyDown())return;
  var fluid=state.getFluidState();if(!fluid.is(FluidTags.WATER)&&!(ModuleSettings.b("jesus","lava")&&fluid.is(FluidTags.LAVA)))return;
  float height=fluid.getHeight(level,pos);VoxelShape shape=Shapes.box(0,0,0,1,height,1);if(context.isAbove(shape,pos,true))ci.setReturnValue(shape);
 }
}
