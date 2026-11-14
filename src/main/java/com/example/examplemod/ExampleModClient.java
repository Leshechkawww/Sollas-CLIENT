package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import java.io.IOException;

@Mod(value=ExampleMod.MODID, dist=Dist.CLIENT)
public final class ExampleModClient {
    public static final KeyMapping OPEN = new KeyMapping("key.myclient.open", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.myclient");
    public ExampleModClient(IEventBus bus, ModContainer container) {
        ModuleRuntime.register(bus);
        bus.addListener(this::keys);
        bus.addListener(this::shaders);
        bus.addListener(this::layers);
        NeoForge.EVENT_BUS.addListener(this::tick);
        NeoForge.EVENT_BUS.addListener(this::screen);
        NeoForge.EVENT_BUS.addListener(this::opening);
        
        container.registerExtensionPoint(IConfigScreenFactory.class, (c, parent) -> new ClickGuiScreen(parent));
    }
    private void keys(RegisterKeyMappingsEvent e) { e.register(OPEN); }
    private void shaders(RegisterShadersEvent e) {
        GlassRenderer.blurShader=null; GlassRenderer.panelShader=null;
        try {
        e.registerShader(new ShaderInstance(e.getResourceProvider(), id("blur"), DefaultVertexFormat.POSITION), s -> GlassRenderer.blurShader=s);
        e.registerShader(new ShaderInstance(e.getResourceProvider(), id("panel"), DefaultVertexFormat.POSITION), s -> GlassRenderer.panelShader=s);
        } catch(IOException failure) { ExampleMod.LOGGER.error("Glass shader unavailable; using solid panels",failure); }
    }
    private void layers(RegisterGuiLayersEvent e) {
        e.registerAboveAll(id("hud"), (g, delta) -> {
            var mc=Minecraft.getInstance();
            if (mc.player != null && ClientSettings.INSTANCE.hud && !mc.options.hideGui && !(mc.screen instanceof ClickGuiScreen) && !(mc.screen instanceof HudEditorScreen) && !(mc.screen instanceof ModulesScreen)) {
                GlassRenderer.begin(g); ClientHud.render(g, false);
            }
        });
    }
    private void tick(ClientTickEvent.Post e) {
        ClientHud.tick();
        FakePlayerManager.tick();
        var mc=Minecraft.getInstance();
        while (OPEN.consumeClick()) {
            if (mc.screen == null) mc.setScreen(new ClickGuiScreen(null));
        }
    }
    private void screen(ScreenEvent.Init.Post e) {
        if (e.getScreen().getClass() == TitleScreen.class && e.getScreen() instanceof TitleScreen s) {
            e.addListener(Button.builder(Component.literal("Sollas CLIENT"), b -> Minecraft.getInstance().setScreen(new ClickGuiScreen(s)))
                .bounds(s.width-148, 8, 140, 20).build());
        }
    }
    private void opening(ScreenEvent.Opening e) {
        if(e.getNewScreen()!=null && e.getNewScreen().getClass()==TitleScreen.class && !Minecraft.getInstance().isDemo()) e.setNewScreen(new LiquidHomeScreen());
    }
    public static ResourceLocation id(String name) { return ResourceLocation.fromNamespaceAndPath(ExampleMod.MODID,name); }
}
