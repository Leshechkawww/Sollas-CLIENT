package com.example.examplemod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.neoforged.fml.loading.FMLPaths;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ClientSettings {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FMLPaths.CONFIGDIR.get().resolve("myclient-ui.json");
    public static ClientSettings INSTANCE = load();
    public boolean hud = true, coordinates = true, outline = true;
    public boolean showFps=true, showBps=true, showNick=true, showTime=true, animations=true;
    public float hudScale=1f, hudHue=.70f;
    public int hudColor(){return 0xFF000000 | java.awt.Color.HSBtoRGB(Math.clamp(hudHue,0f,1f),.48f,1f);}
    public int glass = 2; // 0 solid, 1 frosted, 2 liquid
    public float blur = 2.5f, refraction = 8f;
    public int hudX = 16, hudY = 16;
    public java.util.Map<String, float[]> hudPositions = new java.util.HashMap<>();
    public int accent = 0;
    public int color() { return new int[]{0xFF9C8CFF, 0xFF65DBCC, 0xFFFFB780}[Math.floorMod(accent, 3)]; }
    private static ClientSettings load() {
        try {
            if (Files.exists(FILE)) {
                ClientSettings s = JSON.fromJson(Files.readString(FILE), ClientSettings.class);
                if (s != null) {
                    s.glass = Math.clamp(s.glass, 0, 2);
                    s.hudScale = Float.isFinite(s.hudScale) ? Math.clamp(s.hudScale, .7f, 1.3f) : 1f;
                    s.blur = Float.isFinite(s.blur) ? Math.clamp(s.blur, 0.5f, 5f) : 2.5f;
                    s.refraction = Float.isFinite(s.refraction) ? Math.clamp(s.refraction, 0f, 16f) : 8f;
                    return s;
                }
            }
        } catch (Exception e) { ExampleMod.LOGGER.warn("Cannot read UI settings; using defaults", e); }
        return new ClientSettings();
    }
    public void save() {
        try { Files.createDirectories(FILE.getParent()); Files.writeString(FILE, JSON.toJson(this)); }
        catch (Exception e) { ExampleMod.LOGGER.error("Cannot save UI settings", e); }
    }
}
