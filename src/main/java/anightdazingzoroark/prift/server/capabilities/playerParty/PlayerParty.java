package anightdazingzoroark.prift.server.capabilities.playerParty;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;
import org.jetbrains.annotations.NotNull;

public class PlayerParty implements IPlayerParty {
    //creature party is set here
    private final FixedSizeList<CreatureNBT> party = new FixedSizeList<>(6, new CreatureNBT());
    //teleportation markers are set in this map
    private final MutablePair<Integer, BlockPos> teleportationMarker = new MutablePair<>(-1, null);
    private int selectedPos;

    @Override
    public FixedSizeList<CreatureNBT> getParty() {
        return this.party;
    }

    @Override
    public void addPartyMember(RiftCreature creature) {
        for (int index = 0; index < this.party.size(); index++) {
            CreatureNBT creatureNBT = this.party.get(index);
            if (creatureNBT.nbtIsEmpty()) {
                this.party.set(index, new CreatureNBT(creature));
                break;
            }
        }
    }

    @Override
    public CreatureNBT getPartyMember(int index) {
        return this.party.get(index);
    }

    @Override
    public void updatePartyMember(RiftCreature creature) {
        for (int index = 0; index < this.party.size(); index++) {
            CreatureNBT creatureNBT = this.party.get(index);
            if (creatureNBT.nbtIsEmpty()) continue;
            if (creatureNBT.getUniqueID().equals(creature.getUniqueID())) {
                this.party.set(index, new CreatureNBT(creature));
                break;
            }
        }
    }

    @Override
    public void setPartyMember(int index, CreatureNBT creatureNBT) {
        this.party.set(index, creatureNBT);
    }

    @Override
    public boolean canAddToParty() {
        for (CreatureNBT creatureNBT : this.party.getList()) {
            if (creatureNBT.nbtIsEmpty()) return true;
        }
        return false;
    }

    @Override
    public void deployPartyMember(int index, boolean deploy, EntityPlayer player) {
        if (player == null || (deploy && !this.canDeployPartyMember(index, player))) return;

        //edit first the nbt
        CreatureNBT creatureNBT = this.getPartyMember(index);
        if (deploy) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
            this.setPartyMember(index, creatureNBT);
        }
        else {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
            this.setPartyMember(index, creatureNBT);
        }
    }

    @Override
    public void teleportPartyMember(int index, EntityPlayer player) {
        if (player == null || !this.canDeployPartyMember(index, player)) return;
        this.teleportationMarker.setLeft(index);
        this.teleportationMarker.setRight(player.getPosition());
    }

    @Override
    public void resetTeleportationMarker() {
        this.teleportationMarker.setLeft(-1);
        this.teleportationMarker.setRight(null);
    }

    @Override
    public boolean canDeployPartyMember(int index, EntityPlayer player) {
        if (player == null) return false;
        CreatureNBT creatureNBT = this.getPartyMember(index);
        if (creatureNBT.nbtIsEmpty()) return false;

        BlockPos posBelowPlayer = player.getPosition().down();
        if (creatureNBT.getCreatureType().isAquatic) {
            if (player.world.getBlockState(posBelowPlayer).getMaterial() == Material.WATER) return true;
            else if (player.world.getBlockState(posBelowPlayer).getMaterial() != Material.AIR
                && creatureNBT.getCreatureType().isAmphibious) return true;
        }
        else {
            if (player.world.getBlockState(posBelowPlayer).getMaterial() == Material.AIR && player.isRiding()) return true;
            else if (player.world.getBlockState(posBelowPlayer).getMaterial() != Material.AIR) return true;
        }
        return false;
    }

    public void applyDeploymentOrTeleportation(EntityPlayer player) {
        if (player == null) return;

        for (int index = 0; index < this.party.size(); index++) {
            CreatureNBT creatureNBT = this.party.get(index);
            if (creatureNBT.nbtIsEmpty()) continue;

            //get corresponding creature in the world
            RiftCreature corresponded = creatureNBT.findCorrespondingCreature(player.world);
            //creature is found in world
            if (corresponded != null) {
                //-----deployment-----
                if (creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY) {
                    corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
                    this.setPartyMember(index, new CreatureNBT(corresponded));
                }
                else if (creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE) {
                    corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
                    this.setPartyMember(index, new CreatureNBT(corresponded));
                }

                //-----teleportation (server only cos well)-----
                if (this.teleportationMarker.getLeft() == index && this.teleportationMarker.getRight() != null && !player.world.isRemote) {
                    BlockPos posToTeleportTo = this.teleportationMarker.getRight();
                    corresponded.setPosition(posToTeleportTo.getX(), posToTeleportTo.getY(), posToTeleportTo.getZ());

                    //reset marker
                    this.resetTeleportationMarker();
                }
            }
            else {
                //create the creature, note that its server only
                if (creatureNBT.getDeploymentType() == PlayerTamedCreatures.DeploymentType.PARTY && !player.world.isRemote) {
                    RiftCreature toCreate = creatureNBT.getCreatureAsNBT(player.world);
                    toCreate.setPosition(player.posX, player.posY, player.posZ);
                    player.world.spawnEntity(toCreate);
                }
                //PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE results in.....
                //nothing happening xd
            }
        }
    }

    @Override
    public int getQuickSelectPos() {
        return this.selectedPos;
    }

    @Override
    public int getNextQuickSelectPos() {
        return this.selectedPos + 1 < PlayerPartyHelper.maxSize ? this.selectedPos + 1 : 0;
    }

    @Override
    public int getPrevQuickSelectPos() {
        return this.selectedPos - 1 >= 0 ? this.selectedPos - 1 : PlayerPartyHelper.maxSize - 1;
    }

    @Override
    public void nextQuickSelectPos() {
        this.selectedPos = this.getNextQuickSelectPos();
    }

    @Override
    public void prevQuickSelectPos() {
        this.selectedPos = this.getPrevQuickSelectPos();
    }

    @Override
    public NBTTagCompound getPartyNBT() {
        NBTTagCompound toReturn = new NBTTagCompound();

        //party
        NBTTagList partyTagList = this.getPartyAsNBTList();
        toReturn.setTag("PartyMembers", partyTagList);

        //selected pos
        toReturn.setInteger("SelectedPos", this.selectedPos);

        return toReturn;
    }

    @Override
    public void parsePartyNBT(NBTTagCompound nbtTagCompound) {
        //party
        NBTTagList partyNBTTagList = nbtTagCompound.getTagList("PartyMembers", 10);
        this.parseNBTListToParty(partyNBTTagList);

        //selected pos
        this.selectedPos = nbtTagCompound.getInteger("SelectedPos");
    }

    @Override
    public NBTTagCompound getPartyNBTForSync() {
        NBTTagCompound toReturn = new NBTTagCompound();

        //party
        NBTTagList partyTagList = this.getPartyAsNBTList();
        toReturn.setTag("PartyMembers", partyTagList);

        //teleportation info
        NBTTagCompound tpInfo = this.getTeleportationMarkerAsNBT();
        toReturn.setTag("TeleportationMarker", tpInfo);

        return toReturn;
    }

    @Override
    public void parsePartyNBTForSync(NBTTagCompound nbtTagCompound) {
        //party
        NBTTagList partyNBTTagList = nbtTagCompound.getTagList("PartyMembers", 10);
        this.parseNBTListToParty(partyNBTTagList);

        //teleportation info
        NBTTagCompound tpInfo = nbtTagCompound.getCompoundTag("TeleportationMarker");
        this.parseNBTTeleportationMarker(tpInfo);
    }

    @Override
    @NotNull
    public NBTTagList getPartyAsNBTList() {
        NBTTagList toReturn = new NBTTagList();

        for (int index = 0; index < this.party.size(); index++) {
            CreatureNBT creatureNBT = this.party.get(index);
            if (creatureNBT.nbtIsEmpty()) continue;
            NBTTagCompound compound = new NBTTagCompound();
            compound.setInteger("Index", index);
            compound.setTag("Creature", creatureNBT.getCreatureNBT());
            toReturn.appendTag(compound);
        }

        return toReturn;
    }

    @Override
    public void parseNBTListToParty(NBTTagList nbtTagList) {
        this.party.clear();
        for (int index = 0; index < nbtTagList.tagCount(); index++) {
            NBTTagCompound compound = nbtTagList.getCompoundTagAt(index);
            if (compound.isEmpty()) continue;
            int partyMemIndex = compound.getInteger("Index");
            CreatureNBT creatureNBT = new CreatureNBT(compound.getCompoundTag("Creature"));
            this.party.set(partyMemIndex, creatureNBT);
        }
    }

    @Override
    public NBTTagCompound getTeleportationMarkerAsNBT() {
        if (this.teleportationMarker.getLeft() < 0 || this.teleportationMarker.getRight() == null) return new NBTTagCompound();

        NBTTagCompound toReturn = new NBTTagCompound();
        toReturn.setInteger("Index", this.teleportationMarker.left);
        toReturn.setIntArray("TeleportTo", new int[]{
                this.teleportationMarker.getRight().getX(),
                this.teleportationMarker.getRight().getY(),
                this.teleportationMarker.getRight().getZ()
        });
        return toReturn;
    }

    @Override
    public void parseNBTTeleportationMarker(NBTTagCompound nbtTagCompound) {
        if (nbtTagCompound.isEmpty()) return;

        if (nbtTagCompound.hasKey("Index")) this.teleportationMarker.setLeft(nbtTagCompound.getInteger("Index"));
        if (nbtTagCompound.hasKey("TeleportTo")) {
            int[] teleportToArr = nbtTagCompound.getIntArray("TeleportTo");
            this.teleportationMarker.setRight(new BlockPos(
                    teleportToArr[0],
                    teleportToArr[1],
                    teleportToArr[2]
            ));
        }
    }
}
