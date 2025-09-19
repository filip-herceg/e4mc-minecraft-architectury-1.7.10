package link.e4mc.mixin;

import link.e4mc.E4mcMod;
import net.minecraft.server.management.ServerConfigurationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerConfigurationManager.class)
public abstract class PlayerListMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    void injectListLoads(CallbackInfo ci) {
        E4mcMod.getLogger().info("ServerConfigurationManager initialized");
    }
}
