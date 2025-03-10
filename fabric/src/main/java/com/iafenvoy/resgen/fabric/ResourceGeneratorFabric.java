package com.iafenvoy.resgen.fabric;

import com.iafenvoy.resgen.ResourceGenerator;
import net.fabricmc.api.ModInitializer;

public final class ResourceGeneratorFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ResourceGenerator.init();
    }
}
