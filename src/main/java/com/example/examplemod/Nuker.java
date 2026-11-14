package com.example.examplemod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
/** Mine only blocks matching the held block, using normal server interactions. */
public final class Nuker {
 private static BlockPos target;
 public static void tick(){var mc=Minecraft.getInstance();var p=mc.player;
  if(!ModuleSettings.on("nuker")||!(p.getMainHandItem().getItem() instanceof BlockItem held)){if(target!=null){mc.gameMode.stopDestroyBlock();target=null;}return;}
  int radius=(int)ModuleSettings.v("nuker","radius");BlockPos origin=p.blockPosition();
  if(target==null||!mc.level.getBlockState(target).is(held.getBlock())||target.distSqr(origin)>radius*radius){target=null;double best=Double.MAX_VALUE;
   for(var candidate:BlockPos.betweenClosed(origin.offset(-radius,-radius,-radius),origin.offset(radius,radius,radius))){double dist=candidate.distSqr(origin);if(dist>radius*radius||dist>=best||!mc.level.hasChunkAt(candidate))continue;var state=mc.level.getBlockState(candidate);if(state.is(held.getBlock())&&state.getDestroySpeed(mc.level,candidate)>=0){target=candidate.immutable();best=dist;}}
   if(target!=null)mc.gameMode.startDestroyBlock(target,Direction.UP);
  }
  if(target!=null)mc.gameMode.continueDestroyBlock(target,Direction.UP);
 }
}
