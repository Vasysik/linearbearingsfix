package me.zombii.linearfix;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(LinearBearingsFix.MODID)
public class LinearBearingsFix {
    public static final String MODID = "linearbearingsfix";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LinearBearingsFix(IEventBus modEventBus, ModContainer modContainer) {
    }

}
