package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.lwjgl.glfw.GLFW;

public final class ModuleRuntime {
    public static final KeyMapping FREE=new KeyMapping("key.sobaka.freelook",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_LEFT_ALT,"key.categories.myclient");
    public static final KeyMapping FRIEND=new KeyMapping("key.sobaka.friend",InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_UNKNOWN,"key.categories.myclient");
    public static LivingEntity target,hoverTarget;
    public static long attackedAt,hoverAt;public static int attackDelay;
    public static boolean freeActive;public static float freeYaw,freePitch;
    private static CameraType oldCamera;private static boolean sprintOwned;
    private static Object level;
    public static boolean remove(String key){return ModuleSettings.on("remove")&&ModuleSettings.b("remove",key);}
    public static void register(IEventBus mod){
        mod.addListener((RegisterKeyMappingsEvent e)->{e.register(FREE);e.register(FRIEND);});
        mod.addListener((RegisterGuiLayersEvent e)->e.registerAboveAll(ExampleModClient.id("modules"),(g,d)->ModuleHud.render(g,d.getGameTimeDeltaPartialTick(false))));
        mod.addListener(ModulePostFx::register);
        var bus=NeoForge.EVENT_BUS;
        bus.addListener(FreeCamera::input);
        bus.addListener(ModuleRuntime::tick);bus.addListener(ModuleRuntime::attack);
        bus.addListener(ModuleRuntime::keys);
        bus.addListener(ModuleRuntime::layers);bus.addListener(ModuleRuntime::overlay);
        bus.addListener(ModuleRuntime::fogColor);bus.addListener(ModuleRuntime::fog);
        bus.addListener((ViewportEvent.ComputeCameraAngles e)->{if(freeActive){e.setYaw(freeYaw);e.setPitch(freePitch);}});
        bus.addListener((RenderLivingEvent.Pre<?,?> e)->{if(WorldModules.hideCorpse(e.getEntity()))e.setCanceled(true);});
        bus.addListener(WorldModules::render);
        bus.addListener(ViewModelEditor::init);bus.addListener(ViewModelEditor::draw);
        bus.addListener(ViewModelEditor::click);bus.addListener(ViewModelEditor::drag);bus.addListener(ViewModelEditor::scroll);bus.addListener(ViewModelEditor::release);
    }
    private static void keys(InputEvent.Key e){
        var mc=Minecraft.getInstance();if(e.getAction()!=GLFW.GLFW_PRESS||mc.screen!=null||mc.player==null)return;
        for(var module:ModuleSettings.ALL)if(ModuleSettings.bind(module.id())==e.getKey())ModuleSettings.toggle(module.id());
    }
    private static void attack(AttackEntityEvent e){
        var mc=Minecraft.getInstance();if(e.getEntity()!=mc.player)return;
        attackedAt=System.nanoTime();attackDelay=(int)ModuleSettings.v("sprint","delay");
        if(e.getTarget() instanceof LivingEntity l)target=l;
        if(ModuleSettings.on("sprint")){mc.options.keySprint.setDown(false);mc.player.setSprinting(false);}
    }
    private static void tick(ClientTickEvent.Post e){
        var mc=Minecraft.getInstance();
        if(level!=mc.level){level=mc.level;target=hoverTarget=null;WorldModules.reset();Prediction.reset();setFree(false);}
        FreeCamera.tick();
        if(mc.player==null){releaseSprint();setFree(false);return;}
        if(attackDelay>0)attackDelay--;
        if(ModuleSettings.on("sprint")&&mc.screen==null&&attackDelay==0&&mc.player.input.forwardImpulse>.8&&!mc.player.isShiftKeyDown()&&!mc.player.isUsingItem()){
            mc.options.keySprint.setDown(true);sprintOwned=true;
        }else releaseSprint();
        if(!ModuleSettings.on("free")||mc.screen!=null)setFree(false);
        else if(ModuleSettings.v("free","mode")==0)setFree(FREE.isDown());
        while(FREE.consumeClick())if(ModuleSettings.on("free")&&ModuleSettings.v("free","mode")==1&&mc.screen==null)setFree(!freeActive);
        while(FRIEND.consumeClick())if(mc.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof net.minecraft.world.entity.player.Player p){
            String id=p.getUUID().toString();boolean added=ModuleSettings.INSTANCE.friends.add(id);if(!added)ModuleSettings.INSTANCE.friends.remove(id);ModuleSettings.save();
            mc.player.displayClientMessage(Component.literal((added?"Friend added: ":"Friend removed: ")+p.getName().getString()),true);
        }
        long now=System.nanoTime();
        if(ModuleSettings.on("targethud") && mc.screen==null){
            double reach=ModuleSettings.v("targethud","range"); var eye=mc.player.getEyePosition();var direction=mc.player.getViewVector(1f);var end=eye.add(direction.scale(reach));
            var block=mc.level.clip(new net.minecraft.world.level.ClipContext(eye,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,mc.player));
            if(block.getType()!=net.minecraft.world.phys.HitResult.Type.MISS)end=block.getLocation();
            var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(mc.player,eye,end,mc.player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1),e2->e2 instanceof net.minecraft.world.entity.player.Player && e2.isAlive()&&!e2.isSpectator(),eye.distanceToSqr(end));
            if(hit!=null && hit.getEntity() instanceof net.minecraft.world.entity.player.Player player){hoverTarget=player;hoverAt=now;}
        }
        if(target!=null&&(target.isRemoved()||now-attackedAt>ModuleSettings.v("target","life")*1e9))target=null;
        if(hoverTarget!=null&&(hoverTarget.isRemoved()||!hoverTarget.isAlive()||now-hoverAt>ModuleSettings.v("targethud","hold")*1e9))hoverTarget=null;
        if(!mc.isPaused()){GameplayModules.tick();WorldModules.tick();}
    }
    private static void releaseSprint(){if(sprintOwned){var mc=Minecraft.getInstance();mc.options.keySprint.setDown(InputConstants.isKeyDown(mc.getWindow().getWindow(),mc.options.keySprint.getKey().getValue()));sprintOwned=false;}}
    private static void setFree(boolean active){
        var mc=Minecraft.getInstance();if(active==freeActive)return;freeActive=active;
        if(active&&mc.player!=null){freeYaw=mc.player.getYRot();freePitch=mc.player.getXRot();oldCamera=mc.options.getCameraType();if(ModuleSettings.b("free","third"))mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);}
        else if(oldCamera!=null){mc.options.setCameraType(oldCamera);oldCamera=null;}
    }
    public static void turn(LocalPlayer player,double x,double y){if(FreeCamera.active()){FreeCamera.turn(x,y);return;}if(freeActive){freeYaw+=(float)x*.15f;freePitch=Math.clamp(freePitch+(float)y*.15f,-90,90);}else player.turn(x,y);}
    private static void layers(RenderGuiLayerEvent.Pre e){
        if((ModuleSettings.on("cross")&&e.getName().equals(VanillaGuiLayers.CROSSHAIR))||(remove("effects")&&e.getName().equals(VanillaGuiLayers.EFFECTS)))e.setCanceled(true);
    }
    private static void overlay(RenderBlockScreenEffectEvent e){if(remove("fire")&&e.getOverlayType()==RenderBlockScreenEffectEvent.OverlayType.FIRE)e.setCanceled(true);}
    private static void fogColor(ViewportEvent.ComputeFogColor e){if(ModuleSettings.on("world")){int c=java.awt.Color.HSBtoRGB((float)ModuleSettings.v("world","hue"),.32f,.7f);e.setRed(((c>>16)&255)/255f);e.setGreen(((c>>8)&255)/255f);e.setBlue((c&255)/255f);}}
    private static void fog(ViewportEvent.RenderFog e){if(ModuleSettings.on("world")&&e.getType()==net.minecraft.world.level.material.FogType.NONE){e.setNearPlaneDistance((float)ModuleSettings.v("world","fog")*.25f);e.setFarPlaneDistance((float)ModuleSettings.v("world","fog"));e.setCanceled(true);}else if(remove("dark")&&e.getType()==net.minecraft.world.level.material.FogType.NONE){e.setNearPlaneDistance(Minecraft.getInstance().options.getEffectiveRenderDistance()*12);e.setFarPlaneDistance(Minecraft.getInstance().options.getEffectiveRenderDistance()*16);e.setCanceled(true);}}
}
