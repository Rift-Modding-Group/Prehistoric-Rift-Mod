package anightdazingzoroark.prift.compat.hwyla;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.hwyla.provider.RiftBlowPoweredTurbineProvider;
import anightdazingzoroark.prift.compat.hwyla.provider.RiftCrankProvider;
import anightdazingzoroark.prift.compat.hwyla.provider.RiftCreaturePartProvider;
import anightdazingzoroark.prift.compat.hwyla.provider.RiftSemiManualMachineProvider;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.*;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreaturePart;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin(RiftInitialize.MODID)
public class RiftHWYLA implements IWailaPlugin {
    @Override
    public void register(IWailaRegistrar registrar) {
        RiftCreaturePartProvider creaturePartProvider = new RiftCreaturePartProvider();
        registrar.registerHeadProvider(creaturePartProvider, RiftCreaturePart.class);
        registrar.registerBodyProvider(creaturePartProvider, RiftCreaturePart.class);
        registrar.registerTailProvider(creaturePartProvider, RiftCreaturePart.class);

        if (GeneralConfig.canUseMM()) {
            RiftBlowPoweredTurbineProvider turbineProvider = new RiftBlowPoweredTurbineProvider();
            registrar.registerBodyProvider(turbineProvider, TileEntityBlowPoweredTurbine.class);
            registrar.registerBodyProvider(turbineProvider, TileEntityBlowPoweredTurbinePart.class);

            RiftCrankProvider leadPoweredCrankProvider = new RiftCrankProvider();
            registrar.registerBodyProvider(leadPoweredCrankProvider, TileEntityHandCrank.class);
            registrar.registerBodyProvider(leadPoweredCrankProvider, TileEntityLeadPoweredCrank.class);

            RiftSemiManualMachineProvider smPresserProvider = new RiftSemiManualMachineProvider();
            registrar.registerBodyProvider(smPresserProvider, TileEntitySemiManualExtractor.class);
            registrar.registerBodyProvider(smPresserProvider, TileEntitySemiManualExtractorTop.class);
            registrar.registerBodyProvider(smPresserProvider, TileEntitySemiManualPresser.class);
            registrar.registerBodyProvider(smPresserProvider, TileEntitySemiManualPresserTop.class);
            registrar.registerBodyProvider(smPresserProvider, TileEntitySemiManualExtruder.class);
            registrar.registerBodyProvider(smPresserProvider, TileEntitySemiManualExtruderTop.class);
            registrar.registerBodyProvider(smPresserProvider, TileEntitySemiManualHammerer.class);
            registrar.registerBodyProvider(smPresserProvider, TileEntitySemiManualHammererTop.class);
        }
    }
}
