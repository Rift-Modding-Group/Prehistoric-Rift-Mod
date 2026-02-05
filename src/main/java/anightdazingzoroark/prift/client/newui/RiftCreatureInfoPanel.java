package anightdazingzoroark.prift.client.newui;

import anightdazingzoroark.prift.client.newui.custom.EntityWidget;
import anightdazingzoroark.prift.client.newui.data.CreatureGuiData;
import anightdazingzoroark.prift.client.ui.SelectedCreatureInfo;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.CreatureNBT;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.Rectangle;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Unit;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Row;
import net.minecraft.entity.player.EntityPlayer;

import java.util.Objects;
import java.util.function.DoubleSupplier;

//this one class will be used to display information about a creature
//in the form of a pop-up, and will be shared amongst various uis
public class RiftCreatureInfoPanel {
    public static final int[] size = {211, 122};

    public static ParentWidget<?> build(CreatureGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new ParentWidget<>().padding(7, 7).coverChildren()
                .child(new Row().coverChildren().childPadding(5)
                        //left side is the entity and the name
                        .child(new Column().debugName("leftSide")
                                .childPadding(5).coverChildren()
                                .child(new ParentWidget<>().size(96, 64)
                                        .child(new Rectangle().setColor(0xFF000000).setCornerRadius(5)
                                                .asWidget().size(96, 64))
                                        .child(new Rectangle().setColor(0xFF808080).setCornerRadius(5)
                                                .asWidget().size(94, 62).align(Alignment.Center))
                                        .child(new EntityWidget<>(duplicateCreatureForRender(data), 10f)
                                                .size(92, 60).yawRotationAngle(135f).align(Alignment.Center))
                                )
                                .child(IKey.str(data.getName(false)).scale(0.75f).asWidget())
                                .child(IKey.lang("tametrait.level", data.getLevel()).scale(0.75f).asWidget())
                        )
                        //separator line
                        .child(new Rectangle().setColor(0xFF000000).asWidget().size(1, 108).align(Alignment.Center))
                        //right side is info
                        .child(new Column().debugName("rightSide")
                                .childPadding(5).coverChildren()
                                //species name
                                .child(new ParentWidget<>().width(96).coverChildrenHeight()
                                        .child(IKey.lang("tametrait.species", Objects.requireNonNull(data.getCreatureType()).getTranslatedName())
                                                .scale(0.5f).asWidget().alignment(Alignment.CenterLeft)
                                        )
                                )
                                //health
                                .child(new Column().childPadding(2).coverChildren()
                                        .child(new ParentWidget<>().width(96).coverChildrenHeight()
                                                .child(IKey.lang("tametrait.health", (int) data.getHealth()[0], (int) data.getHealth()[1])
                                                        .scale(0.5f).asWidget().alignment(Alignment.CenterLeft)
                                                )
                                        )
                                        .child(new ParentWidget<>().size(96, 3)
                                                .child(new Rectangle().setColor(0xFF000000).asWidget().size(96, 3))
                                                .child(new Rectangle().setColor(0xFF808080).asWidget().size(94, 1)
                                                        .align(Alignment.Center)
                                                )
                                                .child(new Rectangle().setColor(0xFFFF0000).asWidget().height(1)
                                                        .width(() -> (94 * data.getHealth()[0] / data.getHealth()[1]), Unit.Measure.PIXEL)
                                                        .right(() -> (94 - (94 * data.getHealth()[0] / data.getHealth()[1]) + 1), Unit.Measure.PIXEL)
                                                        .bottom(1)
                                                )
                                        )
                                )
                                //energy
                                .child(new Column().childPadding(2).coverChildren()
                                        .child(new ParentWidget<>().width(96).coverChildrenHeight()
                                                .child(IKey.lang("tametrait.energy", data.getEnergy()[0], data.getEnergy()[1])
                                                        .scale(0.5f).asWidget().alignment(Alignment.CenterLeft)
                                                )
                                        )
                                        .child(new ParentWidget<>().size(96, 3)
                                                .child(new Rectangle().setColor(0xFF000000).asWidget().size(96, 3))
                                                .child(new Rectangle().setColor(0xFF808080).asWidget().size(94, 1)
                                                        .align(Alignment.Center)
                                                )
                                                .child(new Rectangle().setColor(0xFFFFFF00).asWidget().height(1)
                                                        .width(() -> ((double) (94 * data.getEnergy()[0]) / data.getEnergy()[1]), Unit.Measure.PIXEL)
                                                        .right(() -> (94 - ((double) (94 * data.getEnergy()[0]) / data.getEnergy()[1]) + 1), Unit.Measure.PIXEL)
                                                        .bottom(1)
                                                )
                                        )
                                )
                                //experience
                                .child(new Column().childPadding(2).coverChildren()
                                        .child(new ParentWidget<>().width(96).coverChildrenHeight()
                                                .child(IKey.lang("tametrait.xp", data.getXP()[0], data.getXP()[1])
                                                        .scale(0.5f).asWidget().alignment(Alignment.CenterLeft)
                                                )
                                        )
                                        .child(new ParentWidget<>().size(96, 3)
                                                .child(new Rectangle().setColor(0xFF000000).asWidget().size(96, 3))
                                                .child(new Rectangle().setColor(0xFF808080).asWidget().size(94, 1)
                                                        .align(Alignment.Center)
                                                )
                                                .child(new Rectangle().setColor(0xFF98D06B).asWidget().height(1)
                                                        .width(() -> ((double) (94 * data.getXP()[0]) / data.getXP()[1]), Unit.Measure.PIXEL)
                                                        .right(() -> (94 - ((double) (94 * data.getXP()[0]) / data.getXP()[1]) + 1), Unit.Measure.PIXEL)
                                                        .bottom(1)
                                                )
                                        )
                                )
                                //age
                                .child(new ParentWidget<>().width(96).coverChildrenHeight()
                                        .child(IKey.lang("tametrait.age", data.getAgeInDays())
                                                .scale(0.5f).asWidget().alignment(Alignment.CenterLeft)
                                        )
                                )
                                //acquisition info
                                .child(new ParentWidget<>().width(96).coverChildrenHeight()
                                        .child(IKey.str(data.getAcquisitionInfoString())
                                                .scale(0.5f).asWidget().alignment(Alignment.CenterLeft)
                                        )
                                )
                        )
                );
    }

    private static RiftCreature duplicateCreatureForRender(CreatureGuiData data) {
        if (data == null) return null;
        CreatureNBT creatureNBT = data.getCreatureNBT();
        return creatureNBT.recreateCreatureAsNBT(data.getWorld());
    }
}
