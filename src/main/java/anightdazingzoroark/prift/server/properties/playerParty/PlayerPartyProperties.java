package anightdazingzoroark.prift.server.properties.playerParty;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.FixedSizeListPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.HashMapPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.IntPropertyValue;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerPartyProperties extends AbstractEntityProperties<EntityPlayer> {
    //this ensures that data for deployed creatures is accurately reflected on UIs
    private final HashMap<Integer, RiftCreature> convertedPartyHash = new HashMap<>();

    public PlayerPartyProperties(@NotNull String key, @NotNull EntityPlayer entityHolder) {
        super(key, entityHolder);
    }

    @Override
    protected void registerDefaults(EntityPlayer entity) {
        this.register(new FixedSizeListPropertyValue<CreatureNBT>(
                "PlayerParty", new CreatureNBT(), PlayerPartyHelper.maxSize,
                fixedSizeList -> {
                    NBTTagList partyTagList = new NBTTagList();
                    for (int index = 0; index < fixedSizeList.size(); index++) {
                        CreatureNBT creatureNBT = fixedSizeList.get(index);
                        if (creatureNBT.nbtIsEmpty()) continue;
                        NBTTagCompound toAppend = new NBTTagCompound();
                        toAppend.setInteger("Index", index);
                        toAppend.setTag("Creature", creatureNBT.getCreatureNBT());
                        partyTagList.appendTag(toAppend);
                    }
                    return partyTagList;
                },
                nbtBase -> {
                    FixedSizeList<CreatureNBT> toReturn = new FixedSizeList<>(PlayerPartyHelper.maxSize, new CreatureNBT());
                    if (!(nbtBase instanceof NBTTagList partyTagList)) return toReturn;
                    for (int index = 0; index < partyTagList.tagCount(); index++) {
                        NBTTagCompound tagCompound = partyTagList.getCompoundTagAt(index);
                        int partyMemIndex = tagCompound.getInteger("Index");
                        CreatureNBT creatureNBT = new CreatureNBT(tagCompound.getCompoundTag("Creature"));
                        toReturn.set(partyMemIndex, creatureNBT);
                    }
                    return toReturn;
                }
        ));
        this.register(new HashMapPropertyValue<Integer, UUID> (
                "DeployedPartyMembers",
                hashMap -> {
                    NBTTagList toReturn = new NBTTagList();
                    for (Map.Entry<Integer, UUID> entry : hashMap.entrySet()) {
                        NBTTagCompound compound = new NBTTagCompound();
                        compound.setInteger("Index", entry.getKey());
                        compound.setUniqueId("CreatureUUID", entry.getValue());
                        toReturn.appendTag(compound);
                    }
                    return toReturn;
                },
                nbtBase -> {
                    HashMap<Integer, UUID> toReturn = new HashMap<>();
                    if (!(nbtBase instanceof NBTTagList nbtTagList)) return toReturn;
                    for (int listIndex = 0; listIndex < nbtTagList.tagCount(); listIndex++) {
                        NBTTagCompound compound = nbtTagList.getCompoundTagAt(listIndex);
                        if (compound.isEmpty()) continue;

                        int index = compound.getInteger("Index");
                        UUID creatureUUID  = compound.getUniqueId("CreatureUUID");

                        toReturn.put(index, creatureUUID);
                    }

                    return toReturn;
                }
        ));
        this.register(new IntPropertyValue("QuickSelectedPos", 0));
    }

    //-----direct party member editing and getting-----
    public FixedSizeList<CreatureNBT> getPlayerParty() {
        return this.get("PlayerParty");
    }

    public void setPlayerParty(FixedSizeList<CreatureNBT> playerParty) {
        this.set("PlayerParty", playerParty);
    }

    //-----indirect party member editing and getting-----
    public void addPartyMember(RiftCreature creature) {
        if (this.getEntityHolder().world.isRemote) return;
        if (!this.canAddToParty()) return;

        FixedSizeList<CreatureNBT> playerPartyList = this.getPlayerParty();
        for (int index = 0; index < playerPartyList.size(); index++) {
            CreatureNBT creatureNBT = playerPartyList.get(index);
            if (creatureNBT.nbtIsEmpty()) {
                playerPartyList.set(index, new CreatureNBT(creature));
                break;
            }
        }
        this.setPlayerParty(playerPartyList);
    }

    public void updatePartyMember(RiftCreature creature) {
        if (this.getEntityHolder().world.isRemote) return;

        FixedSizeList<CreatureNBT> playerPartyList = this.getPlayerParty();
        for (int index = 0; index < playerPartyList.size(); index++) {
            CreatureNBT creatureNBT = playerPartyList.get(index);
            if (creatureNBT.nbtIsEmpty()) continue;
            if (creatureNBT.getUniqueID().equals(creature.getUniqueID())) {
                playerPartyList.set(index, new CreatureNBT(creature));
                break;
            }
        }
        this.setPlayerParty(playerPartyList);
    }

    public CreatureNBT getPartyMember(int index) {
        return this.getPlayerParty().get(index);
    }

    public boolean canAddToParty() {
        FixedSizeList<CreatureNBT> playerPartyList = this.getPlayerParty();
        for (CreatureNBT creatureNBT : playerPartyList.getList()) {
            if (creatureNBT.nbtIsEmpty()) return true;
        }
        return false;
    }

    //---for deployment and teleportation---
    public boolean canDeployPartyMember(int index) {
        CreatureNBT creatureNBT = this.getPartyMember(index);
        if (creatureNBT.nbtIsEmpty()) return false;

        BlockPos posBelowPlayer = this.getEntityHolder().getPosition().down();
        if (creatureNBT.getCreatureType().isAquatic) {
            if (this.getEntityHolder().world.getBlockState(posBelowPlayer).getMaterial() == Material.WATER) return true;
            else if (this.getEntityHolder().world.getBlockState(posBelowPlayer).getMaterial() != Material.AIR
                    && creatureNBT.getCreatureType().isAmphibious) return true;
        }
        else {
            if (this.getEntityHolder().world.getBlockState(posBelowPlayer).getMaterial() == Material.AIR && this.getEntityHolder().isRiding()) return true;
            else if (this.getEntityHolder().world.getBlockState(posBelowPlayer).getMaterial() != Material.AIR) return true;
        }
        return false;
    }

    public void prepareForBoxMovement(int index) {
        if (this.getEntityHolder().world.isRemote) return;
        CreatureNBT creatureNBT = this.getPartyMember(index);

        //find corresponding creature first
        RiftCreature corresponded = creatureNBT.findCorrespondingCreature(this.getEntityHolder().world);

        //drop inventory
        if (corresponded != null) {
            corresponded.creatureInventory.dropAllItems(this.getEntityHolder().getEntityWorld(), corresponded.getPosition());
        }
        else creatureNBT.dropInventory(this.getEntityHolder().getEntityWorld(), this.getEntityHolder().getPosition());

        //override the stored nbt with the deployed creature's nbt
        creatureNBT.overrideCreature(corresponded);

        //continue as usual
        creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
        FixedSizeList<CreatureNBT> playerPartyList = this.getPlayerParty();
        playerPartyList.set(index, creatureNBT);
        this.setPlayerParty(playerPartyList);
        this.removeCreatureFromDeployMap(index);

        //if the corresponding creature exists (expected), despawn it
        if (corresponded != null) corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.BASE_INACTIVE);
    }

    public void deployPartyMember(int index, boolean deploy) {
        if (this.getEntityHolder().world.isRemote) return;

        CreatureNBT creatureNBT = this.getPartyMember(index);

        //find corresponding creature first
        RiftCreature corresponded = creatureNBT.findCorrespondingCreature(this.getEntityHolder().world);

        //if true, deploy in the world
        if (deploy) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
            FixedSizeList<CreatureNBT> playerPartyList = this.getPlayerParty();
            playerPartyList.set(index, creatureNBT);
            this.setPlayerParty(playerPartyList);

            //if the corresponding creature doesn't exist (expected), spawn it
            //and add to deployed creature map
            if (corresponded == null) {
                RiftCreature creatureToCreate = creatureNBT.getCreatureAsNBT(this.getEntityHolder().world);
                creatureToCreate.setPosition(this.getEntityHolder().posX, this.getEntityHolder().posY, this.getEntityHolder().posZ);
                this.getEntityHolder().world.spawnEntity(creatureToCreate);
                this.addCreatureToDeployMap(index, creatureToCreate);
            }
            //if it already exists for some reason, just add it to deployed creature map
            else this.addCreatureToDeployMap(index, corresponded);
        }
        //else, dismiss it
        else {
            //firstly override the stored nbt with the deployed creature's nbt
            creatureNBT.overrideCreature(corresponded);

            //continue as usual
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
            FixedSizeList<CreatureNBT> playerPartyList = this.getPlayerParty();
            playerPartyList.set(index, creatureNBT);
            this.setPlayerParty(playerPartyList);
            this.removeCreatureFromDeployMap(index);

            //if the corresponding creature exists (expected), despawn it
            if (corresponded != null) corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
        }
    }

    public void teleportPartyMember(int index) {
        if (this.getEntityHolder().world.isRemote) return;
        CreatureNBT creatureNBT = this.getPartyMember(index);
        RiftCreature corresponded = creatureNBT.findCorrespondingCreature(this.getEntityHolder().world);
        if (corresponded == null) return;
        corresponded.setPosition(this.getEntityHolder().posX, this.getEntityHolder().posY, this.getEntityHolder().posZ);
    }

    //-----direct selected pos editing and getting and syncing-----
    public int getQuickSelectPos() {
        return this.get("QuickSelectedPos");
    }

    private void setQuickSelectPos(int value) {
        this.set("QuickSelectedPos", value);
    }

    //indirect selected pos editing and getting
    public int getNextQuickSelectPos() {
        return this.getQuickSelectPos() + 1 < PlayerPartyHelper.maxSize ? this.getQuickSelectPos() + 1 : 0;
    }

    public int getPrevQuickSelectPos() {
        return this.getQuickSelectPos() - 1 >= 0 ? this.getQuickSelectPos() - 1 : PlayerPartyHelper.maxSize - 1;
    }

    public void nextQuickSelectPos() {
        this.setQuickSelectPos(this.getNextQuickSelectPos());
    }

    public void prevQuickSelectPos() {
        this.setQuickSelectPos(this.getPrevQuickSelectPos());
    }

    //-----direct getting and setting of stuff for deployed party members-----
    public HashMap<Integer, UUID> getDeployedPartyMemberMap() {
        return this.get("DeployedPartyMembers");
    }

    public void setDeployedPartyMemberMap(HashMap<Integer, UUID> value) {
        this.set("DeployedPartyMembers", value);
    }

    //-----indirect getting and setting of stuff for deployed party members-----
    public void addCreatureToDeployMap(int index, RiftCreature creature) {
        HashMap<Integer, UUID> deployedCreatureMap = this.getDeployedPartyMemberMap();
        deployedCreatureMap.put(index, creature.getUniqueID());
        this.setDeployedPartyMemberMap(deployedCreatureMap);
    }

    public void removeCreatureFromDeployMap(int index) {
        HashMap<Integer, UUID> deployedCreatureMap = this.getDeployedPartyMemberMap();
        deployedCreatureMap.remove(index);
        this.setDeployedPartyMemberMap(deployedCreatureMap);
    }

    @SideOnly(Side.CLIENT)
    public RiftCreature getLoadedDeployedCreature(int index) {
        if (!this.convertedPartyHash.containsKey(index)) return null;
        return this.convertedPartyHash.get(index);
    }

    @SideOnly(Side.CLIENT)
    public boolean deployedCreatureIsLoadedAtIndex(int index) {
        return this.convertedPartyHash.containsKey(index);
    }

    //this will be ticked client side only
    @SideOnly(Side.CLIENT)
    public void forceReloadPartyMemberMap() {
        if (this.getEntityHolder().world == null
                || !this.getEntityHolder().world.isRemote
                || this.getEntityHolder().world.loadedEntityList.isEmpty()) return;

        //equality will block changes to converted party hash
        HashMap<Integer, UUID> deployedCreatureMap = this.getDeployedPartyMemberMap();
        if (this.convertedPartyHash.keySet().equals(deployedCreatureMap.keySet())) return;

        this.convertedPartyHash.clear();
        for (Map.Entry<Integer, UUID> entry : deployedCreatureMap.entrySet()) {
            UUID uuid = entry.getValue();
            Entity correspondingEntity = RiftUtil.getEntityFromUUID(this.getEntityHolder().world, uuid);
            if (!(correspondingEntity instanceof RiftCreature creature)) continue;
            this.convertedPartyHash.put(entry.getKey(), creature);
        }
    }
}
