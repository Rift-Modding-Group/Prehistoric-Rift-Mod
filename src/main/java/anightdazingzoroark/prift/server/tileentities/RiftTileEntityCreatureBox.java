package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesHelper;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftWaterCreature;
import com.google.common.base.Predicate;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RiftTileEntityCreatureBox extends TileEntity implements ITickable {
    private List<NBTTagCompound> creatureListNBT = new ArrayList<>();
    private int creatureAmntLevel = 0;
    private int wanderRangeLevel = 0;

    @Override
    public void update() {
        //if box has contents, make it so that its indestructible when there's creatures inside
        RiftCreatureBox creatureBox = (RiftCreatureBox)this.world.getBlockState(this.pos).getBlock();
        if (this.isUnbreakable()) creatureBox.setHardness(-1.0f);
        else creatureBox.setHardness(0f);

        //spawn creatures from box
        if (!this.world.isRemote) this.createCreaturesForWandering();

        //remove creatures that are not recently spawned from the wander range of the creature box
        AxisAlignedBB removeAABB = new AxisAlignedBB(this.getPos().getX() - this.getWanderRange(), this.getPos().getY() - this.getWanderRange(), this.getPos().getZ() - this.getWanderRange(), this.getPos().getX() + this.getWanderRange(), this.getPos().getY() + this.getWanderRange(), this.getPos().getZ() + this.getWanderRange());
        for (RiftCreature creature : this.world.getEntitiesWithinAABB(RiftCreature.class, removeAABB, new Predicate<RiftCreature>() {
            @Override
            public boolean apply(@Nullable RiftCreature riftCreature) {
                return riftCreature != null && riftCreature.ticksExisted <= 100 && !riftCreature.isTamed();
            }
        })) {
            RiftUtil.removeCreature(creature);
        }
    }

    public boolean isUnbreakable() {
        return !this.creatureListNBT.isEmpty();
    }

    //this creates the creatures that wander around the box
    private void createCreaturesForWandering() {
        for (NBTTagCompound tagCompound : this.creatureListNBT) {
            RiftCreature creature = PlayerTamedCreaturesHelper.createCreatureFromNBT(this.world, tagCompound);

            //check for validity and if creature already exists, then continue
            if (creature == null || this.creatureExistsInWorld(creature) || !creature.isEntityAlive()) continue;

            //generate a spawn point for creature
            for (int i = 0; i < 10; i++) {
                int xSpawnPos = RiftUtil.randomInRange(this.getPos().getX() - 16, this.getPos().getX() + 16);
                int ySpawnPos = RiftUtil.randomInRange(this.getPos().getY() - 8, this.getPos().getY() + 8);
                int zSpawnPos = RiftUtil.randomInRange(this.getPos().getZ() - 16, this.getPos().getZ() + 16);
                BlockPos pos = new BlockPos(xSpawnPos, ySpawnPos, zSpawnPos);
                IBlockState downState = this.world.getBlockState(pos.down());

                if (creature instanceof RiftWaterCreature) {
                    RiftWaterCreature waterCreature = (RiftWaterCreature) creature;
                    //spawn amphibious creatures
                    if (waterCreature.isAmphibious()) {
                        if ((this.canFitInArea(creature, pos) && downState.getMaterial() != Material.AIR) || this.entireAreaWater(creature, pos)) {
                            creature.setPosition(xSpawnPos, ySpawnPos, zSpawnPos);
                            creature.setHomePos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
                            this.world.spawnEntity(creature);
                            break;
                        }
                    }
                    //spawn aquatic creatures
                    else {
                        if (this.entireAreaWater(creature, pos)) {
                            creature.setPosition(xSpawnPos, ySpawnPos, zSpawnPos);
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
                        creature.setHomePos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());
                        this.world.spawnEntity(creature);
                        break;
                    }
                }
            }
        }
    }

    public void updateCreature(RiftCreature creature) {
        for (NBTTagCompound partyMemCompound : this.creatureListNBT) {
            if (partyMemCompound.getUniqueId("UniqueID") != null && partyMemCompound.getUniqueId("UniqueID").equals(creature.getUniqueID())) {
                NBTTagCompound partyMemCompoundUpdt = PlayerTamedCreaturesHelper.createNBTFromCreature(creature);

                if (this.creatureListNBT.contains(partyMemCompound)) this.replaceInCreatureList(this.creatureListNBT.indexOf(partyMemCompound), partyMemCompoundUpdt);
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
        List<UUID> worldEntityListUUID = this.world.getLoadedEntityList().stream()
                .map(c -> c.getUniqueID())
                .collect(Collectors.toList());
        return worldEntityListUUID.contains(creature.getUniqueID());
    }

    public void addToCreatureList(NBTTagCompound tagCompound) {
        this.creatureListNBT.add(tagCompound);
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public void replaceInCreatureList(int pos, NBTTagCompound tagCompound) {
        this.creatureListNBT.set(pos, tagCompound);
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public void replaceInCreatureList(UUID uuid, NBTTagCompound tagCompound) {
        for (NBTTagCompound deployedCompound : this.creatureListNBT) {
            if (deployedCompound.getUniqueId("UniqueID").equals(uuid)) {
                for (String key : tagCompound.getKeySet()) {
                    NBTBase value = tagCompound.getTag(key);
                    deployedCompound.setTag(key, value);
                }
                break;
            }
        }

        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public void removeFromCreatureList(int pos) {
        this.creatureListNBT.remove(pos);
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public List<NBTTagCompound> getCreatureList() {
        return this.creatureListNBT;
    }

    //this is only for rendering and making obtaining creature data from this box
    //easier to deal with
    public List<RiftCreature> getCreatures() {
        List<RiftCreature> creatureList = new ArrayList<>();
        for (NBTTagCompound compound : this.creatureListNBT) {
            RiftCreature creature = PlayerTamedCreaturesHelper.createCreatureFromNBT(this.world, compound);
            if (creature != null) creatureList.add(creature);
        }
        return creatureList;
    }

    public void setCreatureList(List<NBTTagCompound> value) {
        this.creatureListNBT = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public int getMaxWanderingCreatures() {
        return 10 + this.creatureAmntLevel * 5;
    }

    public int getCreatureAmntLevel() {
        return this.creatureAmntLevel;
    }

    public void setCreatureAmntLevel(int value) {
        if (value > 4) return;
        this.creatureAmntLevel = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    public int getWanderRange() {
        return 16 + this.wanderRangeLevel * 8;
    }

    public int getWanderRangeLevel() {
        return this.wanderRangeLevel;
    }

    public void setWanderRangeLevel(int value) {
        if (value > 4) return;
        this.wanderRangeLevel = value;
        if (!this.world.isRemote) {
            this.markDirty();
            IBlockState state = this.world.getBlockState(this.pos);
            this.world.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.creatureAmntLevel = compound.getInteger("CreatureAmountLevel");
        this.wanderRangeLevel = compound.getInteger("WanderRangeLevel");

        if (compound.hasKey("BoxDeployedCreatures")) {
            NBTTagList boxDeployedCreaturesList = compound.getTagList("BoxDeployedCreatures", 10);
            if (!boxDeployedCreaturesList.isEmpty()) {
                List<NBTTagCompound> finalPartyCreatures = new ArrayList<>();
                for (int i = 0; i < boxDeployedCreaturesList.tagCount(); i++) {
                    finalPartyCreatures.add(boxDeployedCreaturesList.getCompoundTagAt(i));
                }
                this.creatureListNBT = finalPartyCreatures;
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("CreatureAmountLevel", this.creatureAmntLevel);
        compound.setInteger("WanderRangeLevel", this.wanderRangeLevel);

        NBTTagList boxDeployedCreaturesList = new NBTTagList();
        if (!this.creatureListNBT.isEmpty()) {
            for (NBTTagCompound boxNBT : this.creatureListNBT) boxDeployedCreaturesList.appendTag(boxNBT);
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
        this.creatureAmntLevel = tag.getInteger("CreatureAmountLevel");
        this.wanderRangeLevel = tag.getInteger("WanderRangeLevel");

        if (tag.hasKey("BoxDeployedCreatures")) {
            NBTTagList boxDeployedCreaturesList = tag.getTagList("BoxDeployedCreatures", 10);
            if (!boxDeployedCreaturesList.isEmpty()) {
                List<NBTTagCompound> finalPartyCreatures = new ArrayList<>();
                for (int i = 0; i < boxDeployedCreaturesList.tagCount(); i++) {
                    finalPartyCreatures.add(boxDeployedCreaturesList.getCompoundTagAt(i));
                }
                this.creatureListNBT = finalPartyCreatures;
            }
        }
    }


    //for testing only
    public List<String> creatureListNBTSimple() {
        List<String> list = new ArrayList<>();
        for (NBTTagCompound tagCompound : this.creatureListNBT){
            RiftCreatureType creatureType = RiftCreatureType.values()[tagCompound.getByte("CreatureType")];
            int level = tagCompound.getInteger("Level");
            list.add(creatureType.friendlyName+" (Level "+level+")");
        }
        return list;
    }
}
