package anightdazingzoroark.prift.compat.hwyla.provider;

import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbine;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.TileEntityBlowPoweredTurbinePart;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class RiftBlowPoweredTurbineProvider implements IWailaDataProvider {
    @Nonnull
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        TileEntity tileEntity = accessor.getTileEntity();
        if (tileEntity instanceof TileEntityBlowPoweredTurbine) {
            TileEntityBlowPoweredTurbine turbine = (TileEntityBlowPoweredTurbine) tileEntity;
            tooltip.add(I18n.format("hwyla.power_created", (int)turbine.getPower()+".0 R"));
        }
        else if (tileEntity instanceof TileEntityBlowPoweredTurbinePart) {
            TileEntityBlowPoweredTurbinePart turbinePart = (TileEntityBlowPoweredTurbinePart)tileEntity;
            TileEntityBlowPoweredTurbine turbine = turbinePart.getTurbine();
            int power = (int)turbine.getPower();
            tooltip.add(I18n.format("hwyla.power_created", power+".0 R"));
        }
        return tooltip;
    }
}
