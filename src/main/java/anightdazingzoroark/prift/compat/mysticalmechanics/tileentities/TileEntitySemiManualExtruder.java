package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.UIPanelNames;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtruderRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualPresserRecipe;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.DoublePropertyValue;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.riftlib.core.AnimatableValue;
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
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Arrays;
import java.util.List;

public class TileEntitySemiManualExtruder extends TileEntitySemiManualBase implements IGuiHolder<PosGuiData> {
    @Override
    public void registerValues() {
        super.registerValues();
        this.registerValue(new DoublePropertyValue("Rotation", 0D));
    }

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
                if (this.getTopTEntity().getCurrentRecipe() == null) {
                    for (SemiManualExtruderRecipe recipe : RiftMMRecipes.smExtruderRecipes) {
                        if (recipe.matches(this.getTopTEntity().getPower(), this.getInputItem())) {
                            this.getTopTEntity().setCurrentRecipe(recipe);
                        }
                    }
                }
                else {
                    if (!this.getTopTEntity().getMustBeReset() && !this.canDoResetAnim()) {
                        ItemStack outputItem = this.getOutputItem();
                        SemiManualExtruderRecipe currentRecipe = (SemiManualExtruderRecipe) this.getTopTEntity().getCurrentRecipe();
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

                //rotation update
                if (!this.getTopTEntity().getMustBeReset()) {
                    double newRotation = this.getRotation() + this.getTopTEntity().getPower();
                    if (newRotation >= 360f) newRotation = newRotation - 360f;
                    this.setRotation(newRotation);
                }
            }
        }
    }

    public RiftInventoryHandler getOutputInventory() {
        return this.getInventory("Output");
    }

    public double getRotation() {
        return this.getValue("Rotation");
    }

    public void setRotation(double value) {
        this.setValue("Rotation", value);
    }

    public ItemStack getOutputItem() {
        return this.getOutputInventory().getStackInSlot(0);
    }

    @Override
    public List<AnimatableValue> createAnimationVariables() {
        return List.of(new AnimatableValue("spinAxle", 0D));
    }

    @Override
    public List<AnimatableValue> tickAnimationVariables() {
        return List.of(new AnimatableValue("spinAxle", this.getRotation()));
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, PanelSyncManager syncManager, UISettings uiSettings) {
        TileEntitySemiManualExtruder smExtruder = (TileEntitySemiManualExtruder) posGuiData.getTileEntity();
        if (smExtruder == null) return new ModularPanel("semiManualExtruderScreen");
        TileEntitySemiManualExtruderTop smExtruderTop = (TileEntitySemiManualExtruderTop) smExtruder.getTopTEntity();
        if (smExtruderTop == null) return new ModularPanel("semiManualExtruderScreen");

        RiftInventoryHandler inputInventory = smExtruder.getInputInventory();
        syncManager.registerSlotGroup("inputInventory", inputInventory.getSlots());
        SlotGroupWidget.Builder inputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(inputInventory, index).slotGroup("inputInventory")
                        )
                )
                .matrix("I");

        RiftInventoryHandler outputInventory = smExtruder.getOutputInventory();
        syncManager.registerSlotGroup("outputInventory", outputInventory.getSlots());
        SlotGroupWidget.Builder outputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(outputInventory, index).slotGroup("outputInventory").accessibility(false, true)
                        )
                )
                .matrix("I");

        String playerName = posGuiData.getPlayer().getName();

        return new ModularPanel("semiManualExtruderScreen")
                .padding(7, 7)
                //semi manual presser title
                .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight().topRel(0)
                        .child(IKey.lang("tile.semi_manual_extruder.name").asWidget().left(0))
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
                                                        () -> (double) smExtruderTop.getTimeHeld() / smExtruderTop.getMaxRecipeTime(),
                                                        null
                                                ))
                                        )
                                        .childIf(Loader.isModLoaded(RiftInitialize.JEI_MOD_ID),
                                                () -> new ButtonWidget<>().size(20)
                                                        .addTooltipElement(I18n.format("jei.show_recipes"))
                                                        .hoverBackground(IDrawable.EMPTY)
                                                        .background(IDrawable.EMPTY)
                                                        .onMousePressed(button -> {
                                                            RiftJEI.showRecipesForCategory(RiftJEI.smExtruderCat);
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
