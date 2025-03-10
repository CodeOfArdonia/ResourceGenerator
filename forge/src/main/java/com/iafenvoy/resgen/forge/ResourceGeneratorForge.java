package com.iafenvoy.resgen.forge;

import net.minecraftforge.fml.common.Mod;

import com.iafenvoy.resgen.ResourceGenerator;

@Mod(ResourceGenerator.MOD_ID)
public final class ResourceGeneratorForge {
    public ResourceGeneratorForge() {
        ResourceGenerator.init();
    }
}
