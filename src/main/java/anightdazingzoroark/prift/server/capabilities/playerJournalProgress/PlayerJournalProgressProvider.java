package anightdazingzoroark.prift.server.capabilities.playerJournalProgress;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerJournalProgressProvider implements ICapabilitySerializable<NBTBase> {
    @CapabilityInject(IPlayerJournalProgress.class)
    public static final Capability<IPlayerJournalProgress> PLAYER_JOURNAL_PROGRESS_CAPABILITY = null;

    private IPlayerJournalProgress instance = PLAYER_JOURNAL_PROGRESS_CAPABILITY.getDefaultInstance();

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_JOURNAL_PROGRESS_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == PLAYER_JOURNAL_PROGRESS_CAPABILITY ? PLAYER_JOURNAL_PROGRESS_CAPABILITY.cast(this.instance) : null;
    }

    @Override
    public NBTBase serializeNBT() {
        return writeNBT(this.instance, null);
    }

    @Override
    public void deserializeNBT(NBTBase nbt) {
        readNBT(this.instance, null, nbt);
    }

    public static NBTBase writeNBT(IPlayerJournalProgress capability, EnumFacing side) {
        return PLAYER_JOURNAL_PROGRESS_CAPABILITY.writeNBT(capability, side);
    }

    public static void readNBT(IPlayerJournalProgress capability, EnumFacing side, NBTBase nbt) {
        PLAYER_JOURNAL_PROGRESS_CAPABILITY.readNBT(capability, side, nbt);
    }
}
