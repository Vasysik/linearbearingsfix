package me.zombii.linearfix.mixin;

import net.neoforged.bus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marker mixin. The companion config plugin removes Linear Bearing's
 * client-only constructor instructions before this mixin is applied on a
 * dedicated server.
 */
@Mixin(targets = "com.bearing.linearbearing.LinearBearing", remap = false)
public abstract class LinearBearingMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void linearbearingsfix$afterInit(IEventBus modEventBus, CallbackInfo callbackInfo) {
        // The constructor body is patched by LinearBearingsFixMixinPlugin.
    }
}
