package anightdazingzoroark.prift.compat.hwyla.provider;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMechanicalFilter;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class RiftMechanicalFilterProvider implements IWailaDataProvider {
    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity tileEntity = accessor.getTileEntity();
        if (tileEntity instanceof TileEntityMechanicalFilter) {
            TileEntityMechanicalFilter mechanicalFilter = (TileEntityMechanicalFilter) tileEntity;
            tooltip.add(I18n.format("hwyla.power_consumed", (int)mechanicalFilter.getPower()+".0 R"));
        }
        return tooltip;
    }
}
