package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerTamedCreaturesProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(IPlayerTamedCreatures.class)
    public static final Capability<IPlayerTamedCreatures> PLAYER_TAMED_CREATURES_CAPABILITY = null;

    private IPlayerTamedCreatures instance = PLAYER_TAMED_CREATURES_CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_TAMED_CREATURES_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_TAMED_CREATURES_CAPABILITY ? PLAYER_TAMED_CREATURES_CAPABILITY.cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return writeNBT(this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        readNBT(this.instance, null, nbt);
    }

    public static NBTBase writeNBT(IPlayerTamedCreatures capability, EnumFacing side) {
        return PLAYER_TAMED_CREATURES_CAPABILITY.writeNBT(capability, side);
    }

    public static void readNBT(IPlayerTamedCreatures capability, EnumFacing side, NBTBase nbt) {
        PLAYER_TAMED_CREATURES_CAPABILITY.readNBT(capability, side, nbt);
    }
}
