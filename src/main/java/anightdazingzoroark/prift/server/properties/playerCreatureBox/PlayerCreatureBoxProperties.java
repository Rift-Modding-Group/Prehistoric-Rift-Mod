package anightdazingzoroark.prift.server.properties.playerCreatureBox;

import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.helper.CreatureNBT;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.ObjectPropertyValue;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import org.jetbrains.annotations.NotNull;

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
    }

    //-----direct creature box contents editing and getting-----
    public CreatureBoxStorage getCreatureBoxStorage() {
        return this.get("CreatureBoxStorage");
    }

    private void setCreatureBoxStorage(CreatureBoxStorage creatureBoxStorage) {
        this.set("CreatureBoxStorage", creatureBoxStorage);
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
