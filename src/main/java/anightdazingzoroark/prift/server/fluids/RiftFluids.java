package anightdazingzoroark.prift.server.fluids;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class RiftFluids {
    public static Fluid PYROBERRY_JUICE;
    public static Fluid CRYOBERRY_JUICE;

    public static void registerFluids() {
        PYROBERRY_JUICE = registerFluid(new RiftFluid("pyroberry_juice", new ResourceLocation(RiftInitialize.MODID, "blocks/pyroberry_juice_still"), new ResourceLocation(RiftInitialize.MODID, "blocks/pyroberry_juice_flow")));
        CRYOBERRY_JUICE = registerFluid(new RiftFluid("cryoberry_juice", new ResourceLocation(RiftInitialize.MODID, "blocks/cryoberry_juice_still"), new ResourceLocation(RiftInitialize.MODID, "blocks/cryoberry_juice_flow")));
    }

    public static Fluid registerFluid(Fluid fluid) {
        FluidRegistry.registerFluid(fluid);
        FluidRegistry.addBucketForFluid(fluid);
        return fluid;
    }
}
