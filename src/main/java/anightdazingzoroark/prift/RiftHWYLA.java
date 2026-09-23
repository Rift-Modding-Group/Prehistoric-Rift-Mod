package anightdazingzoroark.prift;

import anightdazingzoroark.prift.server.config.RiftDebugFlags;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import java.util.List;

@WailaPlugin(RiftInitialize.MODID)
public class RiftHWYLA implements IWailaPlugin {
    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.registerBodyProvider(new IWailaEntityProvider() {
            @Nonnull
            @Override
            public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
                RiftCreature creature = (RiftCreature) entity;
                if (RiftDebugFlags.showStaminaInHWYLA) {
                    currenttip.add("Stamina: " + String.format("%.1f", creature.getStamina()) + "/" + creature.getMaxStamina());
                }

                //creature owner should go first
                if (creature.getOwner() != null) {
                    currenttip.addFirst(I18n.format("info.owner_name", creature.getOwner().getName()));
                }

                return currenttip;
            }
        }, RiftCreature.class);
    }
}