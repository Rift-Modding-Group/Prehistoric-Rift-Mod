package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.client.newui.holder.SelectedCreatureInfo;
import anightdazingzoroark.prift.client.newui.screens.synced.RiftCreatureBoxScreen;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.helper.ChunkPosWithVerticality;
import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.FixedSizeListPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.IntPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.StringPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.UUIDPropertyValue;
import anightdazingzoroark.prift.server.blocks.RiftCreatureBox;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.entity.CreatureDeployment;
import anightdazingzoroark.prift.server.entity.RiftEgg;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.google.common.base.Predicate;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RiftTileEntityCreatureBox extends RiftTileEntity implements ITickable, IGuiHolder<PosGuiData> {
    //for use in ui
    private boolean isCreatureSwitching;
    private SelectedCreatureInfo selectedCreatureInfo;
    private SelectedCreatureInfo.SwapInfo creatureSwapInfo = new SelectedCreatureInfo.SwapInfo();
    private int currentBoxIndex;

    @Override
    public void registerValues() {
        this.register(new FixedSizeListPropertyValue<CreatureNBT>(
                "CreatureList", new CreatureNBT(), RiftCreatureBox.maxDeployableCreatures,
                fixedSizeList -> {
                    NBTTagCompound toReturn = new NBTTagCompound();
                    getNBTFromDeployedList(toReturn, fixedSizeList);
                    return toReturn;
                },
                nbtBase -> {
                    if (!(nbtBase instanceof NBTTagCompound nbtTagCompound)) {
                        return new FixedSizeList<>(RiftCreatureBox.maxDeployableCreatures, new CreatureNBT());
                    }
                    return getDeployedListFromNBT(nbtTagCompound);
                }
        ));
        this.register(new UUIDPropertyValue("UniqueID", RiftUtil.nilUUID));
        this.register(new UUIDPropertyValue("OwnerID", RiftUtil.nilUUID));
        this.register(new StringPropertyValue("OwnerName"));
        this.register(new IntPropertyValue("DeploymentRange", 1));
    }

    @Override
    public void update() {
        //if box has contents, make it so that its indestructible when there's creatures inside
        RiftCreatureBox creatureBox = (RiftCreatureBox)this.world.getBlockState(this.pos).getBlock();
        if (this.isUnbreakable()) creatureBox.setHardness(-1.0f);
        else creatureBox.setHardness(0f);

        //create creatures
        //if (!this.world.isRemote) this.createCreaturesForWandering();

        //print creatures
        //System.out.println("this.getDeployedCreatures(): "+this.getDeployedCreatures());

        //remove creatures that are not recently spawned from the wander range of the creature box
        if (GeneralConfig.creatureBoxPreventMobSpawn) {
            AxisAlignedBB removeAABB = this.chunkAsAABB();
            for (EntityLiving creature : this.world.getEntitiesWithinAABB(EntityLiving.class, removeAABB, new Predicate<EntityLiving>() {
                @Override
                public boolean apply(@Nullable EntityLiving entityLiving) {
                    if (entityLiving == null) return false;
                    else if (entityLiving instanceof RiftEgg) return false;
                    else if (entityLiving instanceof RiftLargeWeapon) return false;
                    else if (entityLiving instanceof EntityTameable) return !((EntityTameable) entityLiving).isTamed() && entityLiving.ticksExisted <= 100;
                    else return entityLiving.ticksExisted <= 100;
                }
            })) {
                if (creature instanceof RiftCreature) ((RiftCreature) creature).setDeploymentType(CreatureDeployment.BASE_INACTIVE);
                else this.world.removeEntity(creature);
            }
        }
    }

    //-----getters and setters-----
    public void setDeployedCreatures(FixedSizeList<CreatureNBT> value) {
        this.set("CreatureList", value);
    }

    public FixedSizeList<CreatureNBT> getDeployedCreatures() {
        return this.get("CreatureList");
    }

    public void setOwner(EntityPlayer player) {
        this.set("OwnerID", player.getUniqueID(), false);
        this.set("OwnerName", player.getName(), false);
        this.updateServerData();
    }

    public String getOwnerName() {
        String ownerName = this.get("OwnerName");
        return ownerName.isEmpty() ? I18n.format("creature_box.no_owner_name") : ownerName;
    }

    public UUID getOwnerID() {
        return this.get("OwnerID");
    }

    public void setUniqueID(UUID uuid) {
        this.set("UniqueID", uuid);
    }

    public UUID getUniqueID() {
        return this.get("UniqueID");
    }

    public void setDeploymentRange(int value) {
        this.set("DeploymentRange", value);
    }

    public int getDeploymentRange() {
        return this.get("DeploymentRange");
    }

    //-----indirect setters and getters-----
    public boolean isUnbreakable() {
        return !this.getDeployedCreatures().isEmpty();
    }

    public int getDeploymentRangeWidth() {
        return 2 * this.getDeploymentRange() + 1;
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

        int deploymentRange = this.getDeploymentRange();
        for (int x = -deploymentRange; x <= deploymentRange; x++) {
            for (int y = -deploymentRange; y <= deploymentRange; y++) {
                for (int z = -deploymentRange; z <= deploymentRange; z++) {
                    toReturn.add(new ChunkPosWithVerticality(chunkX + x, chunkY + y, chunkZ + z));
                }
            }
        }

        return toReturn;
    }

    public AxisAlignedBB chunkAsAABB() {
        return new AxisAlignedBB(
                this.getXBounds()[0],
                this.getYBounds()[0],
                this.getZBounds()[0],
                this.getXBounds()[1],
                this.getYBounds()[1],
                this.getZBounds()[1]
        );
    }

    public int[] getXBounds() {
        List<ChunkPosWithVerticality> list = this.chunksWithinDeploymentRange();
        int minX = list.getFirst().getXStart();
        int maxX = list.getLast().getXEnd() + 1;
        return new int[]{minX, maxX};
    }

    public int[] getYBounds() {
        List<ChunkPosWithVerticality> list = this.chunksWithinDeploymentRange();
        int minY = Math.max(0, list.getFirst().getYStart());
        int maxY = list.getLast().getYEnd() + 1;
        return new int[]{minY, maxY};
    }

    public int[] getZBounds() {
        List<ChunkPosWithVerticality> list = this.chunksWithinDeploymentRange();
        int minZ = list.getFirst().getZStart();
        int maxZ = list.getLast().getZEnd() + 1;
        return new int[]{minZ, maxZ};
    }

    //-----getters and setters for ui-----
    public boolean getIsCreatureSwitching() {
        return this.isCreatureSwitching;
    }

    public void setIsCreatureSwitching(boolean value) {
        this.isCreatureSwitching = value;
    }

    public SelectedCreatureInfo getSelectedCreatureInfo() {
        return this.selectedCreatureInfo;
    }

    public void setSelectedCreatureInfo(SelectedCreatureInfo value) {
        this.selectedCreatureInfo = value;
    }

    public SelectedCreatureInfo.SwapInfo getCreatureSwapInfo() {
        return this.creatureSwapInfo;
    }

    public void setCreatureSwapInfo(SelectedCreatureInfo.SwapInfo swapInfo) {
        this.creatureSwapInfo = swapInfo;
    }

    public int getCurrentBoxIndex() {
        return this.currentBoxIndex;
    }

    public void setCurrentBoxIndex(int value) {
        this.currentBoxIndex = value;
    }

    //-----ui-----
    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return RiftCreatureBoxScreen.buildCreatureBoxUI(data, syncManager, settings);
    }

    //-----static helper functions-----
    public static FixedSizeList<CreatureNBT> getDeployedListFromNBT(NBTTagCompound nbtTagCompound) {
        FixedSizeList<CreatureNBT> toReturn = new FixedSizeList<>(RiftCreatureBox.maxDeployableCreatures, new CreatureNBT());

        if (nbtTagCompound.hasKey("BoxDeployedCreatures")) {
            NBTTagList boxDeployedCreaturesList = nbtTagCompound.getTagList("BoxDeployedCreatures", 10);
            if (!boxDeployedCreaturesList.isEmpty()) {
                for (int i = 0; i < boxDeployedCreaturesList.tagCount(); i++) {
                    toReturn.set(i, new CreatureNBT(boxDeployedCreaturesList.getCompoundTagAt(i)));
                }
            }
        }
        return toReturn;
    }

    public static void getNBTFromDeployedList(NBTTagCompound nbtTagCompound, FixedSizeList<CreatureNBT> deployedList) {
        NBTTagList boxDeployedCreaturesList = new NBTTagList();
        for (CreatureNBT boxNBT : deployedList.getList()) boxDeployedCreaturesList.appendTag(boxNBT.getCreatureNBT());
        nbtTagCompound.setTag("BoxDeployedCreatures", boxDeployedCreaturesList);
    }
}
