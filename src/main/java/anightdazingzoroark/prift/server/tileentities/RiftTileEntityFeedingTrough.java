package anightdazingzoroark.prift.server.tileentities;

import anightdazingzoroark.prift.client.newui.UIPanelNames;
import anightdazingzoroark.prift.propertySystem.propertyStorage.propertyValue.ObjectPropertyValue;
import anightdazingzoroark.prift.server.blocks.RiftFeedingTroughBlock;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.inventory.RiftInventoryHandler;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftUpdateTileEntityProperty;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.items.ItemStackHandler;
import anightdazingzoroark.riftlib.core.IAnimatable;
import anightdazingzoroark.riftlib.core.manager.AnimationData;
import anightdazingzoroark.riftlib.core.manager.AnimationFactory;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class RiftTileEntityFeedingTrough extends RiftTileEntity implements IAnimatable, ITickable, IGuiHolder<PosGuiData> {
    private final AnimationFactory factory = new AnimationFactory(this);
    public static final int INV_SIZE = 9;
    private int feedCountdown = 0;

    @Override
    public void registerValues() {
        this.register(new ObjectPropertyValue<RiftInventoryHandler>(
                "Inventory", new RiftInventoryHandler(INV_SIZE), RiftInventoryHandler.class,
                ItemStackHandler::serializeNBT,
                nbtBase -> {
                    RiftInventoryHandler toReturn = new RiftInventoryHandler(INV_SIZE);
                    if (!(nbtBase instanceof NBTTagCompound nbtTagCompound)) return toReturn;
                    toReturn.deserializeNBT(nbtTagCompound);
                    return toReturn;
                }
        ));
    }

    public RiftInventoryHandler getInventory() {
        return this.get("Inventory");
    }

    public void setInventory(RiftInventoryHandler inventory) {
        this.set("Inventory", inventory);
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
                    return creature.isTamed() && !creature.isSleeping();
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

                        this.setInventory(inventory);
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
                                new ModularSlot(feedingTroughInventory, index) {
                                    @Override
                                    public void onSlotChanged() {
                                        this.updateFeedingTrough();
                                    }

                                    @Override
                                    public void onSlotChangedReal(ItemStack itemStack, boolean onlyChangedAmount, boolean client, boolean init) {
                                        super.onSlotChangedReal(itemStack, onlyChangedAmount, client, init);
                                        this.updateFeedingTrough();
                                    }

                                    @Override
                                    public void onCraftShiftClick(EntityPlayer player, ItemStack stack) {
                                        this.updateFeedingTrough();
                                    }

                                    @Override
                                    public void putStack(@NotNull ItemStack stack) {
                                        super.putStack(stack);
                                        this.updateFeedingTrough();
                                    }

                                    private void updateFeedingTrough() {
                                        RiftInventoryHandler feedingTroughInventory = (RiftInventoryHandler) this.getItemHandler();
                                        RiftMessages.WRAPPER.sendToServer(new RiftUpdateTileEntityProperty(
                                                posGuiData.getBlockPos(), "Inventory", feedingTroughInventory.serializeNBT()
                                        ));
                                    }
                                }.slotGroup("feedingTroughInventory")
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
