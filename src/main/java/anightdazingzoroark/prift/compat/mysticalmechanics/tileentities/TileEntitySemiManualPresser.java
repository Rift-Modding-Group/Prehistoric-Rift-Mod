package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.UIPanelNames;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualPresserRecipe;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.DoublePropertyValue;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Loader;

public class TileEntitySemiManualPresser extends TileEntitySemiManualBase implements IGuiHolder<PosGuiData> {
    @Override
    public void registerInventories() {
        super.registerInventories();
        this.registerInventory("Output", 1);
        this.registerInventorySiding("Output", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
    }

    @Override
    public void update() {
        super.update();
        if (this.world.isRemote) return;
        if (this.getTopTEntity() != null) {
            if (this.getTopTEntity().getPower() > 0) {
                SemiManualPresserRecipe currentRecipe = (SemiManualPresserRecipe) this.getTopTEntity().getCurrentRecipe();

                if (currentRecipe == null) {
                    for (SemiManualPresserRecipe recipe : RiftMMRecipes.smPresserRecipes) {
                        if (recipe.matches(this.getTopTEntity().getPower(), this.getInputItem())) {
                            this.getTopTEntity().setCurrentRecipe(recipe);
                            break;
                        }
                    }
                }
                else {
                    if (!this.getTopTEntity().getMustBeReset() && !this.canDoResetAnim()) {
                        ItemStack outputItem = this.getOutputItem();
                        boolean outputUsability = (outputItem.isEmpty() || currentRecipe.output.apply(outputItem)) && outputItem.getCount() + currentRecipe.output.matchingStacks[0].getCount() <= outputItem.getMaxStackSize();
                        if (outputUsability) {
                            if (this.getTopTEntity().getTimeHeld() < this.getTopTEntity().getMaxRecipeTime()) {
                                this.getTopTEntity().setTimeHeld(this.getTopTEntity().getTimeHeld() + 1);
                            }
                            else {
                                RiftInventoryHandler outputInv = this.getOutputInventory();
                                ItemStack outputStack = currentRecipe.output.getMatchingStacks()[0].copy();
                                outputInv.insertItem(outputStack);
                                this.getInputItem().shrink(1);
                                this.getTopTEntity().setTimeHeld(0);
                                this.getTopTEntity().setMustBeReset(true);
                            }
                        }
                        if (!this.getTopTEntity().getCurrentRecipe().matches(this.getTopTEntity().getPower(), this.getInputItem())) {
                            this.getTopTEntity().setTimeHeld(0);
                            this.getTopTEntity().setCurrentRecipe(null);
                            this.getTopTEntity().setMustBeReset(true);
                        }
                    }
                }
            }
        }
    }

    public RiftInventoryHandler getOutputInventory() {
        return this.getInventory("Output");
    }

    public ItemStack getOutputItem() {
        return this.getOutputInventory().getStackInSlot(0);
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, PanelSyncManager syncManager, UISettings uiSettings) {
        TileEntitySemiManualPresser smPresser = (TileEntitySemiManualPresser) posGuiData.getTileEntity();
        if (smPresser == null) return new ModularPanel(UIPanelNames.SEMI_MANUAL_PRESSER_SCREEN);
        TileEntitySemiManualPresserTop smPresserTop = (TileEntitySemiManualPresserTop) smPresser.getTopTEntity();
        if (smPresserTop == null) return new ModularPanel(UIPanelNames.SEMI_MANUAL_PRESSER_SCREEN);

        RiftInventoryHandler inputInventory = smPresser.getInputInventory();
        syncManager.registerSlotGroup("inputInventory", inputInventory.getSlots());
        SlotGroupWidget.Builder inputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(inputInventory, index).slotGroup("inputInventory")
                        )
                )
                .matrix("I");

        RiftInventoryHandler outputInventory = smPresser.getOutputInventory();
        syncManager.registerSlotGroup("outputInventory", outputInventory.getSlots());
        SlotGroupWidget.Builder outputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(outputInventory, index).slotGroup("outputInventory").accessibility(false, true)
                        )
                )
                .matrix("I");

        String playerName = posGuiData.getPlayer().getName();

        return new ModularPanel(UIPanelNames.SEMI_MANUAL_PRESSER_SCREEN)
                .padding(7, 7)
                //semi manual presser title
                .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight().topRel(0)
                        .child(IKey.lang("tile.semi_manual_presser.name").asWidget().left(0))
                )
                //processing
                .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight().topRel(0.2f)
                        .child(Flow.row().coverChildren().leftRel(0.5f)
                                .childPadding(25)
                                //input
                                .child(inputInvBuilder.build())
                                //progress bar
                                .child(new ParentWidget<>().size(20)
                                        .child(new ProgressWidget()
                                                .texture(GuiTextures.PROGRESS_ARROW, 20)
                                                .direction(ProgressWidget.Direction.RIGHT)
                                                .value(new DoubleValue.Dynamic(
                                                        () -> (double) smPresserTop.getTimeHeld() / smPresserTop.getMaxRecipeTime(),
                                                        null
                                                ))
                                        )
                                        .childIf(Loader.isModLoaded(RiftInitialize.JEI_MOD_ID),
                                                () -> new ButtonWidget<>().size(20)
                                                        .addTooltipElement(I18n.format("jei.show_recipes"))
                                                        .hoverBackground(IDrawable.EMPTY)
                                                        .background(IDrawable.EMPTY)
                                                        .onMousePressed(button -> {
                                                            RiftJEI.showRecipesForCategory(RiftJEI.smPresserCat);
                                                            return true;
                                                        })
                                        )
                                )
                                //output
                                .child(outputInvBuilder.build())
                        )
                )
                //player inventory
                .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight().bottomRel(0)
                        .child(Flow.column().widthRel(1f).coverChildren()
                                .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                        .child(IKey.str(playerName).asWidget().left(0))
                                )
                                .child(SlotGroupWidget.playerInventory(false))
                        )
                );
    }
}
