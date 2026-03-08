package anightdazingzoroark.prift.server.properties.playerCreatureBox;

import anightdazingzoroark.prift.propertySystem.propertyStorage.AbstractEntityProperties;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.properties.propertyValues.CreatureBoxStoragePropertyValue;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerCreatureBoxProperties extends AbstractEntityProperties<EntityPlayer> {
    public PlayerCreatureBoxProperties(@NotNull String key, @NotNull EntityPlayer entityHolder) {
        super(key, entityHolder);
    }

    @Override
    protected void registerDefaults(EntityPlayer entity) {
        this.put(new CreatureBoxStoragePropertyValue("CreatureBoxStorage", new CreatureBoxStorage()));
    }

    //-----direct creature box contents editing and getting-----
    public CreatureBoxStorage getCreatureBoxStorage() {
        return (CreatureBoxStorage) this.getProperty("CreatureBoxStorage").getValue();
    }

    private void setCreatureBoxStorage(CreatureBoxStorage creatureBoxStorage) {
        this.put(new CreatureBoxStoragePropertyValue("CreatureBoxStorage", creatureBoxStorage));
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
