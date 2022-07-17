package com.denfop.qe;


import com.denfop.api.qe.event.QETileLoadEvent;
import com.denfop.api.qe.event.QETileUnloadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class EventHandler {

    public EventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }


    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEnergyTileLoad(final QETileLoadEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        final QENetLocal local = QENetGlobal.getForWorld(event.getWorld());

        if (local != null) {
            local.addTile(event.tile);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEnergyTileUnload(final QETileUnloadEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        final QENetLocal local = QENetGlobal.getForWorld(event.getWorld());
        if (local != null) {
            local.removeTile(event.tile);
        }
    }

    @SubscribeEvent
    public void tick(final TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            QENetGlobal.onTickEnd(event.world);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(final WorldEvent.Unload event) {
        if (event.getWorld().isRemote) {
            return;
        }
        QENetGlobal.onWorldUnload(event.getWorld());

    }

}
