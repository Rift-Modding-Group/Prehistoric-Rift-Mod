package anightdazingzoroark.prift.server.dataSerializers;

import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.DataSerializerEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.List;

public class PrimerEventHandler {
    private InternalRegistryPrimer registry;

    public PrimerEventHandler(InternalRegistryPrimer registry) {
        this.registry = registry;
    }

    @SubscribeEvent
    public void registerDataSerializers(RegistryEvent.Register<DataSerializerEntry> event) {
        registry.wipe(event.getClass());
        RiftDataSerializers.registerSerializers();
        fillRegistry(event.getRegistry().getRegistrySuperType(), event.getRegistry());
    }

    private <T extends IForgeRegistryEntry<T>> void fillRegistry(Class<T> registrySuperType, IForgeRegistry<T> forgeRegistry) {
        List<?> entries = registry.getEntries(registrySuperType);
        if(entries != null) {
            entries.forEach((e) -> forgeRegistry.register((T) e));
        }
    }
}
