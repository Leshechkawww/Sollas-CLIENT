package com.example.examplemod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;
import java.util.UUID;
/** Camera is client-only, in loaded chunks; player input is suppressed while active. */
public final class FreeCamera {
 private static RemotePlayer camera;private static ClientLevel owner;private static Entity previous;
 public static boolean active(){return camera!=null;}
 public static void tick(){var mc=Minecraft.getInstance();
  if(camera!=null&&(!ModuleSettings.on("freecam")||mc.player==null||mc.level!=owner)){if(mc.getCameraEntity()==camera)mc.setCameraEntity(previous!=null&&!previous.isRemoved()?previous:mc.player);camera=null;owner=null;previous=null;}
  if(!ModuleSettings.on("freecam")||mc.player==null||mc.level==null)return;
  if(camera==null){previous=mc.getCameraEntity();owner=mc.level;camera=new RemotePlayer(owner,new GameProfile(UUID.randomUUID(),"SollasCamera"));camera.setPos(mc.player.getX(),mc.player.getY(),mc.player.getZ());camera.setYRot(mc.player.getYRot());camera.setXRot(mc.player.getXRot());camera.xRotO=camera.getXRot();camera.yRotO=camera.getYRot();mc.setCameraEntity(camera);}
  if(mc.screen!=null)return;
  float f=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0),s=(mc.options.keyLeft.isDown()?1:0)-(mc.options.keyRight.isDown()?1:0);double len=Math.hypot(f,s);if(len>1){f/=len;s/=len;}
  double yaw=Math.toRadians(camera.getYRot()),speed=ModuleSettings.v("freecam","speed");double dx=(-Math.sin(yaw)*f+Math.cos(yaw)*s)*speed,dz=(Math.cos(yaw)*f+Math.sin(yaw)*s)*speed,dy=(mc.options.keyJump.isDown()?speed:0)-(mc.options.keyShift.isDown()?speed:0);
  camera.xo=camera.getX();camera.yo=camera.getY();camera.zo=camera.getZ();camera.setPos(camera.getX()+dx,camera.getY()+dy,camera.getZ()+dz);camera.xRotO=camera.getXRot();camera.yRotO=camera.getYRot();
 }
 public static void turn(double x,double y){if(camera!=null){camera.setYRot(camera.getYRot()+(float)x*.15f);camera.setXRot(Math.clamp(camera.getXRot()+(float)y*.15f,-90,90));}}
 public static void input(net.neoforged.neoforge.client.event.MovementInputUpdateEvent event){if(camera!=null){var input=event.getInput();input.forwardImpulse=0;input.leftImpulse=0;input.jumping=false;input.shiftKeyDown=false;}}
}
