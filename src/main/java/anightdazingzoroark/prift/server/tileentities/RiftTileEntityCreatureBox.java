package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.creatureSpawning.RiftCreatureSpawnLists;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import anightdazingzoroark.prift.server.enums.TameStatusType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

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
        //if box has contents, make it so that its indestructible when there's creatures inside
        RiftCreatureBox creatureBox = (RiftCreatureBox)this.world.getBlockState(this.pos).getBlock();
        if (this.isUnbreakable()) creatureBox.setHardness(-1.0f);
        else creatureBox.setHardness(0f);

        //spawn creatures from box
        if (!this.world.isRemote) {
            this.createCreaturesForWandering();
        }
    }

    public boolean isUnbreakable() {
        return !this.creatureList.isEmpty();
    }

    private void createCreaturesForWandering() {
        for (RiftCreature creature : this.getCreatures()) {
            //check if creature exists in world
            //then generate a spawn point
            if (!this.creatureExistsInWorld(creature)) {
                for (int i = 0; i < 10; i++) {
                    int xSpawnPos = RiftUtil.randomInRange(this.getPos().getX() - this.wanderRange, this.getPos().getX() + this.wanderRange);
                    int ySpawnPos = RiftUtil.randomInRange(this.getPos().getY() - 8, this.getPos().getY() + 8);
                    int zSpawnPos = RiftUtil.randomInRange(this.getPos().getZ() - this.wanderRange, this.getPos().getZ() + this.wanderRange);
                    BlockPos pos = new BlockPos(xSpawnPos, ySpawnPos, zSpawnPos);
                    IBlockState downState = this.world.getBlockState(pos.down());

                    if (creature instanceof RiftWaterCreature) {
                        RiftWaterCreature waterCreature = (RiftWaterCreature) creature;
                        //spawn amphibious creatures
                        if (waterCreature.isAmphibious()) {
                            if ((this.canFitInArea(creature, pos) && downState.getMaterial() != Material.AIR) || this.entireAreaWater(creature, pos)) {
                                creature.setPosition(xSpawnPos, ySpawnPos, zSpawnPos);
                                creature.setTameStatus(TameStatusType.WANDER);
                                creature.setHomePos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
                                this.world.spawnEntity(creature);
                                break;
                            }
                        }
                        //spawn aquatic creatures
                        else {
                            if (this.entireAreaWater(creature, pos)) {
                                creature.setPosition(xSpawnPos, ySpawnPos, zSpawnPos);
                                creature.setTameStatus(TameStatusType.WANDER);
                                creature.setHomePos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
                                this.world.spawnEntity(creature);
                                break;
                            }
                        }
                    }
                    else {
                        //spawn regular land creatures
                        if (this.canFitInArea(creature, pos) && downState.getMaterial() != Material.AIR) {
                            creature.setPosition(xSpawnPos, ySpawnPos, zSpawnPos);
                            creature.setTameStatus(TameStatusType.WANDER);
                            creature.setHomePos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
                            this.world.spawnEntity(creature);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void updateCreatures(RiftCreature creature) {
        for (NBTTagCompound partyMemCompound : this.creatureList) {
            if (partyMemCompound.getUniqueId("UniqueID") != null && partyMemCompound.getUniqueId("UniqueID").equals(creature.getUniqueID())) {
                NBTTagCompound partyMemCompoundUpdt = new NBTTagCompound();
                partyMemCompoundUpdt.setUniqueId("UniqueID", creature.getUniqueID());
                partyMemCompoundUpdt.setString("CustomName", creature.getCustomNameTag());
                creature.writeEntityToNBT(partyMemCompoundUpdt);

                if (this.creatureList.contains(partyMemCompound)) this.replaceInCreatureList(this.creatureList.indexOf(partyMemCompound), partyMemCompoundUpdt);
            }
        }
    }

    private boolean canFitInArea(RiftCreature creature, BlockPos pos) {
        int xMin = (int)Math.floor(creature.width / 2);
        for (int x = -xMin; x <= xMin; x++) {
            for (int y = 0; y < (int)Math.ceil(creature.height); y++) {
                for (int z = -xMin; z <= xMin; z++) {
                    BlockPos newPos = pos.add(x, y, z);
                    IBlockState state = this.world.getBlockState(newPos);
                    if (state.getMaterial() != Material.AIR) return false;
                }
            }
        }
        return true;
    }

    private boolean entireAreaWater(RiftCreature creature, BlockPos pos) {
        int xMin = (int)Math.floor(creature.width / 2);
        for (int x = -xMin; x <= xMin; x++) {
            for (int y = 0; y < (int)Math.ceil(creature.height); y++) {
                for (int z = -xMin; z <= xMin; z++) {
                    IBlockState state = this.world.getBlockState(pos.add(x, y, z));
                    if (state.getMaterial() != Material.WATER) return false;
                }
            }
        }
        return true;
    }

    private boolean creatureExistsInWorld(RiftCreature creature) {
        return this.world.getLoadedEntityList().contains(creature);
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
