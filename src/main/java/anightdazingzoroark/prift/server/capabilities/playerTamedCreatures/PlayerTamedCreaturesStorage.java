package anightdazingzoroark.prift.server.capabilities.playerTamedCreatures;

import anightdazingzoroark.prift.helper.FixedSizeList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

public class PlayerTamedCreaturesStorage implements Capability.IStorage<IPlayerTamedCreatures> {
    @Nullable
    @Override
    public NBTBase writeNBT(Capability<IPlayerTamedCreatures> capability, IPlayerTamedCreatures instance, EnumFacing side) {
        if (instance == null) return null;

        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("LastSelected", instance.getLastSelected());
        compound.setInteger("BoxSizeLevel", instance.getBoxSizeLevel());

        //for party creatures
        NBTTagList partyCreaturesList = new NBTTagList();
        if (!instance.getPartyNBT().isEmpty()) {
            for (NBTTagCompound partyNBT : instance.getPartyNBT().getList()) partyCreaturesList.appendTag(partyNBT);
            compound.setTag("PartyCreatures", partyCreaturesList);
        }
        else compound.setTag("PartyCreatures", partyCreaturesList);

        //for box creatures
        compound.setTag("CreatureBoxStorage", instance.getBoxNBT().writeNBTList());

        compound.setInteger("PartyLastOpenedTime", instance.getPartyLastOpenedTime());
        compound.setInteger("BoxLastOpenedTime", instance.getBoxLastOpenedTime());

        compound.setInteger("SelectedPosInOverlay", instance.getSelectedPosInOverlay());

        compound.setInteger("LastOpenedBox", instance.getLastOpenedBox());

        return compound;
    }

    @Override
    public void readNBT(Capability<IPlayerTamedCreatures> capability, IPlayerTamedCreatures instance, EnumFacing side, NBTBase nbt) {
        if (instance == null || nbt == null) return;

        if (nbt instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound)nbt;

            instance.setLastSelected(compound.getInteger("LastSelected"));
            instance.setBoxSizeLevel(compound.getInteger("BoxSizeLevel"));

            //for party creatures
            if (compound.hasKey("PartyCreatures")) {
                NBTTagList partyCreaturesList = compound.getTagList("PartyCreatures", 10);
                if (!partyCreaturesList.isEmpty()) {
                    FixedSizeList<NBTTagCompound> finalPartyCreatures = new FixedSizeList<>(NewPlayerTamedCreaturesHelper.maxPartySize, new NBTTagCompound());
                    for (int i = 0; i < partyCreaturesList.tagCount(); i++) {
                        finalPartyCreatures.set(i, partyCreaturesList.getCompoundTagAt(i));
                    }
                    instance.setPartyNBT(finalPartyCreatures);
                }
            }

            //for box creatures
            if (compound.hasKey("CreatureBoxStorage")) {
                instance.setBoxNBT(new CreatureBoxStorage(compound.getTagList("CreatureBoxStorage", 10)));
            }

            instance.setPartyLastOpenedTime(compound.getInteger("PartyLastOpenedTime"));
            instance.setBoxLastOpenedTime(compound.getInteger("BoxLastOpenedTime"));

            instance.setSelectedPosInOverlay(compound.getInteger("SelectedPosInOverlay"));

            instance.setLastOpenedBox(compound.getInteger("LastOpenedBox"));
        }
    }
}
