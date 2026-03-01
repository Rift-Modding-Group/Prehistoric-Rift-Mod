package anightdazingzoroark.prift.server.properties.playerParty;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.propertySystem.networking.PropertiesNetworking;
import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.IntPropertyValue;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.RiftPropertyRegistry;
import anightdazingzoroark.prift.server.properties.propertyValues.FixedSizeListCreaturePropertyValue;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class PlayerPartyProperties extends AbstractEntityProperties {
    public PlayerPartyProperties(@NotNull String propertyName, @NotNull Entity entityHolder) {
        super(propertyName, entityHolder);
    }

    @Override
    protected void registerDefaults(Entity entity) {
        this.put(new FixedSizeListCreaturePropertyValue("PlayerParty", new FixedSizeList<>(6, new CreatureNBT())));
        this.put(new IntPropertyValue("QuickSelectedPos", 0));
    }

    //-----direct party member editing and getting and sycncing-----
    public FixedSizeList<CreatureNBT> getPlayerParty() {
        return (FixedSizeList<CreatureNBT>) this.getProperty("PlayerParty").getValue();
    }

    public void setPlayerParty(FixedSizeList<CreatureNBT> playerParty) {
        this.put(new FixedSizeListCreaturePropertyValue("PlayerParty", playerParty));
    }

    //-----indirect party member editing and getting-----
    public void addPartyMember(EntityPlayer player, RiftCreature creature) {
        if (player.world.isRemote) return;
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

    public void updatePartyMember(EntityPlayer player, RiftCreature creature) {
        if (player.world.isRemote) return;

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

    public void deployPartyMember(int index, EntityPlayer player, boolean deploy) {
        if (player.world.isRemote) return;

        CreatureNBT creatureNBT = this.getPartyMember(index);
        //find corresponding creature first
        RiftCreature corresponded = creatureNBT.findCorrespondingCreature(player.world);

        //if true, deploy in the world
        if (deploy) {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY);
            FixedSizeList<CreatureNBT> playerPartyList = this.getPlayerParty();
            playerPartyList.set(index, creatureNBT);
            this.setPlayerParty(playerPartyList);

            //if the corresponding creature doesn't exist (expected), spawn it
            if (corresponded == null) {
                RiftCreature creatureToCreate = creatureNBT.getCreatureAsNBT(player.world);
                creatureToCreate.setPosition(player.posX, player.posY, player.posZ);
                player.world.spawnEntity(creatureToCreate);
            }
        }
        //else, dismiss it
        else {
            creatureNBT.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
            FixedSizeList<CreatureNBT> playerPartyList = this.getPlayerParty();
            playerPartyList.set(index, creatureNBT);
            this.setPlayerParty(playerPartyList);

            //if the corresponding creature exists (expected), despawn it
            if (corresponded != null) corresponded.setDeploymentType(PlayerTamedCreatures.DeploymentType.PARTY_INACTIVE);
        }
    }

    public void teleportPartyMember(int index, EntityPlayer player) {
        if (player.world.isRemote) return;
        CreatureNBT creatureNBT = this.getPartyMember(index);
        RiftCreature corresponded = creatureNBT.findCorrespondingCreature(player.world);
        if (corresponded == null) return;
        corresponded.setPosition(player.posX, player.posY, player.posZ);
    }

    //-----direct selected pos editing and getting and syncing-----
    public int getQuickSelectPos() {
        return (Integer) this.getProperty("QuickSelectedPos").getValue();
    }

    private void setQuickSelectPos(int value) {
        this.put(new IntPropertyValue("QuickSelectedPos", value));
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
}
