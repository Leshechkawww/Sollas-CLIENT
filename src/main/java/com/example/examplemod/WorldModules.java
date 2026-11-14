package com.example.examplemod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.*;
import java.util.*;

public final class WorldModules {
    private record Trail(Vec3 p,long time){}
    private static final class Drop {Vec3 p,v;long birth;boolean grounded;float size;Drop(Vec3 p,Vec3 v,float size){this.p=p;this.v=v;this.size=size;birth=System.nanoTime();}}
    private static final class Piece {Vec3 p,v;long birth;int part;ResourceLocation skin;Piece(Vec3 p,Vec3 v,int part,ResourceLocation skin){this.p=p;this.v=v;this.part=part;this.skin=skin;birth=System.nanoTime();}}
    private static final Deque<Trail> trail=new ArrayDeque<>();private static final List<Drop> drops=new ArrayList<>();private static final List<Piece> pieces=new ArrayList<>();
    private static final Set<Integer> killed=new HashSet<>();private static final Random random=new Random();
    private static Vec3 previous;private static long bloodAt;
    private static final ModelPart[] PARTS=makeParts();
    public static void forgetTestEntity(int id){killed.remove(id);}
    public static void reset(){trail.clear();drops.clear();pieces.clear();killed.clear();previous=null;bloodAt=0;}
    public static boolean hideCorpse(LivingEntity entity){return ModuleSettings.on("kill")&&ModuleSettings.b("kill","parts")&&killed.contains(entity.getId())&&entity.deathTime>0;}
    public static void tick(){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;long now=System.nanoTime();Vec3 p=mc.player.position().add(0,.08,0);
        if(previous==null||previous.distanceToSqr(p)>64){trail.clear();previous=p;}
        if(ModuleSettings.on("trails")&&p.distanceToSqr(previous)>.0009){int count=(int)ModuleSettings.v("trails","density");for(int i=1;i<=count;i++)trail.addLast(new Trail(previous.lerp(p,i/(double)count),now));}
        previous=p;while(!trail.isEmpty()&&(now-trail.peekFirst().time>ModuleSettings.v("trails","life")*1e9||trail.size()>480))trail.removeFirst();
        if(!ModuleSettings.on("trails"))trail.clear();
        var target=ModuleRuntime.target;
        if(ModuleSettings.on("kill")&&target!=null&&(target.deathTime>0||target.getHealth()<=0)&&killed.add(target.getId()))explode(target);
        double life=ModuleSettings.v("kill","life")*1e9;
        drops.removeIf(d->now-d.birth>life);pieces.removeIf(d->now-d.birth>life);
        for(Drop d:drops){if(d.grounded)continue;Vec3 next=d.p.add(d.v);var hit=mc.level.clip(new ClipContext(d.p,next,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));
            if(hit.getType()!=HitResult.Type.MISS){d.p=hit.getLocation().add(0,.008,0);d.grounded=hit.getDirection()==Direction.UP;d.v=Vec3.ZERO;d.size=.10f+random.nextFloat()*.18f;}else{d.p=next;d.v=d.v.scale(.98).add(0,-.035,0);}}
        for(Piece part:pieces){Vec3 next=part.p.add(part.v);var hit=mc.level.clip(new ClipContext(part.p,next.add(0,-.15,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,mc.player));
            if(hit.getType()!=HitResult.Type.MISS){part.p=hit.getLocation().add(0,.16,0);part.v=new Vec3(part.v.x*.6,Math.abs(part.v.y)*.2,part.v.z*.6);if(part.v.lengthSqr()<.002)part.v=Vec3.ZERO;}else{part.p=next;part.v=part.v.scale(.97).add(0,-.04,0);}}
        if(!ModuleSettings.on("kill")){pieces.clear();drops.clear();}
    }
    private static void explode(LivingEntity entity){
        Vec3 center=entity.position().add(0,entity.getBbHeight()*.55,0);bloodAt=System.nanoTime();
        if(ModuleSettings.b("kill","parts")){
            ResourceLocation skin=entity instanceof AbstractClientPlayer p?p.getSkin().texture():null;
            double[] ys={1.65,1.1,1.1,1.1,.4,.4};double[] xs={0,0,-.38,.38,-.13,.13};
            for(int i=0;i<6;i++)pieces.add(new Piece(entity.position().add(xs[i],ys[i]*entity.getBbHeight()/1.8,0),new Vec3((random.nextDouble()-.5)*.35,.12+random.nextDouble()*.2,(random.nextDouble()-.5)*.35),i,skin));
        }
        for(int i=0;i<(int)ModuleSettings.v("kill","amount")&&drops.size()<320;i++)drops.add(new Drop(center,new Vec3((random.nextDouble()-.5)*.7,random.nextDouble()*.35,(random.nextDouble()-.5)*.7),.035f));
        while(pieces.size()>60)pieces.removeFirst();
    }
    public static void render(RenderLevelStageEvent e){
        var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null)return;
        if(e.getStage()==RenderLevelStageEvent.Stage.AFTER_SKY){ModulePostFx.sky(e);return;}
        if(e.getStage()==RenderLevelStageEvent.Stage.AFTER_LEVEL){ModulePostFx.motion(e); renderOverlays(e); return;}
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        int color=ClientSettings.INSTANCE.color();long now=System.nanoTime();double time=now/1e9;Vec3 camera=e.getCamera().getPosition();
        WorldDraw.begin(camera);
        if(ModuleSettings.on("trails")&&(!mc.options.getCameraType().isFirstPerson()||ModuleSettings.b("trails","first"))){
            Trail last=null;float width=(float)ModuleSettings.v("trails","width");int mode=ModuleSettings.mode("trails");
            int colorMode=(int)ModuleSettings.v("trails","colorMode");float hue=(float)ModuleSettings.v("trails","hue"),saturation=(float)ModuleSettings.v("trails","saturation"),opacity=(float)ModuleSettings.v("trails","opacity");
            double lifetime=ModuleSettings.v("trails","life")*1e9;
            int index=0;
            for(Trail t:trail){float fade=Math.clamp(1-(float)((now-t.time)/lifetime),0,1)*opacity;
                float h=colorMode==2?(float)((time*.08+index*.012)%1):hue;
                int trailColor=colorMode==0?color:0xFF000000|java.awt.Color.HSBtoRGB(h,saturation,1f);
                Vec3 delta=last==null?Vec3.ZERO:t.p.subtract(last.p);
                Vec3 side=delta.horizontalDistanceSqr()<1e-10?new Vec3(width,0,0):new Vec3(-delta.z,0,delta.x).normalize().scale(width);
                if((mode==0||mode==6)&&last!=null){
                    WorldDraw.quad(last.p.subtract(side),t.p.subtract(side),t.p.add(side),last.p.add(side),WorldDraw.alpha(trailColor,fade*.35f),WorldDraw.alpha(trailColor,fade*.35f));
                    WorldDraw.line(last.p,t.p,WorldDraw.alpha(trailColor,fade));
                    if(mode==6&&index%3==0)WorldDraw.glow(t.p,width*.65f,WorldDraw.alpha(trailColor,fade*.35f));
                }else if(mode==1)WorldDraw.glow(t.p.add(Math.sin(t.time*.00000003)*.15,fade*.4,Math.cos(t.time*.00000003)*.15),width*.4f,WorldDraw.alpha(trailColor,fade*.7f));
                else if(mode==2)WorldDraw.glow(t.p.add(0,(1-fade)*.5,0),width*(1.5f-fade*.5f),WorldDraw.alpha(trailColor,fade*.16f));
                else if(mode==3&&last!=null){
                    double a=t.time*1e-9*8,b=last.time*1e-9*8;
                    for(int strand=0;strand<2;strand++){double offset=strand*Math.PI;
                        Vec3 p=t.p.add(Math.cos(a+offset)*width,.3+Math.sin(a+offset)*width,Math.sin(a+offset)*width);
                        Vec3 q=last.p.add(Math.cos(b+offset)*width,.3+Math.sin(b+offset)*width,Math.sin(b+offset)*width);
                        WorldDraw.line(q,p,WorldDraw.alpha(trailColor,fade));if(index%5==0)WorldDraw.glow(p,width*.45f,WorldDraw.alpha(trailColor,fade*.55f));
                    }
                }else if(mode==4&&last!=null){WorldDraw.line(last.p.subtract(side),t.p.subtract(side),WorldDraw.alpha(trailColor,fade));WorldDraw.line(last.p.add(side),t.p.add(side),WorldDraw.alpha(trailColor,fade));}
                else if(mode==5&&index%2==0){double a=t.time*1e-9*11;Vec3 spark=t.p.add(Math.sin(a)*width,(1-fade)*width*2,Math.cos(a*1.3)*width);WorldDraw.glow(spark,width*(.25f+.3f*fade),WorldDraw.alpha(trailColor,fade*.8f));}
                last=t;index++;
            }
        }
        if(ModuleSettings.on("block")&&mc.hitResult instanceof BlockHitResult hit&&hit.getType()==HitResult.Type.BLOCK){
            var pos=hit.getBlockPos();var shape=mc.level.getBlockState(pos).getShape(mc.level,pos);int mode=ModuleSettings.mode("block");double phase=time*ModuleSettings.v("block","speed");
            shape.forAllBoxes((x1,y1,z1,x2,y2,z2)->{
                AABB box=new AABB(x1+pos.getX(),y1+pos.getY(),z1+pos.getZ(),x2+pos.getX(),y2+pos.getY(),z2+pos.getZ()).inflate(.003);
                WorldDraw.box(box,color,mode==1||mode==3?(float)ModuleSettings.v("block","alpha")*(mode==3?(float)(.5+.5*Math.sin(phase*3)):1):0,true);
                if(mode==2){Vec3 last=null;for(int i=0;i<80;i++){double a=i*.15+phase*2;Vec3 q=new Vec3(box.getCenter().x+Math.cos(a)*box.getXsize()*.55,box.minY+box.getYsize()*i/80,box.getCenter().z+Math.sin(a)*box.getZsize()*.55);if(last!=null)WorldDraw.line(last,q,color);last=q;}}
            });
        }
        if(ModuleSettings.on("entity")){
            int count=0;double range=ModuleSettings.v("entity","range");
            for(Entity entity:mc.level.entitiesForRendering()){
                if(entity==mc.player||!entity.isAlive()||entity.distanceToSqr(mc.player)>range*range||count>=96)continue;
                boolean friend=ModuleSettings.INSTANCE.friends.contains(entity.getUUID().toString());
                boolean show=entity instanceof Player?(friend?ModuleSettings.b("entity","friends"):ModuleSettings.b("entity","players")):entity instanceof ItemEntity?ModuleSettings.b("entity","items"):entity instanceof LivingEntity&&ModuleSettings.b("entity","mobs");
                if(!show||!e.getFrustum().isVisible(entity.getBoundingBox()))continue;count++;int c=friend?0xFF6FE4B8:color;
                Vec3 interp=entity.getPosition(e.getPartialTick().getGameTimeDeltaPartialTick(false));AABB box=entity.getBoundingBox().move(interp.subtract(entity.position())).inflate(.025);
                if(ModuleSettings.mode("entity")==2)WorldDraw.ring(interp,entity.getBbWidth()*.8,.06,c);else WorldDraw.box(box,c,ModuleSettings.mode("entity")==1?(float)ModuleSettings.v("entity","alpha"):0,true);
            }
        }
        var target=ModuleRuntime.target;
        if(ModuleSettings.on("target")&&target!=null&&target.isAlive()){
            Vec3 p=target.getPosition(e.getPartialTick().getGameTimeDeltaPartialTick(false));int mode=ModuleSettings.mode("target");double speed=ModuleSettings.v("target","speed");
            if(mode==0){for(int ghost=0;ghost<3;ghost++)for(int j=0;j<14;j++){
                double a=time*2*speed+ghost*Math.PI*2/3-j*.055;double h=target.getBbHeight()*.5;Vec3 q=p.add(Math.cos(a)*.8,h+Math.sin(a*1.7+ghost)*h*.8,Math.sin(a)*.8);
                WorldDraw.glow(q,.10f+(14-j)*.012f,WorldDraw.alpha(color,(1-j/14f)*.75f));}}
            else if(mode==1)WorldDraw.ring(p,target.getBbWidth()*.9,(Math.sin(time*2*speed)*.5+.5)*target.getBbHeight(),color);
            else WorldDraw.box(target.getBoundingBox().inflate(.04+.03*Math.sin(time*4)),color,.12f,true);
        }
        GameplayModules.tracers(e);
        Prediction.update();if(ModuleSettings.on("predict")){for(var path:Prediction.paths){var pts=path.points();for(int i=1;i<pts.size();i++)WorldDraw.line(pts.get(i-1),pts.get(i),WorldDraw.alpha(color,.85f));if(path.hit()!=null&&ModuleSettings.b("predict","landing")){WorldDraw.ring(path.hit().getLocation().add(0,.015,0),.22,0,color);WorldDraw.glow(path.hit().getLocation(),.1f,WorldDraw.alpha(color,.7f));}}}
        if(ModuleSettings.on("kill"))for(Drop d:drops){float fade=1-(float)((now-d.birth)/(ModuleSettings.v("kill","life")*1e9));if(d.grounded){if(ModuleSettings.b("kill","pools"))WorldDraw.pool(d.p,d.size,WorldDraw.alpha(0xFF890D22,fade*.8f));}else WorldDraw.glow(d.p,d.size,WorldDraw.alpha(0xFFBC1630,fade));}
        if(ModuleSettings.on("kill"))for(Piece p:pieces)if(p.skin==null)WorldDraw.box(new AABB(p.p.x-.09,p.p.y-.12,p.p.z-.09,p.p.x+.09,p.p.y+.12,p.p.z+.09),0xFF9F3740,.6f,false);
        WorldDraw.flush();renderParts(e);
    }
    private static void renderOverlays(RenderLevelStageEvent e){
        if(!ModuleSettings.on("xray")&&!ModuleSettings.on("chestesp"))return;
        var stack=com.mojang.blaze3d.systems.RenderSystem.getModelViewStack();stack.pushMatrix();
        try {stack.mul(e.getModelViewMatrix());com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();WorldDraw.begin(e.getCamera().getPosition());OreScanner.render();WorldDraw.flush();}
        finally {stack.popMatrix();com.mojang.blaze3d.systems.RenderSystem.applyModelViewMatrix();}
    }
    private static ModelPart[] makeParts(){
        int[][] spec={{8,8,8,0,0},{8,12,4,16,16},{4,12,4,40,16},{4,12,4,32,48},{4,12,4,0,16},{4,12,4,16,48}};ModelPart[] out=new ModelPart[6];
        for(int i=0;i<6;i++){int[] s=spec[i];var mesh=new MeshDefinition();mesh.getRoot().addOrReplaceChild("part",CubeListBuilder.create().texOffs(s[3],s[4]).addBox(-s[0]/2f,-s[1]/2f,-s[2]/2f,s[0],s[1],s[2]),PartPose.ZERO);out[i]=LayerDefinition.create(mesh,64,64).bakeRoot().getChild("part");}return out;
    }
    private static void renderParts(RenderLevelStageEvent e){if(!ModuleSettings.on("kill"))return;var buffers=Minecraft.getInstance().renderBuffers().bufferSource();var cam=e.getCamera().getPosition();
        for(Piece p:pieces)if(p.skin!=null){PoseStack pose=new PoseStack();pose.translate(p.p.x-cam.x,p.p.y-cam.y,p.p.z-cam.z);float t=(System.nanoTime()-p.birth)/1e9f;pose.mulPose(Axis.XP.rotation(t*2+p.part));pose.mulPose(Axis.ZP.rotation(t+p.part));pose.scale(1,-1,1);PARTS[p.part].render(pose,buffers.getBuffer(RenderType.entityTranslucent(p.skin)),LightTexture.FULL_BRIGHT,OverlayTexture.NO_OVERLAY);}
        buffers.endBatch();
    }
    private static void renderTargetHud(RenderLevelStageEvent e){
        var mc=Minecraft.getInstance();var target=ModuleRuntime.hoverTarget;if(!ModuleSettings.on("targethud")||target==null||!target.isAlive()||target.distanceTo(mc.player)>ModuleSettings.v("targethud","range"))return;
        Vec3 p=target.getPosition(e.getPartialTick().getGameTimeDeltaPartialTick(false)).add(0,target.getBbHeight()+ModuleSettings.v("targethud","height"),0).subtract(e.getCamera().getPosition());
        PoseStack pose=new PoseStack();pose.translate(p.x,p.y,p.z);pose.mulPose(e.getCamera().rotation());pose.scale(.025f,-.025f,.025f);
        String name=target.getDisplayName().getString();String hp=String.format(Locale.ROOT,"%.1f / %.1f HP",target.getHealth(),target.getMaxHealth());
        int width=Math.max(mc.font.width(name),mc.font.width(hp))+16;var buffers=mc.renderBuffers().bufferSource();
        mc.font.drawInBatch(name,-width/2f+8,-18,0xFFF4F6FF,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0xAA142238,LightTexture.FULL_BRIGHT);
        mc.font.drawInBatch(hp,-width/2f+8,-7,ClientSettings.INSTANCE.color(),false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0xAA142238,LightTexture.FULL_BRIGHT);buffers.endBatch();
    }
    public static void screenBlood(GuiGraphics g){float fade=Math.max(0,1-(System.nanoTime()-bloodAt)/2_000_000_000f);if(fade<=0)return;for(int i=0;i<7;i++){int x=(int)(g.guiWidth()*(i%2==0?.08+.04*i:.94-.025*i));int y=(int)(g.guiHeight()*(.12+i*.09)+(1-fade)*14);LiquidUi.flat(g,x,y,3+i%3,9+i%7,3,WorldDraw.alpha(0xFF9B122C,fade*.6f));}}
}
