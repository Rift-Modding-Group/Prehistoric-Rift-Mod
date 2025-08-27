package anightdazingzoroark.prift.compat.hwyla.provider;

import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import java.util.List;

public class RiftCreatureProvider implements IWailaEntityProvider {
    @Nonnull
    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        currenttip.clear();
        RiftCreature creature = (RiftCreature) entity;
        if (creature != null) {
            //show owner
            if (creature.isTamed()) currenttip.add(I18n.format("hwyla.owner", creature.getOwner().getName()));
            else currenttip.add(I18n.format("hwyla.wild"));

            //show health and energy
            if (config.getConfig("general.showhp")) {
                int currentHealth = (int)Math.ceil(creature.getHealth());
                int maxHealth = (int)creature.getMaxHealth();
                currenttip.add(I18n.format("hwyla.health", currentHealth, maxHealth));
                if (creature.isTamed()) {
                    currenttip.add(I18n.format("hwyla.energy", creature.getEnergy(), creature.getMaxEnergy()));
                    currenttip.add(creature.getDeploymentType().getDeploymentInfo((EntityPlayer) creature.getOwner()));
                }
            }
        }
        return currenttip;
    }
}
