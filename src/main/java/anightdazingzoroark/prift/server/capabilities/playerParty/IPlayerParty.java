package anightdazingzoroark.prift.server.capabilities.playerParty;

import anightdazingzoroark.prift.helper.FixedSizeList;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPlayerParty {
    //get the party
    FixedSizeList<CreatureNBT> getParty();
    //add party member
    void addPartyMember(RiftCreature creature);
    //get party member as nbt
    CreatureNBT getPartyMember(int index);
    //update party member
    void updatePartyMember(RiftCreature creature);
    //set party member
    void setPartyMember(int index, CreatureNBT creatureNBT);
    //check if new creatures can be added to the party
    boolean canAddToParty();

    //-----everything here is direct manipulating a specific party member-----
    //note that this is for editing nbt only, applyDeploymentOrTeleportation is where it gets applied only on the server
    void deployPartyMember(int index, boolean deploy, EntityPlayer player);
    void teleportPartyMember(int index, EntityPlayer player);
    boolean canDeployPartyMember(int index, EntityPlayer player);
    void applyDeploymentOrTeleportation(EntityPlayer player);

    //-----for parsing and exporting this into nbt-----
    NBTTagList getPartyAsNBTList();
    void parseNBTListToParty(NBTTagList nbtTagList);
    NBTTagCompound getTeleportationMarkerAsNBT();
    void parseNBTTeleportationMarker(NBTTagCompound nbtTagCompound);
}
