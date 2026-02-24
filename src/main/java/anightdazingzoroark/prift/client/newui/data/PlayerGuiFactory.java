package anightdazingzoroark.prift.client.newui.data;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.newui.NewRiftJournalScreen;
import anightdazingzoroark.prift.client.newui.PlayerUIHelper;
import anightdazingzoroark.prift.client.newui.RiftPartyScreen;
import anightdazingzoroark.prift.client.newui.UIPanelNames;
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
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerGuiFactory extends AbstractUIFactory<PlayerGuiData> {
    public static PlayerGuiFactory INSTANCE = new PlayerGuiFactory();
    private String screenName;

    protected PlayerGuiFactory() {
        super(RiftInitialize.MODID+":player");
    }

    public PlayerGuiFactory setScreen(@NotNull String screenName) {
        this.screenName = screenName;
        return this;
    }

    @Override
    public IGuiHolder<PlayerGuiData> getGuiHolder(PlayerGuiData data) {
        return null;
    }

    @Override
    public void writeGuiData(PlayerGuiData guiData, PacketBuffer buffer) {
        buffer.writeInt(guiData.getPlayer().getEntityId());
    }

    @Override
    public @NotNull PlayerGuiData readGuiData(EntityPlayer player, PacketBuffer buffer) {
        int playerId = buffer.readInt();

        EntityPlayer guiDataPlayer = (EntityPlayer) player.world.getEntityByID(playerId);
        return new PlayerGuiData(Objects.requireNonNull(guiDataPlayer));
    }

    public void open(EntityPlayer player) {
        Objects.requireNonNull(player);
        GuiManager.open(this, new PlayerGuiData(player), (EntityPlayerMP) player);
    }

    @Override
    public ModularPanel createPanel(PlayerGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        if (this.screenName.equals(UIPanelNames.PARTY_SCREEN)) return RiftPartyScreen.build(guiData, syncManager, settings);
        else if (this.screenName.equals(UIPanelNames.JOURNAL_SCREEN)) return NewRiftJournalScreen.build(guiData, syncManager, settings);
        else return new ModularPanel(UIPanelNames.EMPTY_SCREEN);
    }

    @Override
    public ModularScreen createScreen(PlayerGuiData guiData, ModularPanel mainPanel) {
        return new ModularScreen(RiftInitialize.MODID, mainPanel);
    }
}
