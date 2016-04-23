package com.thomas15v.testplugin;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id="Testplugin", name = "TestPlugin", version = "1.0.0-Sponge")
public class TestPlugin extends net.md_5.bungee.api.plugin.Plugin {

    @Listener
    public void onStart(final GameStartedServerEvent event) {
        System.out.println("Hello World!");
    }

    @Override
    public void onEnable() {
        System.out.println("HEYYY");
    }
}
