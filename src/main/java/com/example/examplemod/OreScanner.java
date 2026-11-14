package com.example.examplemod;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import java.util.*;
/** Scans only loaded chunks with a fixed per-tick budget. */
public final class OreScanner {
 private static Object world;private static BlockPos center;private static int radius,cursor,ticks;private static final List<BlockPos> ores=new ArrayList<>(),working=new ArrayList<>(),containers=new ArrayList<>();
 public static void tick(){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;
  if(world!=mc.level){world=mc.level;center=null;cursor=0;ores.clear();working.clear();containers.clear();}
  if(!ModuleSettings.on("xray")){ores.clear();working.clear();center=null;cursor=0;}
  else {int r=(int)ModuleSettings.v("xray","radius");BlockPos p=mc.player.blockPosition();
   if(center==null||radius!=r||center.distSqr(p)>64){center=p;radius=r;cursor=0;working.clear();ores.clear();}
   int side=radius*2+1,total=side*side*side;var pos=new BlockPos.MutableBlockPos();
   for(int n=0;n<1024&&cursor<total;n++,cursor++){int x=cursor%side-radius,y=(cursor/side)%side-radius,z=cursor/(side*side)-radius;pos.set(center.getX()+x,center.getY()+y,center.getZ()+z);if(!mc.level.hasChunkAt(pos)||mc.level.isOutsideBuildHeight(pos))continue;
    var state=mc.level.getBlockState(pos);boolean match=(ModuleSettings.b("xray","diamond")&&state.is(BlockTags.DIAMOND_ORES))||(ModuleSettings.b("xray","ancient")&&state.is(Blocks.ANCIENT_DEBRIS))||(ModuleSettings.b("xray","other")&&(state.is(BlockTags.IRON_ORES)||state.is(BlockTags.GOLD_ORES)||state.is(BlockTags.COAL_ORES)||state.is(BlockTags.REDSTONE_ORES)||state.is(BlockTags.LAPIS_ORES)||state.is(BlockTags.EMERALD_ORES)||state.is(BlockTags.COPPER_ORES)));
    if(match&&working.size()<512){BlockPos found=pos.immutable();working.add(found);if(ores.size()<512&&!ores.contains(found))ores.add(found);}
   }
   if(cursor>=total){ores.clear();ores.addAll(working);working.clear();cursor=0;}
  }
  if(!ModuleSettings.on("chestesp")){containers.clear();ticks=0;return;}
  if(++ticks%40!=1)return;containers.clear();int range=(int)ModuleSettings.v("chestesp","range"),cx=mc.player.chunkPosition().x,cz=mc.player.chunkPosition().z,cr=(range+15)/16;
  for(int x=cx-cr;x<=cx+cr;x++)for(int z=cz-cr;z<=cz+cr;z++){
   var chunk=mc.level.getChunkSource().getChunk(x,z,ChunkStatus.FULL,false);if(!(chunk instanceof LevelChunk loaded))continue;
   for(var e:loaded.getBlockEntities().values())if((e instanceof ChestBlockEntity||e instanceof BarrelBlockEntity||e instanceof ShulkerBoxBlockEntity)&&e.getBlockPos().distToCenterSqr(mc.player.position())<=range*range&&containers.size()<512)containers.add(e.getBlockPos().immutable());
  }
 }
 public static void render(){var mc=Minecraft.getInstance();if(mc.level==null)return;
  if(ModuleSettings.on("xray"))for(var p:ores){var state=mc.level.getBlockState(p);if(state.isAir())continue;int c=state.is(Blocks.ANCIENT_DEBRIS)?0xFFFF9A59:state.is(BlockTags.DIAMOND_ORES)?0xFF5EF0F4:0xFFFFD270;WorldDraw.overlayBox(new AABB(p).inflate(.006),c);}
  if(ModuleSettings.on("chestesp"))for(var p:containers)if(mc.level.getBlockEntity(p)!=null)WorldDraw.overlayBox(new AABB(p).deflate(.06),0xFFFFC678);
 }
}
