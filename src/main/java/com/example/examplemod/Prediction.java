package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.*;

public final class Prediction {
    public record Path(List<Vec3> points,HitResult hit){}
    public static List<Path> paths=List.of();public static Vec3 lastImpact;public static String description="";
    private static long updated;
    public static void reset(){paths=List.of();lastImpact=null;updated=0;}
    public static void update(){
        long now=System.nanoTime();if(now-updated<33_000_000)return;updated=now;
        var mc=Minecraft.getInstance();var p=mc.player;if(!ModuleSettings.on("predict")||p==null||mc.level==null){reset();return;}
        ItemStack stack=p.getMainHandItem();if(!supported(stack))stack=p.getOffhandItem();if(!supported(stack)){paths=List.of();lastImpact=null;return;}
        Item item=stack.getItem();double velocity=1.5,gravity=.03,water=.8;float pitchOffset=0;
        boolean arrow=false,rocket=false,inherit=true;List<Float> angles=List.of(0f);String note="";
        if(item instanceof BowItem){float power=p.isUsingItem()?BowItem.getPowerForTime(p.getTicksUsingItem()):1;if(power<.1f){paths=List.of();lastImpact=null;return;}velocity=3*power;gravity=.05;water=.6;arrow=true;if(!p.isUsingItem())note="Full draw · ";}
        else if(item instanceof CrossbowItem){
            var loaded=stack.getOrDefault(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.EMPTY);if(loaded.isEmpty()){paths=List.of();lastImpact=null;return;}
            rocket=loaded.contains(Items.FIREWORK_ROCKET);velocity=rocket?1.6:3.15;gravity=rocket?0:.05;water=.6;arrow=!rocket;inherit=false;
            if(loaded.getItems().size()>1)angles=List.of(-10f,0f,10f);
        }else if(item instanceof TridentItem){velocity=2.5;gravity=.05;water=.99;arrow=true;note="Throw · ";}
        else if(item instanceof ThrowablePotionItem){velocity=.5;gravity=.05;pitchOffset=-20;}
        else if(item==Items.EXPERIENCE_BOTTLE){velocity=.7;gravity=.07;pitchOffset=-20;}
        else if(item==Items.WIND_CHARGE){velocity=1.5;gravity=0;water=1;}
        Vec3 start=p.getEyePosition().add(0,-.1,0);List<Path> result=new ArrayList<>();
        final int maxSteps=(int)ModuleSettings.v("predict","steps"); final boolean collideEntities=ModuleSettings.b("predict","entities");
        for(float angle:angles){
            double yaw=Math.toRadians(p.getYRot()+angle),pitch=Math.toRadians(p.getXRot());
            Vec3 dir=new Vec3(-Math.sin(yaw)*Math.cos(pitch),-Math.sin(Math.toRadians(p.getXRot()+pitchOffset)),Math.cos(yaw)*Math.cos(pitch)).normalize().scale(velocity);
            if(inherit){Vec3 movement=p.getKnownMovement();dir=dir.add(movement.x,p.onGround()?0:movement.y,movement.z);}
            Vec3 pos=start;List<Vec3> points=new ArrayList<>();points.add(pos);HitResult hit=null;
            for(int tick=0;tick<maxSteps;tick++){
                if(!mc.level.hasChunkAt(BlockPos.containing(pos))||pos.y<mc.level.getMinBuildHeight()-32)break;
                Vec3 next=pos.add(dir);
                HitResult block=mc.level.clip(new ClipContext(pos,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
                if(block.getType()!=HitResult.Type.MISS)next=block.getLocation();
                double closest=pos.distanceToSqr(next);HitResult entityHit=null;
                if(collideEntities){
                    AABB region=new AABB(pos,next).inflate(.4);
                    for(var entity:mc.level.getEntities(p,region,e->e.isAlive()&&e.isPickable()&&!e.isSpectator())){
                        var clip=entity.getBoundingBox().inflate(.3).clip(pos,next);
                        if(clip.isPresent()){double dist=pos.distanceToSqr(clip.get());if(dist<closest){closest=dist;entityHit=new EntityHitResult(entity,clip.get());}}
                    }
                }
                if(entityHit!=null){hit=entityHit;points.add(hit.getLocation());break;}
                if(block.getType()!=HitResult.Type.MISS){hit=block;points.add(next);break;}
                points.add(next);
                boolean inWater=mc.level.getFluidState(BlockPos.containing(pos)).is(FluidTags.WATER);
                if(rocket)dir=new Vec3(dir.x*1.15,dir.y+.04,dir.z*1.15);
                else dir=dir.scale(inWater?water:(item==Items.WIND_CHARGE?1:.99)).add(0,-gravity,0);
                pos=next;
            }
            result.add(new Path(points,hit));
        }
        paths=result;Path center=result.get(result.size()/2);lastImpact=center.hit==null?null:center.hit.getLocation();
        description=lastImpact==null?"":note+String.format(Locale.ROOT,"%.1f blocks · %s",start.distanceTo(lastImpact),center.hit instanceof EntityHitResult?"entity":"landing");
    }
    private static boolean supported(ItemStack s){var i=s.getItem();return i instanceof BowItem||i instanceof CrossbowItem||i instanceof TridentItem||i instanceof ThrowablePotionItem||i==Items.SNOWBALL||i==Items.EGG||i==Items.ENDER_PEARL||i==Items.EXPERIENCE_BOTTLE||i==Items.WIND_CHARGE;}
}
