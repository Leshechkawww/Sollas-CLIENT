package com.example.examplemod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
/** Defers a grounded hit until the local player is naturally falling. */
public final class Criticals {
 private static Entity pending;private static long deadline;private static boolean dispatch;
 public static boolean defer(Player player,Entity target){
  if(dispatch||!ModuleSettings.on("criticals")||FakePlayerManager.isFake(target)||!player.onGround()||player.isInWaterOrBubble()||player.onClimbable()||player.isPassenger()||player.hasEffect(MobEffects.BLINDNESS)||player.getAbilities().flying)return false;
  if(pending==null){pending=target;deadline=System.nanoTime()+1_500_000_000L;player.jumpFromGround();}return true;
 }
 public static void tick(){if(pending==null)return;var mc=Minecraft.getInstance();var p=mc.player;
  if(!ModuleSettings.on("criticals")||p==null||mc.level==null||pending.isRemoved()||p.distanceToSqr(pending)>20.25||System.nanoTime()>deadline){pending=null;return;}
  if(!p.onGround()&&p.fallDistance>0&&p.getAttackStrengthScale(0)>.9f){Entity target=pending;pending=null;dispatch=true;try{mc.gameMode.attack(p,target);p.swing(net.minecraft.world.InteractionHand.MAIN_HAND);}finally{dispatch=false;}}
 }
}
