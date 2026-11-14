package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import java.util.Locale;

public final class ClickGuiScreen extends Screen {
    private final Screen parent;
    private int tab;
    private float scale,originX,originY,drawScale,drawX,drawY;
    private long opened=System.nanoTime(),tabAt=opened,closeAt;
    private boolean closing;
    private static final int W=610,H=392,CX=168,CW=420;
    public ClickGuiScreen(Screen parent){super(Component.literal("Sollas CLIENT"));this.parent=parent;}
    @Override protected void init(){
        scale=Math.min(1f,Math.min((width-16)/ (float)W,(height-16)/(float)H));
        originX=(width-W*scale)/2;originY=(height-H*scale)/2;
        drawScale=scale;drawX=originX;drawY=originY;
        addRenderableWidget(button(16,94,128,34,5,"Overview","",()->select(0),()->tab==0,false));
        addRenderableWidget(button(16,134,128,34,14,"Liquid style","",()->select(1),()->tab==1,false));
        addRenderableWidget(button(16,174,128,34,7,"HUD layout","",()->select(2),()->tab==2,false));
        addRenderableWidget(button(16,214,128,34,12,"Modules","",()->minecraft.setScreen(new ModulesScreen(this)),()->false,false));
        addRenderableWidget(button(16,254,128,34,6,"Profiles","",()->minecraft.setScreen(new ProfilesScreen(this)),()->false,false));
        addRenderableWidget(button(16,H-53,128,32,19,"Back","",this::onClose,()->false,false));
        var s=ClientSettings.INSTANCE;
        if(tab==0){
            addRenderableWidget(button(CX,91,CW,54,7,"Heads-up display","Your essentials, always in view",()->{s.hud=!s.hud;s.save();},()->s.hud,true));
            addRenderableWidget(button(CX,153,CW,54,12,"Block outline","A precise, depth-aware selection",()->ModuleSettings.toggle("block"),()->ModuleSettings.on("block"),true));
            addRenderableWidget(button(CX,215,CW,54,14,"Interface motion","Soft transitions and responsive controls",()->{s.animations=!s.animations;s.save();},()->s.animations,true));
        }else if(tab==1){
            String[] names={"Solid","Frosted","Liquid"};
            for(int i=0;i<3;i++){final int mode=i;addRenderableWidget(button(CX+i*142,94,136,34,mode==2?14:13,names[i],"",()->{s.glass=mode;s.save();},()->s.glass==mode,false));}
            addRenderableWidget(new Slider(CX,144,CW,0));
            addRenderableWidget(new Slider(CX,201,CW,1));
            String[] colors={"Iris","Glacier","Peach"};
            for(int i=0;i<3;i++){final int a=i;addRenderableWidget(button(CX+i*142,276,136,34,11,colors[i],"",()->{s.accent=a;s.save();},()->s.accent==a,false));}
        }else{
            addRenderableWidget(button(CX,91,207,42,0,"FPS","",()->{s.showFps=!s.showFps;s.save();},()->s.showFps,true));
            addRenderableWidget(button(CX+213,91,207,42,1,"BPS","",()->{s.showBps=!s.showBps;s.save();},()->s.showBps,true));
            addRenderableWidget(button(CX,141,207,42,2,"Nickname","",()->{s.showNick=!s.showNick;s.save();},()->s.showNick,true));
            addRenderableWidget(button(CX+213,141,207,42,3,"Time","",()->{s.showTime=!s.showTime;s.save();},()->s.showTime,true));
            addRenderableWidget(button(CX,191,207,42,4,"Coordinates","",()->{s.coordinates=!s.coordinates;s.save();},()->s.coordinates,true));
            addRenderableWidget(button(CX+213,191,207,42,8,"Reposition","",()->minecraft.setScreen(new HudEditorScreen(this)),()->false,false));
            addRenderableWidget(new Slider(CX,243,CW,2));
            addRenderableWidget(button(CX,294,CW,27,11,"HUD color","",()->minecraft.setScreen(new HudColorScreen(this)),()->false,false));
        }
    }
    private LiquidButton button(int x,int y,int w,int h,int icon,String title,String detail,Runnable action,java.util.function.Supplier<Boolean> selected,boolean toggle){
        return new LiquidButton(x,y,w,h,icon,()->title,detail,action,selected,toggle);
    }
    private void select(int i){if(tab==i)return;tab=i;tabAt=System.nanoTime();rebuildWidgets();}
    private static float ease(float v){v=Math.clamp(v,0,1);return 1-(1-v)*(1-v)*(1-v);}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        if(minecraft.level==null)renderPanorama(g,dt);
        GlassRenderer.begin(g);
        long now=System.nanoTime();
        float opening=ClientSettings.INSTANCE.animations?ease((now-opened)/240_000_000f):1;
        float progress=closing?1-ease((now-closeAt)/170_000_000f):opening;
        if(!ClientSettings.INSTANCE.animations)progress=closing?0:1;
        if(closing&&progress<=.001f){minecraft.setScreen(parent);return;}
        g.fill(0,0,width,height,((int)(progress*90)<<24)|0x080F20);
        drawScale=scale*(.965f+.035f*progress);drawX=(width-W*drawScale)/2;drawY=(height-H*drawScale)/2+(1-progress)*12;
        g.pose().pushPose();g.pose().translate(drawX,drawY,0);g.pose().scale(drawScale,drawScale,1);
        GlassRenderer.opacity=progress;
        LiquidUi.flat(g,3,7,W,H,22,0x48040A16);
        LiquidUi.surface(g,0,0,W,H,22,LiquidUi.SURFACE);
        LiquidUi.surface(g,8,8,144,H-16,16,0x9D121D30);
        
        LiquidUi.logo(g,23,26,26,ClientSettings.INSTANCE.color());
        LiquidUi.text(g,"Sollas CLIENT",57,34,10,LiquidUi.TEXT,true);
        
        LiquidUi.text(g,"WORKSPACE",22,76,8,LiquidUi.SOFT,true);
        LiquidUi.text(g,"Minecraft 1.21.1",22,H-91,9,LiquidUi.MUTED,false);
        LiquidUi.text(g,"NeoForge Edition",22,H-77,8,LiquidUi.SOFT,false);
        String[] titles={"A clearer view.","Light. Depth. Glass.","Every detail, yours."};
        String[] subtitles={"A quiet interface. Everything within reach.","Tune the surface to your world.","Choose your metrics. Find your balance."};
        float slide=ClientSettings.INSTANCE.animations?(1-ease((now-tabAt)/220_000_000f))*7:0;
        LiquidUi.text(g,titles[tab],CX+slide,29,22,LiquidUi.TEXT,true);
        LiquidUi.text(g,subtitles[tab],CX+slide,58,10,LiquidUi.MUTED,false);
        if(tab==1)LiquidUi.text(g,"ACCENT",CX,261,8,LiquidUi.MUTED,true);
        LiquidUi.surface(g,CX,326,CW,43,11,0x80162439);
        LiquidUi.icon(g,tab==2?8:14,CX+12,339,15,ClientSettings.INSTANCE.color());
        LiquidUi.text(g,tab==2?"Drag it into place":"Sollas CLIENT",CX+37,335,10,LiquidUi.TEXT,true);
        LiquidUi.text(g,tab==2?"Open Reposition to arrange your HUD.":"Right Shift to open  ·  Esc to return",CX+37,350,9,LiquidUi.MUTED,false);
        super.render(g,(int)((mx-drawX)/drawScale),(int)((my-drawY)/drawScale),dt);
        g.flush();GlassRenderer.opacity=1;g.pose().popPose();
    }
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float dt){}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){if(!closing){ClientSettings.INSTANCE.save();closeAt=System.nanoTime();closing=true;}}
    @Override public boolean keyPressed(int k,int scan,int mods){if(k==GLFW.GLFW_KEY_RIGHT_SHIFT){onClose();return true;}return !closing&&super.keyPressed(k,scan,mods);}
    @Override public boolean mouseClicked(double x,double y,int b){return !closing&&super.mouseClicked((x-drawX)/drawScale,(y-drawY)/drawScale,b);}
    @Override public boolean mouseReleased(double x,double y,int b){ClientSettings.INSTANCE.save();return super.mouseReleased((x-drawX)/drawScale,(y-drawY)/drawScale,b);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){return !closing&&super.mouseDragged((x-drawX)/drawScale,(y-drawY)/drawScale,b,dx/drawScale,dy/drawScale);}
    private static final class Slider extends AbstractSliderButton {
        private final int kind;
        private final LiquidUi.Motion hover=new LiquidUi.Motion(0),knob;
        Slider(int x,int y,int w,int kind){
            super(x,y,w,47,Component.empty(),kind==0?(ClientSettings.INSTANCE.blur-.5)/4.5:kind==1?ClientSettings.INSTANCE.refraction/16:(ClientSettings.INSTANCE.hudScale-.7)/.6);
            this.kind=kind;knob=new LiquidUi.Motion((float)value);updateMessage();
        }
        @Override protected void updateMessage(){setMessage(Component.literal(new String[]{"Blur softness","Edge refraction","HUD scale"}[kind]));}
        @Override protected void applyValue(){var s=ClientSettings.INSTANCE;if(kind==0)s.blur=(float)(.5+value*4.5);else if(kind==1)s.refraction=(float)value*16;else s.hudScale=(float)(.7+value*.6);}
        @Override public boolean keyPressed(int key,int scan,int mods){boolean done=super.keyPressed(key,scan,mods);if(done)ClientSettings.INSTANCE.save();return done;}
        @Override public void onRelease(double x,double y){super.onRelease(x,y);ClientSettings.INSTANCE.save();}
        @Override public void renderWidget(GuiGraphics g,int mx,int my,float dt){
            float hot=hover.to(isHoveredOrFocused()?1:0),v=knob.to((float)value);
            LiquidUi.surface(g,getX(),getY(),width,height,11,LiquidUi.mix(0x851B293E,0xAE30435E,hot));
            LiquidUi.text(g,getMessage().getString(),getX()+13,getY()+9,10,LiquidUi.TEXT,true);
            String amount=String.format(Locale.ROOT,kind==2?"%.0f%%":"%.1f",kind==0?.5+value*4.5:kind==1?value*16:70+value*60);
            LiquidUi.text(g,amount,getX()+width-14-LiquidUi.width(amount,10,true),getY()+9,10,ClientSettings.INSTANCE.color(),true);
            LiquidUi.flat(g,getX()+13,getY()+32,width-26,3,2,0xFF42516A);
            LiquidUi.flat(g,getX()+13,getY()+32,Math.max(3,(int)((width-26)*v)),3,2,ClientSettings.INSTANCE.color());
            LiquidUi.flat(g,getX()+9+(int)((width-26)*v),getY()+28,10,10,5,0xFFF2F6FF);
        }
    }
}
