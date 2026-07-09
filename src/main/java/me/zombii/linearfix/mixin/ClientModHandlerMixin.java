package me.zombii.linearfix.mixin;


import com.bearing.linearbearing.LinearBearing;
import com.bearing.linearbearing.LinearBearingClient;
import com.bearing.linearbearing.registrate.ClientModHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientModHandler.class)
public class ClientModHandlerMixin {

    @Inject(method = "onClientSetup", cancellable = true, at = @At("HEAD"))
    private static void onClientSetup(FMLClientSetupEvent event, CallbackInfo ci) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            ci.cancel();
        }
    }

    @Inject(method = "onModelBake", cancellable = true, at = @At("HEAD"))
    private static void onModelBake(ModelEvent.ModifyBakingResult event, CallbackInfo ci) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            ci.cancel();
        }
    }

}
