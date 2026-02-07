package anightdazingzoroark.prift.client.newui.data;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.NewRiftPartyScreen;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.AbstractUIFactory;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.GuiManager;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerGuiFactory extends AbstractUIFactory<GuiData> {
    public static PlayerGuiFactory INSTANCE = new PlayerGuiFactory();

    protected PlayerGuiFactory() {
        super(RiftInitialize.MODID+":player");
    }

    @Override
    public IGuiHolder<GuiData> getGuiHolder(GuiData data) {
        return null;
    }

    @Override
    public void writeGuiData(GuiData guiData, PacketBuffer buffer) {
        buffer.writeInt(guiData.getPlayer().getEntityId());
    }

    @Override
    public @NotNull GuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        int playerId = buffer.readInt();
        EntityPlayer guiDataPlayer = (EntityPlayer) player.world.getEntityByID(playerId);
        return new GuiData(Objects.requireNonNull(guiDataPlayer));
    }

    public void open(EntityPlayer player) {
        Objects.requireNonNull(player);
        GuiManager.open(this, new GuiData(player), (EntityPlayerMP) player);
    }

    @Override
    public ModularPanel createPanel(GuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        return NewRiftPartyScreen.build(guiData, syncManager, settings);
    }

    @Override
    public ModularScreen createScreen(GuiData guiData, ModularPanel mainPanel) {
        return new ModularScreen(mainPanel);
    }
}
