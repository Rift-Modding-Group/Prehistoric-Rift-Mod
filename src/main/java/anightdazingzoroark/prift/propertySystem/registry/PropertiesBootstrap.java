package anightdazingzoroark.prift.propertySystem.registry;

import anightdazingzoroark.prift.RiftInitialize;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class PropertiesBootstrap {
    public static final ResourceLocation KEY = new ResourceLocation(RiftInitialize.MODID, "properties");

    @CapabilityInject(PropertiesRoot.class)
    public static final Capability<PropertiesRoot> CAP = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(
                PropertiesRoot.class,
                new Capability.IStorage<PropertiesRoot>() {
                    @Override
                    public NBTBase writeNBT(Capability<PropertiesRoot> capability, PropertiesRoot instance, EnumFacing side) {
                        return instance.writeToNBT();
                    }

                    @Override
                    public void readNBT(Capability<PropertiesRoot> capability, PropertiesRoot instance, EnumFacing side, NBTBase nbt) {
                        // entity is needed to resolve/init sets, so actual read happens in Provider.deserializeNBT
                        // This method can remain empty safely.
                    }
                },
                PropertiesRoot::new
        );

        MinecraftForge.EVENT_BUS.register(new Attacher());
    }

    private static final class Attacher {
        @SubscribeEvent
        public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> entity) {
            entity.addCapability(KEY, new Provider(entity.getObject()));
        }
    }

    private static final class Provider implements net.minecraftforge.common.capabilities.ICapabilitySerializable<NBTTagCompound> {
        private final PropertiesRoot root = new PropertiesRoot();
        private final Entity owner;

        private Provider(Entity owner) {
            this.owner = owner;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CAP;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            return capability == CAP ? (T) root : null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return this.root.writeToNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            this.root.readFromNBT(nbt, this.owner);
        }
    }
}
