package anightdazingzoroark.prift.compat.mysticalmechanics.tileentities;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.client.ui.RiftUIIcons;
import anightdazingzoroark.prift.client.ui.UIPanelNames;
import anightdazingzoroark.prift.compat.jei.RiftJEI;
import anightdazingzoroark.prift.helper.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.ConsumerMechCapability;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.MillstoneRecipe;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.DoublePropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.IntegerPropertyValue;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.StringPropertyValue;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityContainer;
import anightdazingzoroark.riftlib.core.manager.AnimationDataTileEntity;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IDrawable;
import com.cleanroommc.modularui.api.drawable.IKey;
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
import mysticalmechanics.api.IMechCapability;
import mysticalmechanics.api.MysticalMechanicsAPI;
import mysticalmechanics.util.Misc;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Loader;
import anightdazingzoroark.riftlib.core.IAnimatable;

import javax.annotation.Nullable;
import java.util.List;

public class TileEntityMillstone extends RiftTileEntityContainer implements IAnimatable<AnimationDataTileEntity>, ITickable, IGuiHolder<PosGuiData> {
    private final AnimationDataTileEntity animationData = new AnimationDataTileEntity(this);
    private final IMechCapability mechPower;

    public TileEntityMillstone() {
        super();
        this.mechPower = new ConsumerMechCapability() {
            @Override
            public void onPowerChange() {
                TileEntityMillstone.this.markDirty();
            }
        };
    }

    @Override
    public void registerValues() {
        this.registerValue(new IntegerPropertyValue("TimeHeld", 0));
        this.registerValue(new DoublePropertyValue("Completion", 0D));
        this.registerValue(new StringPropertyValue("CurrentRecipe", ""));
    }

    @Override
    public void registerInventories() {
        this.registerInventory("Input", 3);
        this.registerInventory("Output", 9);

        this.registerInventorySiding("Input", SideInvInteraction.INSERT, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST);
        this.registerInventorySiding("Output", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
        this.finalizeInventorySidingInfo();
    }

    @Override
    public void registerFluidTanks() {}

    @Override
    public void update() {
        if (this.world.isRemote) {
            //get nearby players that will hear the sounds
            AxisAlignedBB hearRange = new AxisAlignedBB(this.getPos().getX() - 8, this.getPos().getY() - 8, this.getPos().getZ() - 8, this.getPos().getX() + 8, this.getPos().getY() + 8, this.getPos().getZ() + 8);
            List<EntityPlayer> playerList = this.world.getEntitiesWithinAABB(EntityPlayer.class, hearRange, null);
            if (this.getPower() > 0 && this.world.rand.nextInt(40) < 2) for (EntityPlayer player : playerList) this.world.playSound(player, this.pos, SoundEvents.ENTITY_MINECART_RIDING, SoundCategory.BLOCKS, 0.75F, this.world.rand.nextFloat() * 0.4F + 0.8F);
        }
        else {
            MillstoneRecipe currentRecipe = this.getCurrentRecipe();
            if (this.getPower() > 0) {
                if (currentRecipe == null) {
                    for (MillstoneRecipe recipe : RiftMMRecipes.millstoneRecipes) {
                        if (recipe.matches(this.getPower(), this.getInputItem())) {
                            this.setCurrentRecipe(recipe);
                        }
                    }
                }
                else {
                    RiftInventoryHandler outputInv = this.getOutputInventory();
                    if (outputInv.canInsertItem(currentRecipe.output.matchingStacks[0])) {
                        this.setTimeHeld(this.getTimeHeld() + 1);
                        if (this.getMaxRecipeTime() != 69420666) this.setCompletionPercentage((double)this.getTimeHeld() / (double)this.getMaxRecipeTime());
                        if (this.getTimeHeld() >= this.getMaxRecipeTime()) {
                            outputInv.insertItem(currentRecipe.output.matchingStacks[0]);
                            this.getInputItem().shrink(1);
                            this.setTimeHeld(0);
                        }
                    }
                    if (!currentRecipe.input.apply(this.getInputItem())) {
                        this.setTimeHeld(0);
                        this.setCompletionPercentage(0);
                        this.setCurrentRecipe(null);
                    }
                }
            }
            else {
                if (currentRecipe != null && !currentRecipe.input.apply(this.getInputItem())) {
                    this.setTimeHeld(0);
                    this.setCompletionPercentage(0);
                    this.setCurrentRecipe(null);
                }
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && facing == EnumFacing.UP) return true;
        return super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == MysticalMechanicsAPI.MECH_CAPABILITY && facing == EnumFacing.UP) return (T) this.mechPower;
        return super.getCapability(capability, facing);
    }

    public RiftInventoryHandler getInputInventory() {
        return this.getInventory("Input");
    }

    public RiftInventoryHandler getOutputInventory() {
        return this.getInventory("Output");
    }

    public MillstoneRecipe getCurrentRecipe() {
        return RiftMMRecipes.getMillstoneRecipe(this.getCurrentRecipeId());
    }

    public String getCurrentRecipeId() {
        return this.getValue("CurrentRecipe");
    }

    public void setCurrentRecipe(MillstoneRecipe value) {
        this.setValue("CurrentRecipe", value != null ? value.getId() : "");
    }

    public int getMaxRecipeTime() {
        //this estimates max time based on power input requires
        //at min power required its the default 10 seconds, but the higher the power the lower
        //the max time is until it reaches 3 seconds, which is 8x the min power
        //note that output is in ticks
        MillstoneRecipe currentRecipe = this.getCurrentRecipe();
        if (currentRecipe != null) {
            double minPower = currentRecipe.getMinPower();
            if (minPower <= this.getPower()) {
                double result = RiftUtil.slopeResult(
                        this.getPower(), true,
                        minPower, minPower * 8,
                        30, 5
                ) * 20;
                return (int) result;
            }
        }
        return 69420666;
    }

    public double getPower() {
        return this.mechPower.getPower(null);
    }

    public int getTimeHeld() {
        return this.getValue("TimeHeld");
    }

    public void setTimeHeld(int value) {
        this.setValue("TimeHeld", value);
    }

    public double getCompletionPercentage() {
        return this.getValue("Completion");
    }

    public void setCompletionPercentage(double value) {
        this.setValue("Completion", value);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        Misc.syncTE(this, false);
    }

    //inventory stuff starts here
    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index == 0 || index == 1 || index == 2;
    }

    public ItemStack getInputItem() {
        RiftInventoryHandler inputInventory = this.getInputInventory();
        for (ItemStack itemStack : inputInventory.getItemStackList()) {
            if (!itemStack.isEmpty()) return itemStack;
        }
        return ItemStack.EMPTY;
    }
    //inventory stuff ends here

    @Override
    public void registerControllers(AnimationDataTileEntity animationData) {}

    @Override
    public AnimationDataTileEntity getAnimationData() {
        return this.animationData;
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, PanelSyncManager syncManager, UISettings uiSettings) {
        TileEntityMillstone millstone = (TileEntityMillstone) posGuiData.getTileEntity();
        if (millstone == null) return new ModularPanel("millstoneScreen");
        RiftInventoryHandler inputInventory = millstone.getInputInventory();
        RiftInventoryHandler outputInventory = millstone.getOutputInventory();

        String playerName = posGuiData.getPlayer().getName();

        syncManager.registerSlotGroup("inputInventory", inputInventory.getSlots());
        SlotGroupWidget.Builder inputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(inputInventory, index).slotGroup("inputInventory")
                        )
                )
                .matrix("III");
        syncManager.registerSlotGroup("outputInventory", outputInventory.getSlots());
        SlotGroupWidget.Builder outputInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(outputInventory, index).slotGroup("outputInventory").accessibility(false, true)
                        )
                )
                .matrix("IIIIIIIII");

        return new ModularPanel("millstoneScreen")
                .padding(7, 7).height(177)
                .child(Flow.column().childPadding(5).coverChildrenHeight()
                        //millstone inventory
                        .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight()
                                .child(Flow.column().widthRel(1f).coverChildrenHeight()
                                        .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                                .child(IKey.lang("tile.millstone.name").asWidget().left(0))
                                        )
                                        .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                                .child(Flow.column().childPadding(5).widthRel(1f).coverChildrenHeight()
                                                        //inputs
                                                        .child(inputInvBuilder.build())
                                                        //progress bar
                                                        .child(new ParentWidget<>().size(20)
                                                                .child(new ProgressWidget()
                                                                        .texture(RiftUIIcons.PROGRESS_BAR_DOWNWARD, 20)
                                                                        .direction(ProgressWidget.Direction.DOWN)
                                                                        .value(new DoubleValue.Dynamic(
                                                                                millstone::getCompletionPercentage,
                                                                                null
                                                                        ))
                                                                )
                                                                .childIf(Loader.isModLoaded(RiftInitialize.JEI_MOD_ID),
                                                                        () -> new ButtonWidget<>().size(20)
                                                                        .addTooltipElement(I18n.format("jei.show_recipes"))
                                                                        .hoverBackground(IDrawable.EMPTY)
                                                                        .background(IDrawable.EMPTY)
                                                                        .onMousePressed(button -> {
                                                                            RiftJEI.showRecipesForCategory(RiftJEI.millstoneCat);
                                                                            return true;
                                                                        })
                                                                )
                                                        )
                                                        //outputs
                                                        .child(outputInvBuilder.build())
                                                )
                                        )
                                )
                        )
                        //player inventory
                        .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight()
                                .child(Flow.column().widthRel(1f).coverChildren()
                                        .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                                .child(IKey.str(playerName).asWidget().left(0))
                                        )
                                        .child(SlotGroupWidget.playerInventory(false))
                                )
                        )
                );
    }
}
