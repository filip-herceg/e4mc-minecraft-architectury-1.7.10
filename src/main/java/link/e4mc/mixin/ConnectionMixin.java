package link.e4mc.mixin;

import io.netty.channel.Channel;
import io.netty.channel.local.LocalAddress;
import net.minecraft.network.NetworkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NetworkManager.class)
public class ConnectionMixin {
    @Shadow
    private Channel channel;

    @Inject(method = "isLocalChannel", at = @At("RETURN"), cancellable = true)
    private void isLocalChannelInject(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            if (this.channel != null && this.channel.localAddress() instanceof LocalAddress) {
                LocalAddress localAddr = (LocalAddress) this.channel.localAddress();
                if ("e4mc-relay".equals(localAddr.id())) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}