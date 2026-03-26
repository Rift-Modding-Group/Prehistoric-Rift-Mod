package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.server.blocks.RiftFeedingTroughBlock;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.google.common.base.Predicate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.List;

public class RiftTileEntityFeedingTrough extends RiftTileEntity implements IAnimatable, ITickable, ISidedInventory, IGuiHolder<PosGuiData> {
    private final AnimationFactory factory = new AnimationFactory(this);
    private int feedCountdown = 0;

    @Override
    public void registerValues() {}

    @Override
    public void registerInventories() {
        this.registerInventory("Inventory", 9);
        this.registerInventorySiding("Inventory", SideInvInteraction.EXTRACT, EnumFacing.DOWN);
        this.registerInventorySiding("Inventory", SideInvInteraction.INSERT, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST);

        this.finalizeInventorySidingInfo();
    }

    public RiftInventoryHandler getInventory() {
        return this.getInventory("Inventory");
    }

    @Override
    public void update() {
        //get all nearby creatures
        if (this.world.isRemote) return;
        this.feedCountdown++;
        if (this.feedCountdown >= 60) {
            List<RiftCreature> creatures = this.world.getEntitiesWithinAABB(RiftCreature.class, this.getFeedingRange(), new Predicate<RiftCreature>() {
                @Override
                public boolean apply(@Nullable RiftCreature creature) {
                    return creature != null && creature.isTamed() && !creature.isSleeping();
                }
            });

            //start feeding them
            if (!creatures.isEmpty()) {
                for (RiftCreature creature : creatures) {
                    RiftInventoryHandler inventory = this.getInventory();

                    RiftInventoryHandler.ItemSearchResult itemSearchResult = inventory.findItem(
                            RiftInventoryHandler.ItemSearchDirection.LAST_TO_FIRST,
                            itemStack -> {
                                return creature.isFavoriteFood(itemStack) || creature.isEnergyRegenItem(itemStack);
                            }
                    );
                    if (itemSearchResult.successful()) {
                        if (creature.isFavoriteFood(itemSearchResult.foundStack())) {
                            creature.eatFoodForHealing(itemSearchResult.foundStack());
                        }
                        else if (creature.isEnergyRegenItem(itemSearchResult.foundStack())) {
                            creature.eatFoodForEnergyRegen(itemSearchResult.foundStack());
                        }
                    }
                }
            }
            this.feedCountdown = 0;
        }
    }

    private AxisAlignedBB getFeedingRange() {
        return new AxisAlignedBB(this.getPos().getX() - 16, this.getPos().getY() - 16, this.getPos().getZ() - 16, this.getPos().getX() + 16, this.getPos().getY() + 16, this.getPos().getZ() + 16);
    }

    public EnumFacing getFacing() {
        IBlockState state = this.getWorld().getBlockState(this.getPos());
        return state.getValue(RiftFeedingTroughBlock.FACING);
    }

    //-----inventory stuff starts here-----
    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return this.getSlotsAtSide(side);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return this.canInsertAtSlot(index, direction);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return this.canExtractAtSlot(index, direction);
    }

    @Override
    public int getSizeInventory() {
        return this.getTotalSidingInfoSize();
    }

    @Override
    public boolean isEmpty() {
        return this.hasEmptySidedInv();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return this.getStackAtSidedSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return this.decStackSizeAtSidedSlot(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return this.removeStackFromSidedSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        this.setStackAtSidedSlot(index, stack, this.getInventoryStackLimit());
        this.markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {}

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
    //-----inventory stuff ends here-----

    @Override
    public void registerControllers(AnimationData animationData) {}

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public ModularPanel buildUI(PosGuiData posGuiData, PanelSyncManager syncManager, UISettings uiSettings) {
        RiftTileEntityFeedingTrough feedingTrough = (RiftTileEntityFeedingTrough) posGuiData.getTileEntity();
        if (feedingTrough == null) return new ModularPanel(UIPanelNames.FEEDING_TROUGH_SCREEN);
        RiftInventoryHandler feedingTroughInventory = feedingTrough.getInventory();

        String playerName = posGuiData.getPlayer().getName();

        syncManager.registerSlotGroup("feedingTroughInventory", feedingTrough.getInventory().getSlots());
        SlotGroupWidget.Builder weaponInvBuilder = SlotGroupWidget.builder()
                .key('I', index -> new ItemSlot().slot(
                                new ModularSlot(feedingTroughInventory, index).slotGroup("feedingTroughInventory")
                        )
                )
                .matrix("IIIIIIIII");

        return new ModularPanel(UIPanelNames.FEEDING_TROUGH_SCREEN)
                .padding(7, 7).height(131)
                .child(Flow.column().childPadding(5).coverChildrenHeight()
                        //weapon inventory
                        .child(new ParentWidget<>().widthRel(1f).coverChildrenHeight()
                                .child(Flow.column().widthRel(1f).coverChildrenHeight()
                                        .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                                .child(IKey.lang("tile.feeding_trough.name").asWidget().left(0))
                                        )
                                        .child(new ParentWidget<>().width(162).coverChildrenHeight()
                                                .child(weaponInvBuilder.build().left(0))
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
