package me.zombii.linearfix;

import com.bearing.linearbearing.LinearBearing;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(LinearBearingsFix.MODID)
public class LinearBearingsFix {
    LinearBearing
    public static final String MODID = "linearbearingsfix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LinearBearingsFix(IEventBus modEventBus, ModContainer modContainer) {
    }

}
