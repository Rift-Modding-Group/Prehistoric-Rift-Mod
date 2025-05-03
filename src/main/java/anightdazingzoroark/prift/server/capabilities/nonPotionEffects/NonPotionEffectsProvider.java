package anightdazingzoroark.prift.server.capabilities.nonPotionEffects;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NonPotionEffectsProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(INonPotionEffects.class)
    public static final Capability<INonPotionEffects> NON_POTION_EFFECTS_CAPABILITY = null;

    private INonPotionEffects instance = NON_POTION_EFFECTS_CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == NON_POTION_EFFECTS_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == NON_POTION_EFFECTS_CAPABILITY ? NON_POTION_EFFECTS_CAPABILITY.cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return writeNBT(this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        readNBT(this.instance, null, nbt);
    }

    public static NBTBase writeNBT(INonPotionEffects capability, EnumFacing side) {
        return NON_POTION_EFFECTS_CAPABILITY.writeNBT(capability, side);
    }

    public static void readNBT(INonPotionEffects capability, EnumFacing side, NBTBase nbt) {
        NON_POTION_EFFECTS_CAPABILITY.readNBT(capability, side, nbt);
    }
}
