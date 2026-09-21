package anightdazingzoroark.prift.server.entity.creature;

import com.cleanroommc.modularui.factory.GuiData;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Data used to build a creature UI for either a deployed creature entity or a stored creature.
 */
public class RiftCreatureGuiData extends GuiData {
    @NotNull
    private final IRiftCreature creature;

    public RiftCreatureGuiData(@NotNull EntityPlayer player, @NotNull IRiftCreature creature) {
        super(player);
        this.creature = Objects.requireNonNull(creature);
    }

    @NotNull
    public IRiftCreature getCreature() {
        return this.creature;
    }
}
