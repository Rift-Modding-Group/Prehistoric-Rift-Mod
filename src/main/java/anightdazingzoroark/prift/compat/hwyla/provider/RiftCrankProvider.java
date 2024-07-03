package anightdazingzoroark.prift.compat.hwyla.provider;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityHandCrank;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityLeadPoweredCrank;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class RiftCrankProvider implements IWailaDataProvider {
    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity tileEntity = accessor.getTileEntity();
        if (tileEntity instanceof TileEntityHandCrank) {
            TileEntityHandCrank handCrank = (TileEntityHandCrank) tileEntity;
            tooltip.add(I18n.format("hwyla.power_created", (int)handCrank.getPower()+".0 R"));
        }
        if (tileEntity instanceof TileEntityLeadPoweredCrank) {
            TileEntityLeadPoweredCrank leadPoweredCrank = (TileEntityLeadPoweredCrank) tileEntity;
            tooltip.add(I18n.format("hwyla.power_created", leadPoweredCrank.mechPower.getPower(null)+" R"));
        }
        return tooltip;
    }
}
