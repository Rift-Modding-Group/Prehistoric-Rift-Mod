package anightdazingzoroark.prift.compat.hwyla.provider;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaEntityAccessor;
import mcp.mobius.waila.api.IWailaEntityProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import java.util.List;

public class RiftCreaturePartProvider implements IWailaEntityProvider {
    @Nonnull
    @Override
    public List<String> getWailaHead(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        currenttip.clear();
        RiftCreaturePart part = (RiftCreaturePart) entity;
        if (part != null) {
            String creatureName = TextFormatting.WHITE + part.getParent().getName(false);
            String level = TextFormatting.GRAY + I18n.format("tametrait.level", part.getParent().getLevel());
            currenttip.add(creatureName + " (" + level + ")");
        }
        return currenttip;
    }

    @Nonnull
    @Override
    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        RiftCreaturePart part = (RiftCreaturePart) entity;
        if (part != null) {
            //show owner
            if (part.getParent().isTamed()) currenttip.add(I18n.format("hwyla.owner", part.getParent().getOwner().getName()));
            else currenttip.add(I18n.format("hwyla.wild"));

            //show health and energy
            if (config.getConfig("general.showhp")) {
                int currentHealth = (int)Math.ceil(part.getParent().getHealth());
                int maxHealth = (int)part.getParent().getMaxHealth();
                currenttip.add(I18n.format("hwyla.health", currentHealth, maxHealth));
                if (part.getParent().isTamed()) currenttip.add(I18n.format("hwyla.energy", part.getParent().getEnergy(), part.getParent().getMaxEnergy()));
            }
        }
        return currenttip;
    }

    @Nonnull
    @Override
    public List<String> getWailaTail(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
        currenttip.clear();
        currenttip.add(TextFormatting.BLUE + "" + TextFormatting.ITALIC + RiftInitialize.MODNAME);
        return currenttip;
    }
}
