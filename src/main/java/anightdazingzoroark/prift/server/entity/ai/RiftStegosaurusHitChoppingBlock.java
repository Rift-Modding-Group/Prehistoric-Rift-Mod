package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.mixin.RiftMixinPyrotechTileEntityChoppingBlock;
import anightdazingzoroark.prift.server.entity.creature.Stegosaurus;
import com.codetaylor.mc.athenaeum.util.ArrayHelper;
import com.codetaylor.mc.athenaeum.util.BlockHelper;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.block.BlockRock;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasicConfig;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.ChoppingBlockRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.TileChoppingBlock;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiftStegosaurusHitChoppingBlock extends EntityAIBase {
    private final Stegosaurus stegosaurus;
    private final int animLength;
    private final int animHitTime;
    private int animTime;
    private boolean destroyedFlag;
    private BlockPos workstationPos;

    public RiftStegosaurusHitChoppingBlock(Stegosaurus stegosaurus) {
        this.stegosaurus = stegosaurus;
        this.animLength = (int)(0.96D * 20D);
        this.animHitTime = (int)(0.36D * 20D);
        this.setMutexBits(4);
    }

    @Override
    public boolean shouldExecute() {
        TileEntity te = this.stegosaurus.world.getTileEntity(this.stegosaurus.getWorkstationPos());
        return GeneralConfig.canUsePyrotech() && te instanceof TileChoppingBlock && this.stegosaurus.isUsingWorkstation();
    }

    @Override
    public void startExecuting() {
        this.animTime = -100;
        this.destroyedFlag = false;
        this.workstationPos = this.stegosaurus.getWorkstationPos();
        this.stegosaurus.setUsingWorkAnim(false);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.stegosaurus.isUsingWorkstation() && !this.destroyedFlag;
    }

    @Override
    public void resetTask() {
        this.stegosaurus.setUsingWorkAnim(false);
        if (this.destroyedFlag) this.stegosaurus.clearWorkstation(true);
    }

    @Override
    public void updateTask() {
        if (this.stegosaurus.workstationUseFromPos() != null) {
            this.stegosaurus.getLookHelper().setLookPosition(this.stegosaurus.getWorkstationPos().getX(), this.stegosaurus.getWorkstationPos().getY(), this.stegosaurus.getWorkstationPos().getZ(), 30, 30);
            if (RiftUtil.entityAtLocation(this.stegosaurus, this.stegosaurus.workstationUseFromPos(), 3)) {
                //use workstation
                TileEntity tileEntity = this.stegosaurus.world.getTileEntity(this.stegosaurus.getWorkstationPos());
                if (tileEntity instanceof TileChoppingBlock) {
                    TileChoppingBlock choppingBlock = (TileChoppingBlock) tileEntity;
                    if (this.stegosaurus.getEnergy() > 6) {
                        if (this.animTime == 0 && !choppingBlock.getStackHandler().getStackInSlot(0).isEmpty()) this.stegosaurus.setUsingWorkAnim(true);
                        if (this.animTime == this.animHitTime && !choppingBlock.getStackHandler().getStackInSlot(0).isEmpty()) {
                            //make progress
                            int[] chops = ChoppingBlockRecipe.getRecipe(choppingBlock.getStackHandler().getStackInSlot(0)).getChops();
                            float recipeProgressIncrement = 1f / ArrayHelper.getOrLast(chops, 3);;
                            ((RiftMixinPyrotechTileEntityChoppingBlock)choppingBlock).invokeSetRecipeProgress(choppingBlock.getRecipeProgress() + recipeProgressIncrement);

                            if (ModuleTechBasicConfig.CHOPPING_BLOCK.USES_DURABILITY) {
                                //decrease chopping block durability
                                ((RiftMixinPyrotechTileEntityChoppingBlock)choppingBlock).invokeSetDurabilityUntilNextDamage(((RiftMixinPyrotechTileEntityChoppingBlock)choppingBlock).invokeGetDurabilityUntilNextDamage() - 1);

                                //increase chopping block damage
                                if (((RiftMixinPyrotechTileEntityChoppingBlock)choppingBlock).invokeGetDurabilityUntilNextDamage() <= 1) {
                                    ((RiftMixinPyrotechTileEntityChoppingBlock)choppingBlock).invokeSetDurabilityUntilNextDamage(ModuleTechBasicConfig.CHOPPING_BLOCK.CHOPS_PER_DAMAGE);
                                    if (choppingBlock.getDamage() + 1 < 4) choppingBlock.setDamage(choppingBlock.getDamage() + 1);
                                    else {
                                        StackHelper.spawnStackHandlerContentsOnTop(this.stegosaurus.world, choppingBlock.getStackHandler(), this.stegosaurus.getWorkstationPos());
                                        ItemStack itemStack = new ItemStack(ModuleCore.Blocks.ROCK, choppingBlock.getSawdust(), BlockRock.EnumType.WOOD_CHIPS.getMeta());
                                        StackHelper.spawnStackOnTop(this.stegosaurus.world, itemStack, this.stegosaurus.getWorkstationPos(), 0);
                                        this.stegosaurus.world.destroyBlock(this.stegosaurus.getWorkstationPos(), false);
                                    }
                                }
                            }

                            //place wood chips
                            if (choppingBlock.getSawdust() < 5 && Math.random() < ModuleTechBasicConfig.CHOPPING_BLOCK.WOOD_CHIPS_CHANCE * 2) {
                                choppingBlock.setSawdust(choppingBlock.getSawdust() + 1);
                                choppingBlock.markDirty();
                                BlockHelper.notifyBlockUpdate(this.stegosaurus.world, choppingBlock.getPos());
                            }

                            List<BlockPos> candidates = new ArrayList<>();
                            if (Math.random() < ModuleTechBasicConfig.CHOPPING_BLOCK.WOOD_CHIPS_CHANCE * 0.5) {
                                BlockHelper.forBlocksInCube(this.stegosaurus.world, this.stegosaurus.getWorkstationPos(), 1, 1, 1, (world, pos, blockState) -> {
                                    if (world.isAirBlock(pos)
                                            && ModuleCore.Blocks.ROCK.canPlaceBlockAt(world, pos)
                                            && blockState.getBlock() != ModuleCore.Blocks.ROCK) {
                                        candidates.add(pos);
                                    }
                                    return true;
                                });
                            }
                            if (!candidates.isEmpty()) {
                                System.out.println("place chip");
                                Collections.shuffle(candidates);
                                this.stegosaurus.world.setBlockState(candidates.get(0), ModuleCore.Blocks.ROCK.getDefaultState().withProperty(BlockRock.VARIANT, BlockRock.EnumType.WOOD_CHIPS));
                            }

                            //give outputs
                            if (choppingBlock.getRecipeProgress() >= 0.9999) {
                                ItemStack output = ChoppingBlockRecipe.getRecipe(choppingBlock.getStackHandler().getStackInSlot(0)).getOutput();
                                int[] quantities = ChoppingBlockRecipe.getRecipe(choppingBlock.getStackHandler().getStackInSlot(0)).getQuantities();

                                if (quantities.length > 0) {
                                    int quantity = ArrayHelper.getOrLast(quantities, 3);
                                    output.setCount(quantity);
                                }
                                this.stegosaurus.creatureInventory.addItem(output);
                                choppingBlock.getStackHandler().extractItem(0, choppingBlock.getStackHandler().getSlotLimit(0), false);
                                choppingBlock.markDirty();
                                BlockHelper.notifyBlockUpdate(this.stegosaurus.world, choppingBlock.getPos());
                            }

                            this.stegosaurus.playSound(this.stegosaurus.useAnimSound(), 2, 1);
                        }
                        if (this.animTime == this.animLength) {
                            this.stegosaurus.setUsingWorkAnim(false);
                            this.stegosaurus.energyActionMod++;
                            this.stegosaurus.setXP(this.stegosaurus.getXP() + 5);
                            this.animTime = -100;
                        }
                    }
                    else this.animTime = -100;

                    if (!choppingBlock.getStackHandler().getStackInSlot(0).isEmpty() || this.stegosaurus.isUsingWorkAnim()) this.animTime++;
                }
            }
            else {
                //move to front of workstation
                this.stegosaurus.getMoveHelper().setMoveTo(this.stegosaurus.workstationUseFromPos().getX(), this.stegosaurus.workstationUseFromPos().getY(), this.stegosaurus.workstationUseFromPos().getZ(), 1);
            }
        }
        if (!this.stegosaurus.isWorkstation(this.workstationPos)) this.destroyedFlag = true;
    }
}
