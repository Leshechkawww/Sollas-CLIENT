package com.example.examplemod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.*;

/** Three explicit local profile slots; Save overwrites only the chosen slot. */
public final class ProfilesScreen extends Screen {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private static final String[] NAMES={"PvP","Survival","Creative"};
    private final Screen parent;private String message="Save your current setup or load a slot.";
    private float scale,ox,oy; private final boolean[] exists=new boolean[3];
    private record Profile(int version,ClientSettings ui,ModuleSettings modules){}
    public ProfilesScreen(Screen parent){super(Component.literal("Sollas profiles"));this.parent=parent;}
    private Path path(int i){return FMLPaths.CONFIGDIR.get().resolve("sollas-profile-"+i+".json");}
    @Override protected void init(){
        scale=Math.min(1f,Math.min((width-16)/500f,(height-16)/310f));ox=(width-500*scale)/2;oy=(height-310*scale)/2;
        for(int i=0;i<3;i++){exists[i]=Files.exists(path(i));final int slot=i;int y=75+i*62;
            addRenderableWidget(new LiquidButton(270,y,95,36,14,()->"Save","",()->save(slot),()->false,false));
            addRenderableWidget(new LiquidButton(375,y,105,36,10,()->"Load","",()->load(slot),()->false,false));
        }
        addRenderableWidget(new LiquidButton(20,260,110,30,19,()->"Back","",this::onClose,()->false,false));
    }
    private void save(int i){try{Files.createDirectories(path(i).getParent());String data=JSON.toJson(new Profile(1,ClientSettings.INSTANCE,ModuleSettings.INSTANCE));Path temp=path(i).resolveSibling(path(i).getFileName()+".tmp");Files.writeString(temp,data);try{Files.move(temp,path(i),StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}catch(AtomicMoveNotSupportedException ex){Files.move(temp,path(i),StandardCopyOption.REPLACE_EXISTING);}exists[i]=true;message="Saved "+NAMES[i]+".";}catch(Exception e){message="Could not save profile. See log.";ExampleMod.LOGGER.error("Cannot save profile",e);}}
    private void load(int i){try{if(!Files.exists(path(i))){message="This slot is empty. Save it first.";return;}Profile p=JSON.fromJson(Files.readString(path(i)),Profile.class);if(p==null||p.version()!=1||p.ui()==null||p.modules()==null)throw new IllegalArgumentException("Invalid profile");
        ModuleSettings.restore(p.modules());ClientSettings.INSTANCE=p.ui();ClientSettings.INSTANCE.save();message="Loaded "+NAMES[i]+".";
    }catch(Exception e){message="Could not load profile. See log.";ExampleMod.LOGGER.error("Cannot load profile",e);}}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){if(minecraft.level==null)renderPanorama(g,dt);GlassRenderer.begin(g);g.fill(0,0,width,height,0x75081120);g.pose().pushPose();g.pose().translate(ox,oy,0);g.pose().scale(scale,scale,1);
        LiquidUi.surface(g,0,0,500,310,20,0xC0162237);LiquidUi.text(g,"Profiles",20,18,20,LiquidUi.TEXT,true);LiquidUi.text(g,"Save overwrites the selected slot",20,48,9,LiquidUi.MUTED,false);
        for(int i=0;i<3;i++){int y=75+i*62;LiquidUi.surface(g,20,y,235,36,10,0xA01B293E);LiquidUi.text(g,NAMES[i],33,y+8,11,LiquidUi.TEXT,true);LiquidUi.text(g,exists[i]?"Saved":"Empty",33,y+23,8,LiquidUi.MUTED,false);}
        LiquidUi.text(g,message,145,270,9,LiquidUi.MUTED,false);super.render(g,(int)((mx-ox)/scale),(int)((my-oy)/scale),dt);g.flush();g.pose().popPose();}
    @Override public boolean mouseClicked(double x,double y,int b){return super.mouseClicked((x-ox)/scale,(y-oy)/scale,b);}
    @Override public boolean mouseReleased(double x,double y,int b){return super.mouseReleased((x-ox)/scale,(y-oy)/scale,b);}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float dt){}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void onClose(){minecraft.setScreen(parent);}
}
