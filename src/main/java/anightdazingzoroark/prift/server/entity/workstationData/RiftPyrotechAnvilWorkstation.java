package anightdazingzoroark.prift.server.entity.workstationData;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.mixin.RiftMixinPyrotechTileEntityAnvil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import com.codetaylor.mc.athenaeum.util.BlockHelper;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import com.codetaylor.mc.pyrotech.modules.tech.basic.network.SCPacketParticleAnvilHit;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.AnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.spi.TileAnvilBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class RiftPyrotechAnvilWorkstation extends RiftWorkstationData {
    @Override
    public boolean canUseWorkstation(RiftCreature user, BlockPos workstationPos) {
        if (!GeneralConfig.canUsePyrotech()) return false;

        TileAnvilBase tileAnvilBase = tileAnvilBase(user, workstationPos);
        if (tileAnvilBase == null) return false;

        //test if there's a valid recipe for the anvil based on the item on it, or set it
        if (tileAnvilBase.getRecipe() == null) {
            ItemStack inputItemStack = tileAnvilBase.getStackHandler().extractItem(0, tileAnvilBase.getStackHandler().getSlotLimit(0), true);
            AnvilRecipe recipe = AnvilRecipe.getRecipe(inputItemStack, tileAnvilBase.getRecipeTier(), AnvilRecipe.EnumType.HAMMER);
            if (recipe != null) {
                ((RiftMixinPyrotechTileEntityAnvil)tileAnvilBase).invokeAnvilSetRecipe(recipe);
            }
        }

        return super.canUseWorkstation(user, workstationPos) && tileAnvilBase.getRecipe() != null;
    }

    @Override
    public void onStartWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    @Override
    public void onHitWorkstation(RiftCreature user, BlockPos workstationPos) {
        TileAnvilBase tileAnvilBase = tileAnvilBase(user, workstationPos);
        
        //make progress
        int hits = Math.max(1, tileAnvilBase.getRecipe().getHits() - 3);
        float recipeProgressIncrement = 1f / hits;
        tileAnvilBase.setRecipeProgress(tileAnvilBase.getRecipeProgress() + recipeProgressIncrement);

        if (tileAnvilBase.useDurability()) {
            //decrease anvil durability
            if (tileAnvilBase.getRecipe() instanceof AnvilRecipe.IExtendedRecipe) {
                ((AnvilRecipe.IExtendedRecipe<?>) tileAnvilBase.getRecipe()).applyDamage(user.world, tileAnvilBase);
            }
            else tileAnvilBase.setDurabilityUntilNextDamage(tileAnvilBase.getDurabilityUntilNextDamage() - 1);

            //increase anvil damage
            if (tileAnvilBase.getDurabilityUntilNextDamage() <= 1) {
                tileAnvilBase.setDurabilityUntilNextDamage(((RiftMixinPyrotechTileEntityAnvil)tileAnvilBase).invokeGetHitsPerDamage());
                if (tileAnvilBase.getDamage() + 1 < 4) tileAnvilBase.setDamage(tileAnvilBase.getDamage() + 1);
                else {
                    if (tileAnvilBase.getRecipe() instanceof AnvilRecipe.IExtendedRecipe) {
                        ((AnvilRecipe.IExtendedRecipe<?>) tileAnvilBase.getRecipe()).onAnvilDurabilityExpired(user.world, tileAnvilBase, 0.5f, 0.4375f, 0.5f);
                    }
                    else {
                        StackHelper.spawnStackHandlerContentsOnTop(user.world, tileAnvilBase.getStackHandler(), workstationPos);
                        user.world.destroyBlock(workstationPos, false);
                    }
                }
            }
        }

        //give outputs
        if (tileAnvilBase.getRecipeProgress() >= 0.9999) {
            if (tileAnvilBase.getRecipe() instanceof AnvilRecipe.IExtendedRecipe) {
                List<ItemStack> outputList = ((AnvilRecipe.IExtendedRecipe) tileAnvilBase.getRecipe()).onRecipeCompleted(tileAnvilBase, user.world, tileAnvilBase.getStackHandler(), tileAnvilBase.getRecipe(), null);
                for (ItemStack itemOutput : outputList) user.creatureInventory.addItem(itemOutput);
            }
            else {
                user.creatureInventory.addItem(tileAnvilBase.getRecipe().getOutput());
                tileAnvilBase.getStackHandler().extractItem(0, tileAnvilBase.getStackHandler().getSlotLimit(0), false);
            }
            ((RiftMixinPyrotechTileEntityAnvil)tileAnvilBase).invokeAnvilSetRecipe(null);
        }
        tileAnvilBase.markDirty();
        BlockHelper.notifyBlockUpdate(user.world, tileAnvilBase.getPos());

        user.playSound(SoundEvents.BLOCK_STONE_BREAK, 2, 1);
        ModuleTechBasic.PACKET_SERVICE.sendToAllAround(new SCPacketParticleAnvilHit(workstationPos, 0.5f, 0.4375f, 0.5f), tileAnvilBase);
    }

    @Override
    public void onEndWorkstationUse(RiftCreature user, BlockPos workstationPos) {}

    private TileAnvilBase tileAnvilBase(RiftCreature creature, BlockPos workstationPos) {
        TileEntity tileEntity = creature.world.getTileEntity(workstationPos);
        if (tileEntity instanceof TileAnvilBase) return (TileAnvilBase) tileEntity;
        return null;
    }
}
