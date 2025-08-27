package anightdazingzoroark.prift.compat.hwyla.provider;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitbox;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.List;

public class RiftCreaturePartProvider implements IWailaEntityProvider {
    @Nonnull
    @Override
    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        if (!this.isCreaturePart(entity)) return currenttip;

        currenttip.clear();
        EntityHitbox part = (EntityHitbox) entity;
        RiftCreature creature = (RiftCreature) part.getParentAsEntityLiving();
        String creatureName = TextFormatting.WHITE + creature.getName(false);
        String level = TextFormatting.GRAY + I18n.format("tametrait.level", creature.getLevel());
        currenttip.add(creatureName + " (" + level + ")");
        return currenttip;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        if (!this.isCreaturePart(entity)) return currenttip;

        EntityHitbox part = (EntityHitbox) entity;
        RiftCreature creature = (RiftCreature) part.getParentAsEntityLiving();

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

                //for being deployed from the party
                if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                    currenttip.add(I18n.format("deployment_info.party", creature.getOwner().getName()));
                }
                else if (creature.getDeploymentType() == PlayerTamedCreatures.DeploymentType.BASE) {
                    currenttip.add(I18n.format(
                            "deployment_info.box",
                            creature.getOwner().getName(),
                            creature.getHomePos().getX(),
                            creature.getHomePos().getY(),
                            creature.getHomePos().getZ()
                    ));
                }
            }
        }
        return currenttip;
    }

    @Nonnull
    @Override
    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        if (!this.isCreaturePart(entity)) return currenttip;

        currenttip.clear();
        currenttip.add(TextFormatting.BLUE + "" + TextFormatting.ITALIC + RiftInitialize.MODNAME);
        return currenttip;
    }

    private boolean isCreaturePart(Entity entity) {
        return entity instanceof EntityHitbox && ((EntityHitbox) entity).getParentAsEntityLiving() instanceof RiftCreature;
    }
}
