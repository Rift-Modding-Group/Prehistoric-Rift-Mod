package anightdazingzoroark.prift.compat.hwyla.provider;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityMillstone;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class RiftMillstoneProvider implements IWailaDataProvider {
    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity tileEntity = accessor.getTileEntity();
        if (tileEntity instanceof TileEntityMillstone) {
            TileEntityMillstone millstone = (TileEntityMillstone) tileEntity;
            tooltip.add(I18n.format("hwyla.power_consumed", (int)millstone.getPower()+".0 R"));
        }
        return tooltip;
    }
}
