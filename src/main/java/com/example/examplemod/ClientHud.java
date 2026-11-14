package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Cached telemetry and a measured, wrapping layout; no fixed-width nickname assumptions. */
public final class ClientHud {
    private static final DateTimeFormatter CLOCK=DateTimeFormatter.ofPattern("HH:mm:ss");
    private static long refreshed;
    private static Layout cachedLayout; private static int cachedViewport, cachedFlags; private static float cachedScale; private static long cachedRefresh;
    private static String fps="0",speed="0.0",name="Player",time="00:00:00",xyz="— / — / —";
    private static double px,pz,bps;
    private static UUID player;
    private static Object level;
    public record Bounds(int x,int y,int width,int height){}
    private record Tile(int icon,String label,String value,int x,int y,int width){}
    private record Layout(List<Tile> tiles,int width,int height,float scale){}
    public static void tick(){
        var mc=Minecraft.getInstance();
        if(mc.player==null || mc.isPaused()){player=null;level=null;bps=0;return;}
        var p=mc.player;
        if(!p.getUUID().equals(player)||level!=mc.level){px=p.getX();pz=p.getZ();player=p.getUUID();level=mc.level;bps=0;return;}
        double distance=Math.hypot(p.getX()-px,p.getZ()-pz);
        bps=distance>8?0:distance*20; // horizontal blocks / second, reject teleport spikes
        px=p.getX();pz=p.getZ();
    }
    private static void refresh(){
        long now=System.nanoTime();if(now-refreshed<200_000_000)return;refreshed=now;
        var mc=Minecraft.getInstance();fps=Integer.toString(mc.getFps());
        speed=String.format(Locale.ROOT,"%.1f",mc.player==null?0:bps);
        name=mc.player!=null?mc.player.getGameProfile().getName():mc.getUser().getName();
        time=LocalTime.now().format(CLOCK);
        xyz=mc.player==null?"— / — / —":mc.player.getBlockX()+" / "+mc.player.getBlockY()+" / "+mc.player.getBlockZ();
    }
    private static Layout layout(int viewport){
        refresh();var s=ClientSettings.INSTANCE;
        int flags=(s.showFps?1:0)|(s.showBps?2:0)|(s.showNick?4:0)|(s.showTime?8:0)|(s.coordinates?16:0);
        if(cachedLayout!=null && cachedViewport==viewport && cachedFlags==flags && cachedScale==s.hudScale && cachedRefresh==refreshed)return cachedLayout;
        float scale=Math.min(s.hudScale,Math.max(.45f,(viewport-16)/190f));
        int max=Math.min(570,Math.max(190,(int)((viewport-16)/scale)));
        List<Tile> tiles=new ArrayList<>();int x=0,y=0,total=0;
        List<String[]> values=new ArrayList<>();
        values.add(new String[]{"14","", "Sollas CLIENT"});
        if(s.showFps)values.add(new String[]{"0","FPS",fps});
        if(s.showBps)values.add(new String[]{"1","BPS",speed});
        if(s.showNick)values.add(new String[]{"2","PLAYER",LiquidUi.fit(name,125,12)});
        if(s.showTime)values.add(new String[]{"3","TIME",time});
        if(s.coordinates)values.add(new String[]{"4","XYZ",xyz});
        for(String[] v:values){
            int w=Math.min(max,Math.max(72,43+(int)Math.ceil(Math.max(LiquidUi.width(v[1],8,true),LiquidUi.width(v[2],12,true)))));
            if(x>0&&x+w>max){x=0;y+=47;}
            tiles.add(new Tile(Integer.parseInt(v[0]),v[1],v[2],x,y,w));total=Math.max(total,x+w);x+=w+6;
        }
        cachedViewport=viewport;cachedFlags=flags;cachedScale=s.hudScale;cachedRefresh=refreshed;
        return cachedLayout=new Layout(tiles,total,y+41,scale);
    }
    public static Bounds bounds(int viewport,int height){
        Layout l=layout(viewport);var s=ClientSettings.INSTANCE;
        int w=(int)Math.ceil(l.width*l.scale),h=(int)Math.ceil(l.height*l.scale);
        return new Bounds(Math.clamp(s.hudX,4,Math.max(4,viewport-w-4)),Math.clamp(s.hudY,4,Math.max(4,height-h-4)),w,h);
    }
    public record TileBounds(String id,int x,int y,int width,int height){}
    public static java.util.List<TileBounds> tileBounds(int viewport,int height){
        Layout l=layout(viewport);Bounds origin=bounds(viewport,height);var s=ClientSettings.INSTANCE;
        if(s.hudPositions==null)s.hudPositions=new java.util.HashMap<>();
        var result=new java.util.ArrayList<TileBounds>();
        for(Tile t:l.tiles){
            String id=Integer.toString(t.icon);int w=(int)Math.ceil(t.width*l.scale),h=(int)Math.ceil(41*l.scale);
            int x=origin.x+(int)(t.x*l.scale),y=origin.y+(int)(t.y*l.scale);float[] p=s.hudPositions.get(id);
            if(p!=null&&p.length==2&&Float.isFinite(p[0])&&Float.isFinite(p[1])){x=Math.round(p[0]*Math.max(1,viewport-w));y=Math.round(p[1]*Math.max(1,height-h));}
            result.add(new TileBounds(id,Math.clamp(x,0,Math.max(0,viewport-w)),Math.clamp(y,0,Math.max(0,height-h)),w,h));
        }return result;
    }
    public static void place(String id,int x,int y,int viewport,int height,int w,int h){
        var s=ClientSettings.INSTANCE;if(s.hudPositions==null)s.hudPositions=new java.util.HashMap<>();
        s.hudPositions.put(id,new float[]{Math.clamp(x/(float)Math.max(1,viewport-w),0,1),Math.clamp(y/(float)Math.max(1,height-h),0,1)});
    }
    public static void render(GuiGraphics g,boolean preview){
        Layout l=layout(g.guiWidth());var bounds=tileBounds(g.guiWidth(),g.guiHeight());
        for(int i=0;i<l.tiles.size();i++){Tile t=l.tiles.get(i);TileBounds b=bounds.get(i);
            g.pose().pushPose();g.pose().translate(b.x,b.y,0);g.pose().scale(l.scale,l.scale,1);
            LiquidUi.surface(g,0,0,t.width,41,12,0xBF152238);
            if(t.label.isEmpty())LiquidUi.logo(g,10,12,16,ClientSettings.INSTANCE.hudColor());else LiquidUi.icon(g,t.icon,10,12,16,ClientSettings.INSTANCE.hudColor());
            LiquidUi.text(g,t.label,33,7,8,LiquidUi.mix(ClientSettings.INSTANCE.hudColor(),0xFFFFFFFF,.35f),true);
            LiquidUi.text(g,t.value,33,20,12,ClientSettings.INSTANCE.hudColor(),true);
            g.flush();g.pose().popPose();
        }
    }
}