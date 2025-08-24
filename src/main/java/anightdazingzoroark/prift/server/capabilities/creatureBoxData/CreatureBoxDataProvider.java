package anightdazingzoroark.prift.server.capabilities.creatureBoxData;

import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CreatureBoxDataProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(ICreatureBoxData.class)
    public static final Capability<ICreatureBoxData> CREATURE_BOX_DATA_CAPABILITY = null;

    private ICreatureBoxData instance = CREATURE_BOX_DATA_CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CREATURE_BOX_DATA_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == CREATURE_BOX_DATA_CAPABILITY ? CREATURE_BOX_DATA_CAPABILITY.cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return writeNBT(this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        readNBT(this.instance, null, nbt);
    }

    public static NBTBase writeNBT(ICreatureBoxData capability, EnumFacing side) {
        return CREATURE_BOX_DATA_CAPABILITY.writeNBT(capability, side);
    }

    public static void readNBT(ICreatureBoxData capability, EnumFacing side, NBTBase nbt) {
        CREATURE_BOX_DATA_CAPABILITY.readNBT(capability, side, nbt);
    }
}
