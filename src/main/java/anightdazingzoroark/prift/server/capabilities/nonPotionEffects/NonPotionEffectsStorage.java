package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class NonPotionEffectsStorage implements Capability.IStorage<INonPotionEffects> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<INonPotionEffects> capability, INonPotionEffects instance, EnumFacing side) {
        if (instance == null) return null;
        NBTTagCompound compound = new NBTTagCompound();

        compound.setBoolean("Bleeding", instance.isBleeding());
        compound.setInteger("BleedStrength", instance.getBleedStrength());
        compound.setInteger("BleedTick", instance.getBleedTick());

        if (instance.getCaptor() != null) compound.setUniqueId("CaptorID", instance.getCaptor().getUniqueID());
        else compound.setUniqueId("CaptorID", RiftUtil.nilUUID);

        compound.setBoolean("BolaCaptured", instance.isBleeding());
        compound.setInteger("BolaCapturedTick", instance.getBolaCapturedTick());

        return compound;
    }

    @Override
    public void readNBT(Capability<INonPotionEffects> capability, INonPotionEffects instance, EnumFacing side, NBTBase nbt) {
        if (instance == null || nbt == null) return;

        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound)nbt;

            if (compound.hasKey("Bleeding") && compound.getBoolean("Bleeding")) {
                instance.setBleeding(compound.getInteger("BleedStrength"), compound.getInteger("BleedTick"));
            }

            if (compound.hasKey("CaptorID") && !compound.getUniqueId("CaptorID").equals(RiftUtil.nilUUID)) {
                instance.setCapturedByCreature((RiftCreature) RiftUtil.getEntityFromUUID(Minecraft.getMinecraft().world, compound.getUniqueId("CaptorID")));
            }

            if (compound.hasKey("BolaCaptured") && compound.getBoolean("BolaCaptured")) {
                instance.setBolaCaptured(compound.getInteger("BolaCapturedTick"));
            }
        }
    }
}
