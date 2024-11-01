package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

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

        compound.setBoolean("Captured", instance.isCaptured());

        compound.setBoolean("BolaCaptured", instance.isBleeding());
        compound.setInteger("BolaCapturedTick", instance.getBolaCapturedTick());

        compound.setBoolean("Riding", instance.isRiding());

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

            if (compound.hasKey("Captured")) {
                instance.setCaptured(compound.getBoolean("Captured"));
            }

            if (compound.hasKey("BolaCaptured") && compound.getBoolean("BolaCaptured")) {
                instance.setBolaCaptured(compound.getInteger("BolaCapturedTick"));
            }

            if (compound.hasKey("Riding")) {
                instance.setRiding(compound.getBoolean("Riding"));
            }
        }
    }
}
