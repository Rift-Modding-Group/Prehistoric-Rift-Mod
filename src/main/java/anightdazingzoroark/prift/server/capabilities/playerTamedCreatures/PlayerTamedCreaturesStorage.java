package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PlayerTamedCreaturesStorage implements Capability.IStorage<IPlayerTamedCreatures> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IPlayerTamedCreatures> capability, IPlayerTamedCreatures instance, EnumFacing side) {
        if (instance == null) return null;

        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("LastSelected", instance.getLastSelected());
        compound.setInteger("PartySizeLevel", instance.getPartySizeLevel());
        compound.setInteger("BoxSizeLevel", instance.getBoxSizeLevel());

        //for party creatures
        NBTTagList partyCreaturesList = new NBTTagList();
        if (!instance.getPartyNBT().isEmpty()) {
            for (NBTTagCompound partyNBT : instance.getPartyNBT()) partyCreaturesList.appendTag(partyNBT);
            compound.setTag("PartyCreatures", partyCreaturesList);
        }
        else compound.setTag("PartyCreatures", partyCreaturesList);

        //for box creatures
        NBTTagList boxCreaturesList = new NBTTagList();
        if (!instance.getBoxNBT().isEmpty()) {
            for (NBTTagCompound boxNBT : instance.getBoxNBT()) boxCreaturesList.appendTag(boxNBT);
            compound.setTag("BoxCreatures", boxCreaturesList);
        }
        else compound.setTag("BoxCreatures", boxCreaturesList);

        compound.setInteger("PartyLastOpenedTime", instance.getPartyLastOpenedTime());
        compound.setInteger("BoxLastOpenedTime", instance.getBoxLastOpenedTime());

        return compound;
    }

    @Override
    public void readNBT(Capability<IPlayerTamedCreatures> capability, IPlayerTamedCreatures instance, EnumFacing side, NBTBase nbt) {
        if (instance == null || nbt == null) return;

        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound)nbt;

            instance.setLastSelected(compound.getInteger("LastSelected"));
            instance.setPartySizeLevel(compound.getInteger("PartySizeLevel"));
            instance.setBoxSizeLevel(compound.getInteger("BoxSizeLevel"));

            //for party creatures
            if (compound.hasKey("PartyCreatures")) {
                NBTTagList partyCreaturesList = compound.getTagList("PartyCreatures", 10);
                if (!partyCreaturesList.isEmpty()) {
                    List<NBTTagCompound> finalPartyCreatures = new ArrayList<>();
                    for (int i = 0; i < partyCreaturesList.tagCount(); i++) {
                        finalPartyCreatures.add(partyCreaturesList.getCompoundTagAt(i));
                    }
                    instance.setPartyNBT(finalPartyCreatures);
                }
            }

            //for box creatures
            if (compound.hasKey("BoxCreatures")) {
                NBTTagList boxCreaturesList = compound.getTagList("BoxCreatures", 10);
                if (!boxCreaturesList.isEmpty()) {
                    List<NBTTagCompound> finalBoxCreatures = new ArrayList<>();
                    for (int i = 0; i < boxCreaturesList.tagCount(); i++) {
                        finalBoxCreatures.add(boxCreaturesList.getCompoundTagAt(i));
                    }
                    instance.setBoxNBT(finalBoxCreatures);
                }
            }

            instance.setPartyLastOpenedTime(compound.getInteger("PartyLastOpenedTime"));
            instance.setBoxLastOpenedTime(compound.getInteger("BoxLastOpenedTime"));
        }
    }
}
