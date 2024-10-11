package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiftTileEntityCreatureBox extends TileEntity implements ITickable {
    private List<NBTTagCompound> creatureList = new ArrayList<>();
    private int maxWanderingCreatures = 10;
    private int wanderRange = 128;

    @Override
    public void update() {
        RiftCreatureBox creatureBox = (RiftCreatureBox)this.world.getBlockState(this.pos).getBlock();
        if (this.isUnbreakable()) creatureBox.setHardness(-1.0f);
        else creatureBox.setHardness(0f);
    }

    public boolean isUnbreakable() {
        return !this.creatureList.isEmpty();
    }

    public void createCreaturesForWandering() {
        if (!this.world.isRemote) {
            for (RiftCreature creature : this.getCreatures()) {
                //check if creature exists in world
                //then generate a spawn point
            }
        }
    }

    public void addToCreatureList(NBTTagCompound tagCompound) {
        this.creatureList.add(tagCompound);
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public void replaceInCreatureList(int pos, NBTTagCompound tagCompound) {
        this.creatureList.set(pos, tagCompound);
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public void removeFromCreatureList(int pos) {
        this.creatureList.remove(pos);
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public List<NBTTagCompound> getCreatureList() {
        return this.creatureList;
    }

    public void setCreatureList(List<NBTTagCompound> value) {
        this.creatureList = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public List<RiftCreature> getCreatures() {
        List<RiftCreature> creatures = new ArrayList<>();
        for (NBTTagCompound compound : this.creatureList) {
            RiftCreatureType creatureType = RiftCreatureType.values()[compound.getByte("CreatureType")];
            UUID uniqueID = compound.getUniqueId("UniqueID");
            String customName = compound.getString("CustomName");
            if (creatureType != null) {
                RiftCreature creature = creatureType.invokeClass(world);

                //attributes and creature health dont carry over on client side, this should be a workaround
                if (world.isRemote) {
                    creature.setHealth(compound.getFloat("Health"));
                    SharedMonsterAttributes.setAttributeModifiers(creature.getAttributeMap(), compound.getTagList("Attributes", 10));
                }

                creature.readEntityFromNBT(compound);
                creature.setUniqueId(uniqueID);
                creature.setCustomNameTag(customName);
                creatures.add(creature);
            }
        }
        return creatures;
    }

    public int getMaxWanderingCreatures() {
        return this.maxWanderingCreatures;
    }

    public void setMaxWanderingCreatures(int value) {
        this.maxWanderingCreatures = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public int getWanderRange() {
        return this.wanderRange;
    }

    public void setWanderRange(int value) {
        this.wanderRange = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.maxWanderingCreatures = compound.getInteger("MaxWanderingCreatures");
        this.wanderRange = compound.getInteger("WanderRange");

        if (compound.hasKey("BoxDeployedCreatures")) {
            NBTTagList boxDeployedCreaturesList = compound.getTagList("BoxDeployedCreatures", 10);
            if (!boxDeployedCreaturesList.isEmpty()) {
                List<NBTTagCompound> finalPartyCreatures = new ArrayList<>();
                for (int i = 0; i < boxDeployedCreaturesList.tagCount(); i++) {
                    finalPartyCreatures.add(boxDeployedCreaturesList.getCompoundTagAt(i));
                }
                this.creatureList = finalPartyCreatures;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("MaxWanderingCreatures", this.maxWanderingCreatures);
        compound.setInteger("WanderRange", this.wanderRange);

        NBTTagList boxDeployedCreaturesList = new NBTTagList();
        if (!this.creatureList.isEmpty()) {
            for (NBTTagCompound boxNBT : this.creatureList) boxDeployedCreaturesList.appendTag(boxNBT);
            compound.setTag("BoxDeployedCreatures", boxDeployedCreaturesList);
        }
        else compound.setTag("BoxDeployedCreatures", boxDeployedCreaturesList);

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
    public void handleUpdateTag(NBTTagCompound tag) {
        this.maxWanderingCreatures = tag.getInteger("MaxWanderingCreatures");
        this.wanderRange = tag.getInteger("WanderRange");

        if (tag.hasKey("BoxDeployedCreatures")) {
            NBTTagList boxDeployedCreaturesList = tag.getTagList("BoxDeployedCreatures", 10);
            if (!boxDeployedCreaturesList.isEmpty()) {
                List<NBTTagCompound> finalPartyCreatures = new ArrayList<>();
                for (int i = 0; i < boxDeployedCreaturesList.tagCount(); i++) {
                    finalPartyCreatures.add(boxDeployedCreaturesList.getCompoundTagAt(i));
                }
                this.creatureList = finalPartyCreatures;
            }
        }
    }
}
