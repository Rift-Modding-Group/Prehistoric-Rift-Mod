package anightdazingzoroark.prift;

import anightdazingzoroark.prift.server.config.RiftDebugFlags;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import mcp.mobius.waila.api.*;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import java.util.List;

@WailaPlugin(RiftInitialize.MODID)
public class RiftHWYLA implements IWailaPlugin {
    @Override
    public void register(IWailaRegistrar registrar) {
        if (!RiftDebugFlags.showStaminaInHWYLA) return;

        registrar.registerBodyProvider(new IWailaEntityProvider() {
            @Nonnull
            @Override
            public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
                RiftCreature creature = (RiftCreature) entity;
                currenttip.add("Stamina: "+String.format("%.1f", creature.getStamina())+"/"+creature.getMaxStamina());
                return currenttip;
            }
        }, RiftCreature.class);
    }
}