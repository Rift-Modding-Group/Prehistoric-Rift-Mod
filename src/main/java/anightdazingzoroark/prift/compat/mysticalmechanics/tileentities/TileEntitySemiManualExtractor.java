package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.RiftUIIcons;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.SemiManualExtractorRecipe;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.DoubleValue;
import com.cleanroommc.modularui.value.ObjectValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.FluidDisplayWidget;
import com.cleanroommc.modularui.widgets.ProgressWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Loader;

public class TileEntitySemiManualExtractor extends TileEntitySemiManualBase implements IGuiHolder<PosGuiData> {
    private final int maxVolume = 4000;

    @Override
    public void registerInventories() {
        super.registerInventories();
        this.registerInventory("BucketInput", new RiftInventoryHandler(1, itemStack -> {
            return itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        }));
        this.registerInventory("BucketOutput", 1);
        this.registerInventorySiding("BucketOutput", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
    }

    @Override
    public void registerFluidTanks() {
        this.registerFluidTank("OutputTank", this.maxVolume);
    }

    @Override
    public void update() {
        super.update();
        if (this.world.isRemote) return;

        FluidTank outputTank = this.getOutputTank();

        //for creating fluid
        if (this.getTopTEntity() != null) {
            if (this.getTopTEntity().getPower() > 0) {
                if (this.getTopTEntity().getCurrentRecipe() == null) {
                    for (SemiManualExtractorRecipe recipe : RiftMMRecipes.smExtractorRecipes) {
                        if (recipe.matches(this.getTopTEntity().getPower(), this.getInputItem())) {
                            this.getTopTEntity().setCurrentRecipe(recipe);
                        }
                    }
                }
                else {
                    if (!this.getTopTEntity().getMustBeReset() && !this.canDoResetAnim()) {
                        SemiManualExtractorRecipe currentRecipe = (SemiManualExtractorRecipe) this.getTopTEntity().getCurrentRecipe();
                        boolean tankUsability = outputTank.getFluid() == null || (outputTank.getFluid().getFluid() == currentRecipe.output.getFluid() && outputTank.getFluid().amount < this.maxVolume);
                        if (tankUsability) {
                            if (this.getTopTEntity().getTimeHeld() < this.getTopTEntity().getMaxRecipeTime()) {
                                this.getTopTEntity().setTimeHeld(this.getTopTEntity().getTimeHeld() + 1);
                            }
                            else {
                                outputTank.fillInternal(currentRecipe.output, true);
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

        //for bucket filling
        RiftInventoryHandler bucketInput = this.getBucketInput();
        RiftInventoryHandler bucketOutput = this.getBucketOutput();
        if (!bucketInput.isEmpty() && outputTank.getFluid() != null) {
            ItemStack bucket = bucketInput.getStackInSlot(0);
            ItemStack bucketToFill = bucket.copy();
            bucketToFill.setCount(1);
            ItemStack filledBucket = RiftUtil.fillBucketWithFluid(bucketToFill, outputTank.getFluid().getFluid());
            if (bucketOutput.isEmpty() && outputTank.getFluid().amount >= Fluid.BUCKET_VOLUME) {
                bucketOutput.insertItem(filledBucket);
                bucket.setCount(bucket.getCount() - 1);
                outputTank.drain(1000, true);
            }
        }
    }

    public FluidTank getOutputTank() {
        return this.getFluidTank("OutputTank");
    }

    public RiftInventoryHandler getBucketInput() {
        return this.getInventory("BucketInput");
    }

    public RiftInventoryHandler getBucketOutput() {
        return this.getInventory("BucketOutput");
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, PanelSyncManager syncManager, UISettings uiSettings) {
        TileEntitySemiManualExtractor smExtractor = (TileEntitySemiManualExtractor) posGuiData.getTileEntity();
        if (smExtractor == null) return new ModularPanel("semiManualExtractorScreen");
        TileEntitySemiManualExtractorTop smExtractorTop = (TileEntitySemiManualExtractorTop) smExtractor.getTopTEntity();
        if (smExtractorTop == null) return new ModularPanel("semiManualExtractorScreen");

        RiftInventoryHandler inputInventory = smExtractor.getInputInventory();
        syncManager.registerSlotGroup("inputInventory", inputInventory.getSlots());
        SlotGroupWidget.Builder inputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(inputInventory, index).slotGroup("inputInventory")
                        )
                )
                .matrix("I");

        RiftInventoryHandler bucketInputInventory = smExtractor.getBucketInput();
        syncManager.registerSlotGroup("bucketInputInventory", bucketInputInventory.getSlots());
        SlotGroupWidget.Builder bucketInputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(bucketInputInventory, index).slotGroup("bucketInputInventory").filter(
                                        itemStack -> itemStack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
                                )
                        )
                )
                .matrix("I");

        RiftInventoryHandler bucketOutputInventory = smExtractor.getBucketOutput();
        syncManager.registerSlotGroup("bucketOutputInventory", bucketOutputInventory.getSlots());
        SlotGroupWidget.Builder bucketOutputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(bucketOutputInventory, index).slotGroup("bucketOutputInventory").accessibility(false, true)
                        )
                )
                .matrix("I");

        String playerName = posGuiData.getPlayer().getName();

        return new ModularPanel("semiManualExtractorScreen").size(176, 176)
                .padding(7, 7)
                //semi manual extractor title
                .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight().topRel(0)
                        .child(IKey.lang("tile.semi_manual_extractor.name").asWidget().left(0))
                )
                //processing
                .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight().topRel(0.2f)
                        .child(Flow.row().coverChildren().leftRel(0.5f)
                                .childPadding(15)
                                //input
                                .child(inputInvBuilder.build())
                                //progress bar
                                .child(new ParentWidget<>().size(20)
                                        .child(new ProgressWidget()
                                                .texture(GuiTextures.PROGRESS_ARROW, 20)
                                                .direction(ProgressWidget.Direction.RIGHT)
                                                .value(new DoubleValue.Dynamic(
                                                        () -> (double) smExtractorTop.getTimeHeld() / smExtractorTop.getMaxRecipeTime(),
                                                        null
                                                ))
                                        )
                                        .childIf(Loader.isModLoaded(RiftInitialize.JEI_MOD_ID),
                                                () -> new ButtonWidget<>().size(20)
                                                        .addTooltipElement(I18n.format("jei.show_recipes"))
                                                        .hoverBackground(IDrawable.EMPTY)
                                                        .background(IDrawable.EMPTY)
                                                        .onMousePressed(button -> {
                                                            RiftJEI.showRecipesForCategory(RiftJEI.smExtractorCat);
                                                            return true;
                                                        })
                                        )
                                )
                                //tank output and bucket management
                                .child(new ParentWidget<>().coverChildren()
                                        .child(Flow.row().childPadding(15).coverChildren()
                                                .child(new FluidDisplayWidget()
                                                        .size(40, 50)
                                                        .fluidTooltip((tooltip, fluidStack) -> {
                                                            String fluidName = fluidStack != null ? fluidStack.getLocalizedName() : "n/a";
                                                            String fluidAmnt = fluidStack != null ? String.valueOf(fluidStack.amount) : "0";
                                                            tooltip.addLine(IKey.lang("info.fluid.name", fluidName));
                                                            tooltip.addLine(IKey.lang("info.fluid.amount", fluidAmnt));
                                                        })
                                                        .displayAmount(false)
                                                        .capacity(smExtractor.maxVolume)
                                                        .value(new ObjectValue.Dynamic<>(
                                                                FluidStack.class,
                                                                () -> smExtractor.getOutputTank().getFluid(),
                                                                fluidStack -> {}
                                                        ))
                                                )
                                                .child(new ParentWidget<>().coverChildren()
                                                        .child(Flow.col().childPadding(2).coverChildren()
                                                                .child(bucketInputInvBuilder.build())
                                                                .child(RiftUIIcons.ARROW_DOWNWARD.asWidget())
                                                                .child(bucketOutputInvBuilder.build())
                                                        )
                                                )
                                        )
                                )
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
