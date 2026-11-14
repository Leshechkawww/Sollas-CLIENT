package com.example.examplemod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class GameplayModules {
 private static int equipmentDelay,fishDelay,scaffoldDelay;private static Object world;
 public static void tick(){var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.level==null||mc.gameMode==null)return;
  if(world!=mc.level){world=mc.level;equipmentDelay=fishDelay=0;}
  if(equipmentDelay>0)equipmentDelay--;if(fishDelay>0)fishDelay--;if(scaffoldDelay>0)scaffoldDelay--;
  OreScanner.tick();Criticals.tick();
  if(mc.screen!=null)return;
  if(FreeCamera.active())return;
  movement(mc,p);Nuker.tick();
  if(ModuleSettings.on("aim"))aim(p,"aim",false);
  if(ModuleSettings.on("bowaim")&&p.isUsingItem()&&p.getUseItem().getItem() instanceof BowItem)aim(p,"bowaim",true);
  if(ModuleSettings.on("aura")&&p.getAttackStrengthScale(0)>=.95f&&!p.isUsingItem()){
   LivingEntity target=find(p,ModuleSettings.v("aura","range"),ModuleSettings.b("aura","players"),ModuleSettings.b("aura","mobs"));
   if(target!=null){mc.gameMode.attack(p,target);p.swing(net.minecraft.world.InteractionHand.MAIN_HAND);}
  }
  if(equipmentDelay==0&&p.containerMenu==p.inventoryMenu&&p.inventoryMenu.getCarried().isEmpty()){
   if(ModuleSettings.on("totem")&&p.getHealth()<=ModuleSettings.v("totem","health")&&!p.getOffhandItem().is(Items.TOTEM_OF_UNDYING)){
    for(int slot=9;slot<=44;slot++)if(p.inventoryMenu.getSlot(slot).getItem().is(Items.TOTEM_OF_UNDYING)){move(mc,slot,45);equipmentDelay=5;break;}
   }
   if(equipmentDelay==0&&ModuleSettings.on("autoarmor")){
    for(int slot=9;slot<=44;slot++){var item=p.inventoryMenu.getSlot(slot).getItem();if(!(item.getItem() instanceof ArmorItem armor))continue;EquipmentSlot type=armor.getEquipmentSlot();int dest=switch(type){case HEAD->5;case CHEST->6;case LEGS->7;case FEET->8;default->-1;};
     if(dest>=0&&p.inventoryMenu.getSlot(dest).getItem().isEmpty()){mc.gameMode.handleInventoryMouseClick(p.inventoryMenu.containerId,slot,0,ClickType.QUICK_MOVE,p);equipmentDelay=5;break;}}
   }
  }
  if(ModuleSettings.on("autofish")&&p.fishing!=null&&fishDelay==0&&((com.example.examplemod.mixin.FishingHookAccessor)(Object)p.fishing).sollas$isBiting()){mc.gameMode.useItem(p,net.minecraft.world.InteractionHand.MAIN_HAND);fishDelay=30;}
 }
 private static void movement(Minecraft mc,LocalPlayer p){
  if(!p.isPassenger()){
   boolean fly=ModuleSettings.on("fly"),speed=ModuleSettings.on("speed");
   if(fly||speed){float f=p.input.forwardImpulse,side=p.input.leftImpulse;double len=Math.hypot(f,side);if(len>1){f/=len;side/=len;}
    double yaw=Math.toRadians(p.getYRot()),base=fly?ModuleSettings.v("fly","speed"):ModuleSettings.v("speed","speed");
    double vx=(-Math.sin(yaw)*f+Math.cos(yaw)*side)*base,vz=(Math.cos(yaw)*f+Math.sin(yaw)*side)*base;
    double vy=fly?(p.input.jumping?base:p.input.shiftKeyDown?-base:0):p.getDeltaMovement().y;
    p.setDeltaMovement(vx,vy,vz);
   }
  }
  if(ModuleSettings.on("scaffold")&&scaffoldDelay==0&&p.getMainHandItem().getItem() instanceof BlockItem){
   var below=p.blockPosition().below();if(p.level().getBlockState(below).canBeReplaced()){
    for(var face:net.minecraft.core.Direction.values()){
     var neighbor=below.relative(face);if(p.level().getBlockState(neighbor).isAir())continue;
     var direction=face.getOpposite();var point=Vec3.atCenterOf(neighbor).add(direction.getStepX()*.5,direction.getStepY()*.5,direction.getStepZ()*.5);
     if(p.getEyePosition().distanceToSqr(point)>20.25)continue;
     var hit=new net.minecraft.world.phys.BlockHitResult(point,direction,neighbor,false);
     mc.gameMode.useItemOn(p,net.minecraft.world.InteractionHand.MAIN_HAND,hit);p.swing(net.minecraft.world.InteractionHand.MAIN_HAND);scaffoldDelay=(int)ModuleSettings.v("scaffold","delay");break;
    }
   }
  }
 }
 private static void move(Minecraft mc,int from,int to){var p=mc.player;int id=p.inventoryMenu.containerId;mc.gameMode.handleInventoryMouseClick(id,from,0,ClickType.PICKUP,p);mc.gameMode.handleInventoryMouseClick(id,to,0,ClickType.PICKUP,p);mc.gameMode.handleInventoryMouseClick(id,from,0,ClickType.PICKUP,p);}
 private static LivingEntity find(LocalPlayer p,double range,boolean players,boolean mobs){LivingEntity best=null;double dist=range*range;for(var e:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range),a->a!=p&&a.isAlive()&&!a.isSpectator()&&p.hasLineOfSight(a))){if(e instanceof Player?( !players||ModuleSettings.INSTANCE.friends.contains(e.getUUID().toString())):!mobs)continue;double v=p.distanceToSqr(e);if(v<dist){dist=v;best=e;}}return best;}
 private static void aim(LocalPlayer p,String id,boolean bow){double range=ModuleSettings.v(id,"range");LivingEntity target=find(p,range,bow||ModuleSettings.b(id,"players"),bow||ModuleSettings.b(id,"mobs"));if(target==null)return;
  Vec3 aim=target.getEyePosition().subtract(p.getEyePosition());double horizontal=Math.hypot(aim.x,aim.z);float yaw=(float)(Math.toDegrees(Math.atan2(aim.z,aim.x))-90),pitch=(float)-Math.toDegrees(Math.atan2(aim.y,horizontal));
  float smooth=(float)ModuleSettings.v(id,"smooth");p.setYRot(p.getYRot()+Mth.wrapDegrees(yaw-p.getYRot())*smooth);p.setXRot(Mth.clamp(p.getXRot()+(pitch-p.getXRot())*smooth,-90,90));
 }
 public static void tracers(RenderLevelStageEvent event){if(!ModuleSettings.on("tracers"))return;var mc=Minecraft.getInstance();var p=mc.player;double range=ModuleSettings.v("tracers","range");Vec3 start=event.getCamera().getPosition().add(p.getViewVector(1).scale(.5));int count=0;
  for(var e:mc.level.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range),a->a!=p&&a.isAlive())){if(count++>100)break;if(e instanceof Player?!ModuleSettings.b("tracers","players"):!ModuleSettings.b("tracers","mobs"))continue;WorldDraw.line(start,e.position().add(0,e.getBbHeight()*.5,0),ClientSettings.INSTANCE.color());}
 }
}
