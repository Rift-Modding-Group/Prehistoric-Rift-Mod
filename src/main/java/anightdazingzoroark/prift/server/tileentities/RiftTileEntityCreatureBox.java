package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.helper.ChunkPosWithVerticality;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiftTileEntityCreatureBox extends TileEntity implements ITickable {
    private final FixedSizeList<CreatureNBT> creatureListNBT = new FixedSizeList<>(RiftCreatureBox.maxDeployableCreatures, new CreatureNBT());
    private UUID uniqueID;
    private UUID ownerID;
    private String ownerName = "";
    private int deploymentRange = 1;

    @Override
    public void update() {
        //if box has contents, make it so that its indestructible when there's creatures inside
        RiftCreatureBox creatureBox = (RiftCreatureBox)this.world.getBlockState(this.pos).getBlock();
        if (this.isUnbreakable()) creatureBox.setHardness(-1.0f);
        else creatureBox.setHardness(0f);

        //create creatures
        if (!this.world.isRemote) this.createCreaturesForWandering();
    }

    public void setOwner(EntityPlayer player) {
        this.ownerID = player.getUniqueID();
        this.ownerName = player.getName();
        this.updateServerData();
    }

    public String getOwnerName() {
        return this.ownerName.isEmpty() ? I18n.format("creature_box.no_owner_name") : this.ownerName;
    }

    public UUID getOwnerID() {
        return this.ownerID;
    }

    public void setUniqueID(UUID uuid) {
        this.uniqueID = uuid;
        this.updateServerData();
    }

    public UUID getUniqueID() {
        return this.uniqueID;
    }

    public void setDeploymentRange(int value) {
        this.deploymentRange = value;
        this.updateServerData();
    }

    public int getDeploymentRange() {
        return this.deploymentRange;
    }

    public int getDeploymentRangeWidth() {
        return 2 * this.deploymentRange + 1;
    }

    public boolean posWithinDeploymentRange(BlockPos testPos) {
        int chunkX = testPos.getX() >> 4;
        int chunkY = testPos.getY() >> 4;
        int chunkZ = testPos.getZ() >> 4;

        ChunkPosWithVerticality blockChunk = new ChunkPosWithVerticality(chunkX, chunkY, chunkZ);
        return this.chunksWithinDeploymentRange().contains(blockChunk);
    }

    public List<ChunkPosWithVerticality> chunksWithinDeploymentRange() {
        List<ChunkPosWithVerticality> toReturn = new ArrayList<>();
        int chunkX = this.pos.getX() >> 4;
        int chunkY = this.pos.getY() >> 4;
        int chunkZ = this.pos.getZ() >> 4;

        for (int x = -this.deploymentRange; x <= this.deploymentRange; x++) {
            for (int y = -this.deploymentRange; y <= this.deploymentRange; y++) {
                for (int z = -this.deploymentRange; z <= this.deploymentRange; z++) {
                    toReturn.add(new ChunkPosWithVerticality(chunkX + x, chunkY + y, chunkZ + z));
                }
            }
        }

        return toReturn;
    }

    public int[] getXBounds() {
        List<ChunkPosWithVerticality> list = this.chunksWithinDeploymentRange();
        int minX = list.get(0).getXStart();
        int maxX = list.get(list.size() - 1).getXEnd() + 1;
        return new int[]{minX, maxX};
    }

    public int[] getYBounds() {
        List<ChunkPosWithVerticality> list = this.chunksWithinDeploymentRange();
        int minY = Math.max(0, list.get(0).getYStart());
        int maxY = list.get(list.size() - 1).getYEnd() + 1;
        return new int[]{minY, maxY};
    }

    public int[] getZBounds() {
        List<ChunkPosWithVerticality> list = this.chunksWithinDeploymentRange();
        int minZ = list.get(0).getZStart();
        int maxZ = list.get(list.size() - 1).getZEnd() + 1;
        return new int[]{minZ, maxZ};
    }

    //this creates the creatures that wander around the box
    private void createCreaturesForWandering() {
        for (CreatureNBT tagCompound : this.creatureListNBT.getList()) {
            if (tagCompound.nbtIsEmpty()) continue;

            UUID uuid = tagCompound.getUniqueID();
            if (this.creatureWithUUIDExists(uuid)) continue;

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

    private boolean creatureWithUUIDExists(UUID uuid) {
        if (uuid == null || uuid.equals(RiftUtil.nilUUID)) return false;
        if (!(this.world instanceof WorldServer)) return false;
        return ((WorldServer) this.world).getEntityFromUuid(uuid) != null;
    }

    private boolean creatureExistsInWorld(RiftCreature creature) {
        if (creature == null) return false;
        if (!(this.world instanceof WorldServer)) return false;
        return ((WorldServer) this.world).getEntityFromUuid(creature.getUniqueID()) != null;
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
        if (compound.hasKey("UniqueID")) this.uniqueID = compound.getUniqueId("UniqueID");
        this.deploymentRange = compound.getInteger("DeploymentRange");
        if (compound.hasKey("OwnerID")) this.ownerID = compound.getUniqueId("OwnerID");
        this.ownerName = compound.getString("OwnerName");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        NBTTagList boxDeployedCreaturesList = new NBTTagList();
        for (CreatureNBT boxNBT : this.creatureListNBT.getList()) boxDeployedCreaturesList.appendTag(boxNBT.getCreatureNBT());
        compound.setTag("BoxDeployedCreatures", boxDeployedCreaturesList);

        if (this.uniqueID != null && !this.uniqueID.equals(RiftUtil.nilUUID)) compound.setUniqueId("UniqueID", this.uniqueID);
        compound.setInteger("DeploymentRange", this.deploymentRange);
        if (this.ownerID != null && !this.ownerID.equals(RiftUtil.nilUUID)) compound.setUniqueId("OwnerID", this.ownerID);
        compound.setString("OwnerName", this.ownerName);

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
