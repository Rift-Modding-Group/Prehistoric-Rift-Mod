package anightdazingzoroark.prift.server.creatureBox;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.RiftCreatureBoxUI;
import anightdazingzoroark.prift.server.block.RiftBlocks;
import anightdazingzoroark.prift.server.properties.PlayerCreatureBoxProperties;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class CreatureBoxGuiFactory extends AbstractUIFactory<CreatureBoxGuiData> implements IGuiHolder<CreatureBoxGuiData> {
    public static final CreatureBoxGuiFactory INSTANCE = new CreatureBoxGuiFactory();

    private CreatureBoxGuiFactory() {
        super(RiftInitialize.MODID + ":creature_box");
    }

    public void open(@NotNull EntityPlayer player, @NotNull BlockPos position, int box) {
        CreatureBoxGuiData data = new CreatureBoxGuiData(player, position, box);
        if (this.canInteractWith(player, data)) GuiManager.open(this, data, verifyServerSide(player));
    }

    @Override
    @NotNull
    public IGuiHolder<CreatureBoxGuiData> getGuiHolder(CreatureBoxGuiData data) {
        return this;
    }

    @Override
    @NotNull
    public ModularPanel buildUI(CreatureBoxGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return RiftCreatureBoxUI.show(data, syncManager, settings);
    }

    @Override
    @NotNull
    public ModularScreen createScreen(CreatureBoxGuiData data, ModularPanel mainPanel) {
        return new ModularScreen(RiftInitialize.MODID, mainPanel);
    }

    @Override
    public void writeGuiData(CreatureBoxGuiData data, PacketBuffer buffer) {
        CreatureBoxGuiData.write(buffer, data);
    }

    @Override
    @NotNull
    public CreatureBoxGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        CreatureBoxGuiData data = CreatureBoxGuiData.read(player, buffer);
        if (!this.canInteractWith(player, data)) throw new IllegalArgumentException("Creature box is out of reach!");
        return data;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player, CreatureBoxGuiData data) {
        return player.isEntityAlive() && data.getSquaredDistance(player) <= 64D
                && player.world.isBlockLoaded(data.getBlockPos())
                && player.world.getBlockState(data.getBlockPos()).getBlock() == RiftBlocks.CREATURE_BOX
                && player.world.getTileEntity(data.getBlockPos()) instanceof CreatureBoxTileEntity
                && PlayerCreatureBoxProperties.get(player) != null;
    }
}
