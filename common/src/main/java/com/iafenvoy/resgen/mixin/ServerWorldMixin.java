package com.iafenvoy.resgen.mixin;

import com.iafenvoy.resgen.data.WorldGeneratorState;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Inject(method = "tick", at = @At("RETURN"))
    private void onTickWorld(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        WorldGeneratorState.getState((ServerWorld) (Object) this).tick();
    }
}
