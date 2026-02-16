package anightdazingzoroark.prift.server.capabilities.playerParty;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerPartyProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(IPlayerParty.class)
    public static final Capability<IPlayerParty> PLAYER_PARTY_CAPABILITY = null;

    private IPlayerParty instance = PLAYER_PARTY_CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_PARTY_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_PARTY_CAPABILITY ? PLAYER_PARTY_CAPABILITY.cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return writeNBT(this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        readNBT(this.instance, null, nbt);
    }

    public static NBTBase writeNBT(IPlayerParty capability, EnumFacing side) {
        return PLAYER_PARTY_CAPABILITY.writeNBT(capability, side);
    }

    public static void readNBT(IPlayerParty capability, EnumFacing side, NBTBase nbt) {
        PLAYER_PARTY_CAPABILITY.readNBT(capability, side, nbt);
    }
}
