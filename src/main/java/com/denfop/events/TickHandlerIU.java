package com.denfop.events;

import com.denfop.IUCore;
import ic2.core.IWorldTickCallback;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Map;
import java.util.WeakHashMap;

public class TickHandlerIU {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            IUCore.proxy.profilerStartSection("Keyboard");
            IUCore.keyboard.sendKeyUpdate();
            IUCore.proxy.profilerEndStartSection("AudioManager");
            IUCore.audioManager.onTick();
            IUCore.proxy.profilerEndStartSection("updates");


            IUCore.proxy.profilerEndSection();
        }

    }

}
