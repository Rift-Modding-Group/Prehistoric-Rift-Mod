package anightdazingzoroark.prift.server.fluids;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

public class RiftFluid extends Fluid {
    public RiftFluid(String fluidName, ResourceLocation still, ResourceLocation flowing) {
        super(fluidName, still, flowing);
        this.setUnlocalizedName(fluidName);
    }
}
