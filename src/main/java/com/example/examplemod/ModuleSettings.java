package com.example.examplemod;

import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.Files;
import java.util.*;

public final class ModuleSettings {
    public record Option(String key,String label,double initial,double min,double max,String[] modes) {
        public boolean toggle(){return modes!=null&&modes.length==2&&modes[0].equals("Off");}
    }
    public record Module(String id,String title,String description,int icon,List<Option> options){}
    private Map<String,Boolean> enabled=new HashMap<>();
    private Map<String,Double> values=new HashMap<>();
    private Map<String,Integer> binds=new HashMap<>();
    public Set<String> friends=new HashSet<>();
    public static final List<Module> ALL=List.of(
        mod("aura","KillAura","Attacks visible targets at attack cooldown",12,n("range","Range",3,1,4.5),b("players","Players",true),b("mobs","Mobs",true)),
        mod("aim","Aimbot","Smooth aim at visible targets",13,n("range","Range",12,2,40),n("smooth","Aim speed",.25,.05,1),b("players","Players",true),b("mobs","Mobs",true)),
        mod("bowaim","Bow Aimbot","Aim while drawing a bow",13,n("range","Range",30,4,64),n("smooth","Aim speed",.2,.05,1)),
        mod("totem","Auto Totem","Moves a totem into offhand",12,n("health","Health threshold",10,1,20)),
        mod("autoarmor","Auto Armor","Equips armor into empty slots",12),
        mod("autofish","Auto Fish","Reel when a bite is detected",12),
        mod("fastplace","Fast Place","Reduce placement delay",12,n("delay","Delay ticks",1,0,4)),
        mod("xray","X-Ray","Highlights loaded ores through walls",12,n("radius","Scan radius",12,6,24),b("diamond","Diamond",true),b("ancient","Ancient debris",true),b("other","Other ores",false)),
        mod("chestesp","Chest ESP","Highlights loaded containers through walls",12,n("range","Range",48,8,96)),
        mod("tracers","Tracers","Lines to nearby players and mobs",13,n("range","Range",48,8,128),b("players","Players",true),b("mobs","Mobs",false)),
        mod("criticals","Criticals","Wait for a vanilla falling strike",12),
        mod("freecam","FreeCam","Local detached camera in loaded chunks",13,n("speed","Camera speed",.45,.1,2)),
        mod("jesus","Jesus","Walk on fluid surfaces client-side",1,b("lava","Include lava",false)),
        mod("step","Step","Increase client step height",1,n("height","Step height",1,0.6,2)),
        mod("nuker","Nuker","Mine nearby blocks matching held block",12,n("radius","Radius",3,1,4)),
        mod("velocity","Velocity","Scale server velocity updates",12,n("horizontal","Horizontal",0,0,1),n("vertical","Vertical",0,0,1)),
        mod("fly","Fly","Client movement; server validation still applies",1,n("speed","Flight speed",.3,.05,1)),
        mod("speed","Speed","Set horizontal movement speed",1,n("speed","Blocks per tick",.32,.1,.8)),
        mod("noslow","No Slowdown","Remove item-use movement penalty",1),
        mod("fastbreak","Fast Break","Remove delay between blocks",12),
        mod("scaffold","Scaffold","Place held blocks below using normal interactions",12,n("delay","Delay ticks",4,1,12)),
        mod("sprint","Auto sprint","Vanilla sprint key; releases after attacks",1, n("delay","Attack release ticks",4,1,12)),
        mod("hp","HP indication","Red screen edges when health is low",0,n("threshold","Health threshold",10,2,20),n("strength","Vignette strength",.65,.1,1)),
        mod("bright","Full bright","Client lightmap brightness",14,n("strength","Brightness",.8,0,1)),
        mod("view","View model","Open chat: choose a hand, drag; wheel = depth",8,n("rx","Right X",0,-1.5,1.5),n("ry","Right Y",0,-1.5,1.5),n("rz","Right Z",0,-1.5,1.5),n("lx","Left X",0,-1.5,1.5),n("ly","Left Y",0,-1.5,1.5),n("lz","Left Z",0,-1.5,1.5)),
        mod("cross","Crosshair","Pulse cross, orbit ring or focus dot",5,m("style","Style","Pulse cross","Orbit ring","Focus dot"),n("size","Size",5,2,14),n("width","Thickness",1,1,3),n("gap","Gap",3,0,10),b("theme","Theme color",true),n("hue","Custom hue",.5,0,1),b("animate","Attack animation",true)),
        mod("trails","Trails","Animated trails with customizable colors",14,m("style","Style","Ribbon","Particles","Glass cloud","Neon helix","Twin streak","Comet sparks","Prism ribbon"),m("colorMode","Color mode","Theme","Custom","Rainbow"),n("hue","Color hue",.75,0,1),n("saturation","Color saturation",.8,0,1),n("opacity","Opacity",.8,.1,1),n("life","Lifetime seconds",1.4,.3,4),n("width","Width",.25,.05,.8),n("density","Density",2,1,6),b("first","Show in first person",false)),
        mod("block","Block outline","Animated outline, glow fill or spiral",12,m("style","Style","Outline","Glow","Spiral","Wave"),n("alpha","Fill opacity",.18,.02,.6),n("speed","Animation speed",1,.1,3)),
        mod("free","Freelook","Independent camera; bind in Controls",13,m("mode","Activation","Hold","Toggle"),b("third","Third-person while active",true)),
        mod("kill","Kill effect","Animated fragments, droplets and floor decals",14,n("life","Lifetime seconds",4,1,10),n("amount","Blood droplets",28,0,80),b("parts","Body fragments",true),b("pools","Ground splashes",true),b("screen","Screen droplets",true)),
        mod("predict","Prediction","Vanilla projectile physics and collision preview",10,n("steps","Simulation ticks",180,40,300),b("entities","Entity collisions",true),b("landing","Landing marker",true)),
        mod("target","Target ESP","Visualize the most recently attacked entity",13,m("style","Style","Three spirits","Orbit ring","Pulse box"),n("life","Target timeout seconds",6,1,15),n("speed","Orbit speed",1,.2,3)),
        mod("remove","Removals","Visual-only filters; server effects still apply",9,b("fire","Fire overlay",true),b("particles","Particles",false),b("nausea","Nausea and portal wobble",true),b("dark","Blindness and darkness visuals",true),b("hurt","Hurt camera shake",true),b("bob","Walking camera bob",false),b("eat","Eating hand bob",false),b("sky","Sky",false),b("clouds","Clouds",false),b("shadows","Entity shadows",false),b("effects","Effect icons",false),b("wither","Wither heart appearance",false)),
        mod("sword","Sword animation","Replace first-person item swing transforms",12,m("style","Style","Slowed","Plastic","To me","To target","Fast","Horizontal","Vertical"),n("strength","Motion strength",1,.2,2)),
        mod("entity","Entity ESP","Depth-tested category highlights",12,m("style","Style","Box","Gradient fill","Halo"),b("through","Through walls",true),b("friends","Friends",true),b("players","Other players",true),b("mobs","Mobs",false),b("items","Dropped items",false),n("range","Range blocks",40,8,96),n("alpha","Fill opacity",.2,.03,.6)),
        mod("hand","Glow hand","Screen-space hand silhouette and animated fill",14,m("style","Style","Outline glow","Liquid fill"),n("alpha","Opacity",.45,.05,.9),n("width","Glow radius",3,1,6)),
        mod("world","Custom world","Client time, fog tint and animated skies",16,n("time","Time of day",18000,0,23999),n("hue","Fog hue",.61,0,1),n("fog","Fog distance",128,24,256),m("style","Sky style","Atmosphere","Normal","Milky Way","Aurora","Starfall")),
        mod("motion","Motion blur","Depth-reconstructed camera velocity blur",1,n("strength","Shutter strength",.45,.05,1),n("limit","Max blur pixels",18,3,40)),
        mod("glassinv","Glass inventory","Liquid panels for inventory and chests",14,n("tint","Panel tint",.55,.15,.9),n("slots","Slot opacity",.35,.1,.8)),
        mod("fake","Fake Player","Client-only test target; attack never reaches server",2,n("distance","Spawn distance",3,1.5,6),n("damage","Damage per hit",4,1,20),n("health","Starting health",20,1,20),m("equipment","Armor","Copy yours","None","Iron","Diamond","Netherite")),
        mod("targethud","Target HUD","Player card below the crosshair",2,n("hold","Keep visible seconds",2,.2,6),n("height","Distance below crosshair",.5,.2,1.4),n("offsetX","Horizontal offset",0,-200,200),n("scale","Scale",1,.5,1.6),m("colorMode","Color","HUD color","Custom"),n("hue","Custom hue",.7,0,1),n("range","Maximum distance",32,6,64))
    );
    private static final Map<String,Option> OPTIONS=new HashMap<>();
    static {for(Module m:ALL)for(Option o:m.options)OPTIONS.put(m.id+"."+o.key,o);}
    public static final ModuleSettings INSTANCE=load();
    private static Option n(String k,String l,double v,double min,double max){return new Option(k,l,v,min,max,null);}
    private static Option b(String k,String l,boolean v){return new Option(k,l,v?1:0,0,1,new String[]{"Off","On"});}
    private static Option m(String k,String l,String... v){return new Option(k,l,0,0,v.length-1,v);}
    private static Module mod(String id,String name,String desc,int icon,Option... options){return new Module(id,name,desc,icon,List.of(options));}
    public static boolean on(String id){return INSTANCE.enabled.getOrDefault(id,id.equals("targethud")||id.equals("glassinv"));}
    public static void toggle(String id){boolean state=!on(id);INSTANCE.enabled.put(id,state);save();ModuleNotifications.push(ALL.stream().filter(m->m.id.equals(id)).map(Module::title).findFirst().orElse(id),state);}
    public static double v(String id,String key){
        Option o=Objects.requireNonNull(OPTIONS.get(id+"."+key));
        double v=INSTANCE.values.getOrDefault(id+"."+key,o.initial);return Double.isFinite(v)?Math.clamp(v,o.min,o.max):o.initial;
    }
    public static boolean b(String id,String key){return v(id,key)>.5;}
    public static int mode(String id){return (int)v(id,"style");}
    public static void set(String id,Option o,double value){INSTANCE.values.put(id+"."+o.key,Math.clamp(value,o.min,o.max));}
    public static void set(String id,String key,double value){var m=ALL.stream().filter(a->a.id.equals(id)).findFirst().orElseThrow();set(id,m.options.stream().filter(a->a.key.equals(key)).findFirst().orElseThrow(),value);}
    public static int bind(String id){return INSTANCE.binds==null?-1:INSTANCE.binds.getOrDefault(id,-1);}
    public static void bind(String id,int key){if(INSTANCE.binds==null)INSTANCE.binds=new HashMap<>();if(key<0)INSTANCE.binds.remove(id);else INSTANCE.binds.put(id,key);save();}
    public static void restore(ModuleSettings other){if(other.enabled==null||other.values==null||other.friends==null)throw new IllegalArgumentException("Invalid module profile");INSTANCE.enabled=new HashMap<>(other.enabled);INSTANCE.values=new HashMap<>(other.values);INSTANCE.friends=new HashSet<>(other.friends);INSTANCE.binds=other.binds==null?new HashMap<>():new HashMap<>(other.binds);save();}
    public static void save(){try{Files.writeString(FMLPaths.CONFIGDIR.get().resolve("sobaka-modules.json"),new GsonBuilder().setPrettyPrinting().create().toJson(INSTANCE));}catch(Exception e){ExampleMod.LOGGER.error("Cannot save modules",e);}}
    private static ModuleSettings load(){try{var p=FMLPaths.CONFIGDIR.get().resolve("sobaka-modules.json");if(Files.exists(p)){var c=new GsonBuilder().create().fromJson(Files.readString(p),ModuleSettings.class);if(c!=null){if(c.enabled==null)c.enabled=new HashMap<>();if(c.values==null)c.values=new HashMap<>();if(c.friends==null)c.friends=new HashSet<>();return c;}}}catch(Exception e){ExampleMod.LOGGER.warn("Invalid modules config",e);}return new ModuleSettings();}
}
