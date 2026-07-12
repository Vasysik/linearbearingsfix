package me.zombii.linearfix.mixin;


import com.bearing.linearbearing.LinearBearing;
import com.bearing.linearbearing.LinearBearingClient;
import com.bearing.linearbearing.events.ModDataGenerators;
import com.bearing.linearbearing.registrate.ClientModHandler;
import com.bearing.linearbearing.registrate.ModBlocks;
import com.bearing.linearbearing.registrate.ModComponents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.bearing.linearbearing.LinearBearing.CREATIVE_MODE_TABS;

@Mixin(LinearBearing.class)
public abstract class LinearBearingMixin {

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/lang/Object;<init>()V", shift = At.Shift.AFTER), cancellable = true)
    private static void onInit(IEventBus modEventBus, CallbackInfo ci) {
        ci.cancel();

        ModBlocks.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(ModDataGenerators::gatherData);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(ClientModHandler::onClientSetup);
            modEventBus.addListener(ClientModHandler::onModelBake);
        }
        ModComponents.register(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LinearBearingClient.registerClient(modEventBus);
        }
    }

}
