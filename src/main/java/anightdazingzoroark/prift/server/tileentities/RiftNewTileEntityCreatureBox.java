package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RiftNewTileEntityCreatureBox extends TileEntity implements ITickable {
    private final FixedSizeList<CreatureNBT> creatureListNBT = new FixedSizeList<>(RiftCreatureBox.maxDeployableCreatures, new CreatureNBT());

    @Override
    public void update() {
        //if box has contents, make it so that its indestructible when there's creatures inside
        RiftCreatureBox creatureBox = (RiftCreatureBox)this.world.getBlockState(this.pos).getBlock();
        if (this.isUnbreakable()) creatureBox.setHardness(-1.0f);
        else creatureBox.setHardness(0f);
    }

    //this creates the creatures that wander around the box
    private void createCreaturesForWandering() {
        this.world.getChunk(this.pos);
        for (CreatureNBT tagCompound : this.creatureListNBT.getList()) {
            RiftCreature creature = tagCompound.getCreatureAsNBT(this.world);

            //check for validity and if creature already exists, then continue
            if (creature == null || this.creatureExistsInWorld(creature) || !creature.isEntityAlive()) continue;

            //generate a spawn point for creature
            BlockPos spawnPoint = RiftTileEntityCreatureBoxHelper.creatureCreatureSpawnPoint(this.pos, this.world, creature);
            if (spawnPoint != null) {
                creature.setPosition(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
                creature.setHomePos(this.pos.getX(), this.pos.getY(), this.pos.getZ());
                this.world.spawnEntity(creature);
            }
        }
    }

    private boolean creatureExistsInWorld(RiftCreature creature) {
        List<UUID> worldEntityListUUID = this.world.getLoadedEntityList().stream()
                .map(Entity::getUniqueID)
                .collect(Collectors.toList());
        return worldEntityListUUID.contains(creature.getUniqueID());
    }

    public boolean isUnbreakable() {
        return !this.creatureListNBT.isEmpty();
    }

    public FixedSizeList<CreatureNBT> getDeployedCreatures() {
        return this.creatureListNBT;
    }

    public void setCreatureInPos(int pos, CreatureNBT creatureNBT) {
        this.creatureListNBT.set(pos, creatureNBT);
        this.updateServerData();
    }

    public void setCreatureListNBT(FixedSizeList<CreatureNBT> value) {
        int maxSize = Math.min(RiftCreatureBox.maxDeployableCreatures, value.size());
        for (int i = 0; i < maxSize; i++) {
            CreatureNBT valueFromInput = value.get(i);
            this.creatureListNBT.set(i, valueFromInput);
        }
        this.updateServerData();
    }

    //saving and updating nbt starts here
    private void updateServerData() {
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
        this.readFromNBT(compound);
    }
    //saving and updating nbt ends here
}
