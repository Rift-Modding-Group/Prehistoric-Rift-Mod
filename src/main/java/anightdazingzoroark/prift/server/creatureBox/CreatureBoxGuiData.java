package anightdazingzoroark.prift.server.creatureBox;

import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties.StoragePosition;
import com.cleanroommc.modularui.factory.PosGuiData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Selection and pending confirmation belong to one open container, never to the block.
 * */
public class CreatureBoxGuiData extends PosGuiData {
    public int selectedBox;
    public boolean swapping;
    public int selectedStorage = -2;
    public int selectedSlot = -1;
    @Nullable
    public UUID selectedCreature;
    @Nullable
    public StoragePosition pendingSource;
    @Nullable
    public StoragePosition pendingDestination;
    @Nullable
    public UUID pendingCreature;
    @Nullable
    public UUID pendingDestinationCreature;
    @NotNull
    public String renameDraft = "";

    public CreatureBoxGuiData(@NotNull EntityPlayer player, @NotNull BlockPos position, int box) {
        super(player, position);
        this.selectedBox = Math.clamp(box, 0, PlayerCreatureBoxProperties.BOX_COUNT - 1);
    }
}