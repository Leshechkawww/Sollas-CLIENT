package com.example.examplemod;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.Locale;

public final class ModulesScreen extends Screen {
    private final Screen parent;private ModuleSettings.Module module;
    private int page;private float scale,ox,oy;private String query="";private boolean searchFocus,waitingBind;
    private int category;private static final String[] CATEGORIES={"Combat","Movement","Player","World","Misc","Render"};
    public ModulesScreen(Screen parent){super(Component.literal("Sollas modules"));this.parent=parent;}
    @Override protected void init(){
        scale=Math.min(1f,Math.min((width-16)/610f,(height-16)/396f));ox=(width-610*scale)/2;oy=(height-396*scale)/2;
        addRenderableWidget(btn(18,18,92,28,19,"Back",()->{if(module!=null){module=null;page=0;rebuildWidgets();}else onClose();}));
        var filtered=ModuleSettings.ALL.stream().filter(m->(!query.isBlank() || categoryOf(m.id())==category) && m.title().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))).toList();
        if(module==null)for(int i=0;i<CATEGORIES.length;i++){final int selected=i;addRenderableWidget(new CategoryButton(18+i*96,55,91,28,CATEGORIES[i],()->{category=selected;query="";page=0;searchFocus=false;rebuildWidgets();},()->category==selected&&query.isBlank()));}
        int total=module==null?filtered.size():module.options().size(),start=page*6,end=Math.min(total,start+6);
        if(module==null){
            for(int i=start;i<end;i++){var m=filtered.get(i);int cell=i-start;
                addRenderableWidget(new LiquidButton(18+(cell%2)*292,100+(cell/2)*76,282,65,m.icon(),m::title,"Settings / "+(ModuleSettings.on(m.id())?"enabled":"disabled"),()->{module=m;page=0;rebuildWidgets();},()->ModuleSettings.on(m.id()),false));}
        }else{
            addRenderableWidget(new LiquidButton(480,18,112,30,13,()->ModuleSettings.on(module.id())?"Enabled":"Disabled","",()->ModuleSettings.toggle(module.id()),()->ModuleSettings.on(module.id()),true));
            addRenderableWidget(new LiquidButton(320,18,150,30,6,()->waitingBind?"Press key...":bindLabel(),"",()->{waitingBind=true;},()->waitingBind,false));
            if(module.id().equals("fake")&&page==0)addRenderableWidget(new LiquidButton(18,306,282,30,8,()->"Recreate in front","",()->{FakePlayerManager.recreate();},()->false,false));
            for(int i=start;i<end;i++){var o=module.options().get(i);int cell=i-start,x=18+(cell%2)*292,y=104+(cell/2)*70;
                if(o.modes()!=null)addRenderableWidget(new LiquidButton(x,y,282,52,6,()->o.label(),o.toggle()?"":o.modes()[(int)ModuleSettings.v(module.id(),o.key())],()->{ModuleSettings.set(module.id(),o,((int)ModuleSettings.v(module.id(),o.key())+1)%o.modes().length);ModuleSettings.save();rebuildWidgets();},()->ModuleSettings.v(module.id(),o.key())>.5,o.toggle()));
                else addRenderableWidget(new ValueSlider(x,y,282,module.id(),o));
            }
        }
        if(page>0)addRenderableWidget(btn(18,346,112,30,19,"Previous",()->{page--;rebuildWidgets();}));
        if(end<total)addRenderableWidget(btn(480,346,112,30,10,"Next",()->{page++;rebuildWidgets();}));
    }
    private static int categoryOf(String id){return switch(id){
        case "aura","aim","bowaim","totem","target","velocity","criticals" -> 0;
        case "sprint","fly","speed","noslow","step","jesus" -> 1;
        case "autoarmor","fake" -> 2;
        case "autofish","fastplace","fastbreak","scaffold","nuker","world" -> 3;
        case "remove" -> 4;
        case "freecam","xray","chestesp","tracers","hp","bright","view","cross","trails","block","free","kill","predict","sword","entity","hand","motion","glassinv","targethud" -> 5;
        default -> 4;
    };}
    private static final class CategoryButton extends net.minecraft.client.gui.components.AbstractWidget {
        private final Runnable action;private final java.util.function.Supplier<Boolean> selected;private final LiquidUi.Motion hover=new LiquidUi.Motion(0);
        CategoryButton(int x,int y,int w,int h,String title,Runnable action,java.util.function.Supplier<Boolean> selected){super(x,y,w,h,Component.literal(title));this.action=action;this.selected=selected;}
        @Override protected void renderWidget(GuiGraphics g,int mx,int my,float dt){float h=hover.to(isHoveredOrFocused()?1:0);int bg=LiquidUi.mix(0x98202D44,0xD3415077,selected.get()?1:h*.55f);LiquidUi.surface(g,getX(),getY(),width,height,8,bg);String title=getMessage().getString();float tw=LiquidUi.width(title,10,true);LiquidUi.text(g,title,getX()+(width-tw)/2,getY()+10,10,selected.get()?ClientSettings.INSTANCE.color():LiquidUi.TEXT,true);}
        @Override public void onClick(double x,double y){action.run();}
        @Override public boolean keyPressed(int k,int scan,int mods){if(isFocused()&&(k==org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER||k==org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE)){action.run();return true;}return super.keyPressed(k,scan,mods);}
        @Override protected void updateWidgetNarration(net.minecraft.client.gui.narration.NarrationElementOutput out){defaultButtonNarrationText(out);}
    }
    private String bindLabel(){int key=ModuleSettings.bind(module.id());if(key<0)return "Bind: none";String name=org.lwjgl.glfw.GLFW.glfwGetKeyName(key,0);return "Bind: "+(name==null?Integer.toString(key):name.toUpperCase(Locale.ROOT));}
    private LiquidButton btn(int x,int y,int w,int h,int icon,String label,Runnable run){return new LiquidButton(x,y,w,h,icon,()->label,"",run,()->false,false);}
    @Override public void render(GuiGraphics g,int mx,int my,float dt){
        if(minecraft.level==null)renderPanorama(g,dt);GlassRenderer.begin(g);g.fill(0,0,width,height,0x60081220);
        g.pose().pushPose();g.pose().translate(ox,oy,0);g.pose().scale(scale,scale,1);
        LiquidUi.surface(g,0,0,610,396,22,0xC0162237);
        LiquidUi.text(g,module==null?"Modules":module.title(),128,22,20,LiquidUi.TEXT,true);
                if(module!=null)LiquidUi.text(g,LiquidUi.fit(module.description(),570,10),18,65,10,LiquidUi.MUTED,false);
        else if(!query.isBlank())LiquidUi.text(g,"Searching all categories",18,86,8,LiquidUi.MUTED,false);
        if(module==null){LiquidUi.flat(g,320,18,272,30,8,searchFocus?0xCC34445F:0x99243249);LiquidUi.icon(g,10,328,25,12,LiquidUi.MUTED);LiquidUi.text(g,query.isEmpty()?"Click to search...":LiquidUi.fit(query,220,10),348,27,10,query.isEmpty()?LiquidUi.MUTED:LiquidUi.TEXT,false);}
        if(module==null && ModuleSettings.ALL.stream().noneMatch(m->(!query.isBlank()||categoryOf(m.id())==category)&&m.title().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))))LiquidUi.text(g,"No modules found",245,180,11,LiquidUi.MUTED,false);
        super.render(g,(int)((mx-ox)/scale),(int)((my-oy)/scale),dt);
        LiquidUi.text(g,"Page "+(page+1)+"   ·   All changes saved",195,356,9,LiquidUi.MUTED,false);
        g.flush();g.pose().popPose();
    }
        @Override public boolean mouseClicked(double x,double y,int b){double px=(x-ox)/scale,py=(y-oy)/scale;if(module==null&&px>=320&&px<=592&&py>=18&&py<=48){searchFocus=true;return true;}searchFocus=false;return super.mouseClicked(px,py,b);}
    @Override public boolean charTyped(char c,int mods){if(module==null&&searchFocus&&!Character.isISOControl(c)&&query.length()<48){query+=c;page=0;rebuildWidgets();return true;}return super.charTyped(c,mods);}
    @Override public boolean keyPressed(int key,int scan,int mods){
        if(waitingBind&&module!=null){ModuleSettings.bind(module.id(),(key==org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE||key==org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE||key==org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE)?-1:key);waitingBind=false;return true;}
        if(module==null&&searchFocus){if(key==org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE&&!query.isEmpty()){query=query.substring(0,query.length()-1);page=0;rebuildWidgets();return true;}if(key==org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE||key==org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER){searchFocus=false;return true;}}
        return super.keyPressed(key,scan,mods);
    }
    @Override public boolean mouseReleased(double x,double y,int b){ModuleSettings.save();return super.mouseReleased((x-ox)/scale,(y-oy)/scale,b);}
    @Override public boolean mouseDragged(double x,double y,int b,double dx,double dy){return super.mouseDragged((x-ox)/scale,(y-oy)/scale,b,dx/scale,dy/scale);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public void renderBackground(GuiGraphics g,int x,int y,float dt){}
    @Override public void onClose(){ModuleSettings.save();minecraft.setScreen(parent);}
    private static final class ValueSlider extends AbstractSliderButton {
        private final String id;private final ModuleSettings.Option option;
        ValueSlider(int x,int y,int w,String id,ModuleSettings.Option o){super(x,y,w,52,Component.literal(o.label()),(ModuleSettings.v(id,o.key())-o.min())/(o.max()-o.min()));this.id=id;option=o;}
        @Override protected void updateMessage(){}
        @Override protected void applyValue(){ModuleSettings.set(id,option,option.min()+value*(option.max()-option.min()));}
        @Override public void onRelease(double x,double y){super.onRelease(x,y);ModuleSettings.save();}
        @Override public boolean keyPressed(int key,int scan,int mods){boolean r=super.keyPressed(key,scan,mods);if(r)ModuleSettings.save();return r;}
        @Override public void renderWidget(GuiGraphics g,int mx,int my,float dt){
            LiquidUi.surface(g,getX(),getY(),width,height,10,isHoveredOrFocused()?0xBD334863:0x9C1B2D43);
            LiquidUi.text(g,option.label(),getX()+12,getY()+10,10,LiquidUi.TEXT,true);
            LiquidUi.text(g,String.format(Locale.ROOT,"%.2f",ModuleSettings.v(id,option.key())),getX()+width-58,getY()+10,9,ClientSettings.INSTANCE.color(),false);
            LiquidUi.flat(g,getX()+12,getY()+35,width-24,3,2,0xFF44516B);
            LiquidUi.flat(g,getX()+12,getY()+35,Math.max(3,(int)((width-24)*value)),3,2,ClientSettings.INSTANCE.color());
        }
    }
}
