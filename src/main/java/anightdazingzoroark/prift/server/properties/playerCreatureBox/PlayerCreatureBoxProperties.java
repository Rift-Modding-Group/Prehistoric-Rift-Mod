package anightdazingzoroark.prift.server.properties.playerCreatureBox;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.HashMapPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.IntPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.ObjectPropertyValue;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerCreatureBoxProperties extends AbstractEntityProperties<EntityPlayer> {
    public PlayerCreatureBoxProperties(@NotNull String key, @NotNull EntityPlayer entityHolder) {
        super(key, entityHolder);
    }

    @Override
    protected void registerDefaults(EntityPlayer entity) {
        this.register(new ObjectPropertyValue<CreatureBoxStorage>(
                "CreatureBoxStorage", new CreatureBoxStorage(), CreatureBoxStorage.class,
                CreatureBoxStorage::writeNBTList,
                nbtBase -> {
                        CreatureBoxStorage toReturn = new CreatureBoxStorage();
                        if (!(nbtBase instanceof NBTTagList nbtTagList)) return toReturn;
                        toReturn.parseNBTList(nbtTagList);
                        return toReturn;
                    }
                )
        );
        //pair represents box index then index
        //integer represents time left in ticks to revive
        this.register(new HashMapPropertyValue<ImmutablePair<Integer, Integer>, Integer>(
            "CreatureReviveTime",
                PlayerCreatureBoxHelper::getNBTListFromReviveInfo,
                nbtBase -> {
                    if (!(nbtBase instanceof NBTTagList nbtTagList)) return new HashMap<>();
                    return PlayerCreatureBoxHelper.parseReviveInfoFromNBTList(nbtTagList);
                }
        ));
        //is in ticks
        this.register(new IntPropertyValue("LastTimeOpened", 0), false);
    }

    //-----direct creature box contents editing and getting-----
    public CreatureBoxStorage getCreatureBoxStorage() {
        return this.get("CreatureBoxStorage");
    }

    //this is a bit different as it will also edit CreatureReviveTime
    public void setCreatureBoxStorage(CreatureBoxStorage creatureBoxStorage) {
        //save to creatureboxstorage first
        this.set("CreatureBoxStorage", creatureBoxStorage, false);

        //evaluate to see if there's dead creatures that aren't in revive time hashmap
        HashMap<ImmutablePair<Integer, Integer>, Integer> positionsToRevive = this.getCreatureReviveTime();

        //otherwise compare with already existing positions in positionsToRevive before adding
        for (int boxIndex = 0; boxIndex < CreatureBoxStorage.maxBoxAmnt; boxIndex++) {
            for (int index = 0; index < CreatureBoxStorage.maxBoxStorableCreatures; index++) {
                ImmutablePair<Integer, Integer> newPairToRevive = new ImmutablePair<>(boxIndex, index);

                CreatureNBT creatureNBT = creatureBoxStorage.getBoxContents(boxIndex).get(index);
                if (creatureNBT.nbtIsEmpty()) {
                    positionsToRevive.remove(newPairToRevive);
                    continue;
                }

                if (!this.positionOccupied(positionsToRevive, newPairToRevive) && creatureNBT.getCreatureHealth()[0] <= 0) {
                    positionsToRevive.put(newPairToRevive, GeneralConfig.creatureBoxReviveTime);
                }
                else if (this.positionOccupied(positionsToRevive, newPairToRevive) && creatureNBT.getCreatureHealth()[0] > 0) {
                    positionsToRevive.remove(newPairToRevive);
                }
            }
        }

        //now save to creature revive time property
        this.set("CreatureReviveTime", positionsToRevive, false);

        //finally sync
        this.syncToClientMultiple("CreatureBoxStorage", "CreatureReviveTime");
    }

    private boolean positionOccupied(
            HashMap<ImmutablePair<Integer, Integer>, Integer> positionsToRevive,
            ImmutablePair<Integer, Integer> posToFind
    ) {
        Set<ImmutablePair<Integer, Integer>> setPositionsToRevive = positionsToRevive.keySet();

        for (ImmutablePair<Integer, Integer> setPosition : setPositionsToRevive) {
            if (this.intPairEqual(setPosition, posToFind)) return true;
        }
        return false;
    }

    //small helper function
    private boolean intPairEqual(
            ImmutablePair<Integer, Integer> pairOne,
            ImmutablePair<Integer, Integer> pairTwo
    ) {
        return Objects.equals(pairOne.getLeft(), pairTwo.getLeft()) && Objects.equals(pairOne.getRight(), pairTwo.getRight());
    }

    //-----direct revival hash map editing and getting-----
    public HashMap<ImmutablePair<Integer, Integer>, Integer> getCreatureReviveTime() {
        return this.get("CreatureReviveTime");
    }

    public void setCreatureReviveTime(HashMap<ImmutablePair<Integer, Integer>, Integer> value) {
        this.set("CreatureReviveTime", value);
    }

    //-----direct last time opened getting and editing-----
    public int getLastTimeOpened() {
        return this.get("LastTimeOpened");
    }

    public void setLastTimeOpened(int value) {
        //set time change and last time opened
        //the delta is divided by 2 because for some reason it subtracts too much time otherwise
        int timeChange = Math.max(0, (value - this.getLastTimeOpened()) / 2);
        this.set("LastTimeOpened", value, false);

        //use above info to change revive time
        HashMap<ImmutablePair<Integer, Integer>, Integer> positionsToRevive = this.getCreatureReviveTime();
        for (Map.Entry<ImmutablePair<Integer, Integer>, Integer> posToRevive : positionsToRevive.entrySet()) {
            posToRevive.setValue(Math.max(posToRevive.getValue() - timeChange, 0));
        }
        this.set("CreatureReviveTime", positionsToRevive);

        this.syncToClientMultiple("LastTimeOpened", "CreatureReviveTime");
    }

    //-----indirect creature box contents editing and getting-----
    public void addCreatureToBox(RiftCreature creature) {
        CreatureBoxStorage creatureBoxStorage = this.getCreatureBoxStorage();
        creatureBoxStorage.addCreatureToBox(new CreatureNBT(creature));
        this.setCreatureBoxStorage(creatureBoxStorage);
    }

    public boolean canAddCreatureToBox() {
        CreatureBoxStorage creatureBoxStorage = this.getCreatureBoxStorage();
        for (int x = 0; x < CreatureBoxStorage.maxBoxAmnt; x++) {
            int validSpace = creatureBoxStorage.validSpaceInBox(x);
            if (validSpace >= 0) return true;
        }
        return false;
    }
}
