package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import javax.annotation.Nullable;

public class RiftNewTileEntityCreatureBox extends TileEntity implements ITickable {
    private final FixedSizeList<CreatureNBT> creatureListNBT = new FixedSizeList<>(RiftCreatureBox.maxDeployableCreatures, new CreatureNBT());

    @Override
    public void update() {}

    public boolean isUnbreakable() {
        return !this.creatureListNBT.isEmpty();
    }

    public FixedSizeList<CreatureNBT> getDeployedCreatures() {
        return this.creatureListNBT;
    }

    public void addCreature(CreatureNBT creatureNBT) {
        this.creatureListNBT.add(creatureNBT);
    }

    public void setCreatureInPos(int pos, CreatureNBT creatureNBT) {
        this.creatureListNBT.set(pos, creatureNBT);
        this.updateData();
    }

    //saving and updating nbt starts here
    private void updateData() {
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("BoxDeployedCreatures")) {
            NBTTagList boxDeployedCreaturesList = compound.getTagList("BoxDeployedCreatures", 10);
            if (!boxDeployedCreaturesList.isEmpty()) {
                for (int i = 0; i < boxDeployedCreaturesList.tagCount(); i++) {
                    this.creatureListNBT.set(i, new CreatureNBT(boxDeployedCreaturesList.getCompoundTagAt(i)));
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList boxDeployedCreaturesList = new NBTTagList();
        for (CreatureNBT boxNBT : this.creatureListNBT.getList()) boxDeployedCreaturesList.appendTag(boxNBT.getCreatureNBT());
        compound.setTag("BoxDeployedCreatures", boxDeployedCreaturesList);

        return compound;
    }

    @Override
    @Nullable
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        this.writeToNBT(nbtTag);
        return new SPacketUpdateTileEntity(this.pos, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void handleUpdateTag(NBTTagCompound compound) {
        if (compound.hasKey("BoxDeployedCreatures")) {
            NBTTagList boxDeployedCreaturesList = compound.getTagList("BoxDeployedCreatures", 10);
            if (!boxDeployedCreaturesList.isEmpty()) {
                for (int i = 0; i < boxDeployedCreaturesList.tagCount(); i++) {
                    this.creatureListNBT.set(i, new CreatureNBT(boxDeployedCreaturesList.getCompoundTagAt(i)));
                }
            }
        }
    }
    //saving and updating nbt ends here
}
