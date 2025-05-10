package anightdazingzoroark.prift.server.entity.workstationData;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.mixin.RiftMixinPyrotechTileEntityChoppingBlock;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.codetaylor.mc.athenaeum.util.ArrayHelper;
import com.codetaylor.mc.athenaeum.util.BlockHelper;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.block.BlockRock;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasicConfig;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.ChoppingBlockRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.TileChoppingBlock;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiftPyrotechChoppingBlockWorkstation extends RiftWorkstationData {
    @Override
    public boolean canUseWorkstation(RiftCreature user, BlockPos workstationPos) {
        if (!GeneralConfig.canUsePyrotech()) return false;

        TileChoppingBlock tileChoppingBlock = tileChoppingBlock(user, workstationPos);
        if (tileChoppingBlock == null) return false;

        return super.canUseWorkstation(user, workstationPos) && !tileChoppingBlock.getStackHandler().getStackInSlot(0).isEmpty();
    }

    @Override
    public void onStartWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    @Override
    public void onHitWorkstation(RiftCreature user, BlockPos workstationPos) {
        TileChoppingBlock tileChoppingBlock = tileChoppingBlock(user, workstationPos);

        //make progress
        int[] chops = ChoppingBlockRecipe.getRecipe(tileChoppingBlock.getStackHandler().getStackInSlot(0)).getChops();
        float recipeProgressIncrement = 1f / ArrayHelper.getOrLast(chops, 3);;
        ((RiftMixinPyrotechTileEntityChoppingBlock)tileChoppingBlock).invokeSetRecipeProgress(tileChoppingBlock.getRecipeProgress() + recipeProgressIncrement);

        if (ModuleTechBasicConfig.CHOPPING_BLOCK.USES_DURABILITY) {
            //decrease chopping block durability
            ((RiftMixinPyrotechTileEntityChoppingBlock)tileChoppingBlock).invokeSetDurabilityUntilNextDamage(((RiftMixinPyrotechTileEntityChoppingBlock)tileChoppingBlock).invokeGetDurabilityUntilNextDamage() - 1);

            //increase chopping block damage
            if (((RiftMixinPyrotechTileEntityChoppingBlock)tileChoppingBlock).invokeGetDurabilityUntilNextDamage() <= 1) {
                ((RiftMixinPyrotechTileEntityChoppingBlock)tileChoppingBlock).invokeSetDurabilityUntilNextDamage(ModuleTechBasicConfig.CHOPPING_BLOCK.CHOPS_PER_DAMAGE);
                if (tileChoppingBlock.getDamage() + 1 < 4) tileChoppingBlock.setDamage(tileChoppingBlock.getDamage() + 1);
                else {
                    StackHelper.spawnStackHandlerContentsOnTop(user.world, tileChoppingBlock.getStackHandler(), workstationPos);
                    ItemStack itemStack = new ItemStack(ModuleCore.Blocks.ROCK, tileChoppingBlock.getSawdust(), BlockRock.EnumType.WOOD_CHIPS.getMeta());
                    StackHelper.spawnStackOnTop(user.world, itemStack, workstationPos, 0);
                    user.world.destroyBlock(workstationPos, false);
                }
            }
        }

        //place wood chips
        if (tileChoppingBlock.getSawdust() < 5 && Math.random() < ModuleTechBasicConfig.CHOPPING_BLOCK.WOOD_CHIPS_CHANCE * 2) {
            tileChoppingBlock.setSawdust(tileChoppingBlock.getSawdust() + 1);
            tileChoppingBlock.markDirty();
            BlockHelper.notifyBlockUpdate(user.world, tileChoppingBlock.getPos());
        }

        List<BlockPos> candidates = new ArrayList<>();
        if (Math.random() < ModuleTechBasicConfig.CHOPPING_BLOCK.WOOD_CHIPS_CHANCE * 0.5) {
            BlockHelper.forBlocksInCube(user.world, workstationPos, 1, 1, 1, (world, pos, blockState) -> {
                if (world.isAirBlock(pos)
                        && ModuleCore.Blocks.ROCK.canPlaceBlockAt(world, pos)
                        && blockState.getBlock() != ModuleCore.Blocks.ROCK) {
                    candidates.add(pos);
                }
                return true;
            });
        }
        if (!candidates.isEmpty()) {
            Collections.shuffle(candidates);
            user.world.setBlockState(candidates.get(0), ModuleCore.Blocks.ROCK.getDefaultState().withProperty(BlockRock.VARIANT, BlockRock.EnumType.WOOD_CHIPS));
        }

        //give outputs
        if (tileChoppingBlock.getRecipeProgress() >= 0.9999) {
            ItemStack output = ChoppingBlockRecipe.getRecipe(tileChoppingBlock.getStackHandler().getStackInSlot(0)).getOutput();
            int[] quantities = ChoppingBlockRecipe.getRecipe(tileChoppingBlock.getStackHandler().getStackInSlot(0)).getQuantities();

            if (quantities.length > 0) {
                int quantity = ArrayHelper.getOrLast(quantities, 3);
                output.setCount(quantity);
            }
            user.creatureInventory.addItem(output);
            tileChoppingBlock.getStackHandler().extractItem(0, tileChoppingBlock.getStackHandler().getSlotLimit(0), false);
            tileChoppingBlock.markDirty();
            BlockHelper.notifyBlockUpdate(user.world, tileChoppingBlock.getPos());
        }

        user.playSound(SoundEvents.BLOCK_WOOD_BREAK, 2, 1);
    }

    @Override
    public void onEndWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    private TileChoppingBlock tileChoppingBlock(RiftCreature creature, BlockPos workstationPos) {
        TileEntity tileEntity = creature.world.getTileEntity(workstationPos);
        if (tileEntity instanceof TileChoppingBlock) return (TileChoppingBlock) tileEntity;
        return null;
    }
}
