package anightdazingzoroark.prift.server.entity.ai;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.mixin.RiftMixinPyrotechTileEntityAnvil;
import anightdazingzoroark.prift.server.entity.creature.Ankylosaurus;
import com.codetaylor.mc.athenaeum.util.BlockHelper;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.init.ItemInitializer;
import com.codetaylor.mc.pyrotech.modules.tech.basic.network.SCPacketParticleAnvilHit;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.AnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.spi.TileAnvilBase;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class RiftAnkylosaurusHitAnvil extends EntityAIBase {
    private final Ankylosaurus ankylosaurus;
    private final int animLength;
    private final int animHitTime;
    private int animTime;
    private boolean destroyedFlag;
    private BlockPos workstationPos;

    public RiftAnkylosaurusHitAnvil(Ankylosaurus ankylosaurus) {
        this.ankylosaurus = ankylosaurus;
        this.animLength = (int)(1.2D * 20D);
        this.animHitTime = (int)(0.6D * 20D);
        this.setMutexBits(4);
    }

    @Override
    public boolean shouldExecute() {
        TileEntity te = this.ankylosaurus.world.getTileEntity(this.ankylosaurus.getWorkstationPos());
        return GeneralConfig.canUsePyrotech() && te instanceof TileAnvilBase && this.ankylosaurus.isUsingWorkstation() && !this.ankylosaurus.isHiding();
    }

    @Override
    public void startExecuting() {
        this.animTime = -5;
        this.destroyedFlag = false;
        this.workstationPos = this.ankylosaurus.getWorkstationPos();
        this.ankylosaurus.setUsingWorkAnim(false);
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.ankylosaurus.isUsingWorkstation() && !this.destroyedFlag;
    }

    @Override
    public void resetTask() {
        this.ankylosaurus.setUsingWorkAnim(false);
        if (this.destroyedFlag) this.ankylosaurus.clearWorkstation(true);
    }

    @Override
    public void updateTask() {
        if (this.ankylosaurus.workstationUseFromPos() != null) {
            this.ankylosaurus.getLookHelper().setLookPosition(this.ankylosaurus.getWorkstationPos().getX(), this.ankylosaurus.getWorkstationPos().getY(), this.ankylosaurus.getWorkstationPos().getZ(), 30, 30);
            if (RiftUtil.entityAtLocation(this.ankylosaurus, this.ankylosaurus.workstationUseFromPos(), 3)) {
                //use workstation
                TileEntity tileEntity = this.ankylosaurus.world.getTileEntity(this.ankylosaurus.getWorkstationPos());
                if (tileEntity != null) {

                    if (tileEntity instanceof TileAnvilBase) {
                        TileAnvilBase tileAnvilBase = (TileAnvilBase) tileEntity;
                        if (tileAnvilBase.getRecipe() == null) {
                            ItemStack inputItemStack = tileAnvilBase.getStackHandler().extractItem(0, tileAnvilBase.getStackHandler().getSlotLimit(0), true);
                            AnvilRecipe recipe = AnvilRecipe.getRecipe(inputItemStack, tileAnvilBase.getRecipeTier(), AnvilRecipe.EnumType.HAMMER);
                            if (recipe != null) {
                                ((RiftMixinPyrotechTileEntityAnvil)tileAnvilBase).invokeAnvilSetRecipe(recipe);
                                this.animTime = -5;
                            }
                        }

                        if (this.ankylosaurus.getEnergy() > 6) {
                            if (this.animTime == 0 && tileAnvilBase.getRecipe() != null) this.ankylosaurus.setUsingWorkAnim(true);
                            if (this.animTime == this.animHitTime && tileAnvilBase.getRecipe() != null) {
                                int hits = Math.max(1, tileAnvilBase.getRecipe().getHits() - 3);
                                float recipeProgressIncrement = 1f / hits;
                                tileAnvilBase.setRecipeProgress(tileAnvilBase.getRecipeProgress() + recipeProgressIncrement);

                                if (tileAnvilBase.useDurability()) {
                                    //decrease anvil durability
                                    if (tileAnvilBase.getRecipe() instanceof AnvilRecipe.IExtendedRecipe) {
                                        ((AnvilRecipe.IExtendedRecipe<?>) tileAnvilBase.getRecipe()).applyDamage(this.ankylosaurus.world, tileAnvilBase);
                                    }
                                    else tileAnvilBase.setDurabilityUntilNextDamage(tileAnvilBase.getDurabilityUntilNextDamage() - 1);

                                    //decrease anvil damage
                                    if (tileAnvilBase.getDurabilityUntilNextDamage() <= 1) {
                                        tileAnvilBase.setDurabilityUntilNextDamage(((RiftMixinPyrotechTileEntityAnvil)tileAnvilBase).invokeGetHitsPerDamage());
                                        if (tileAnvilBase.getDamage() + 1 < 4) tileAnvilBase.setDamage(tileAnvilBase.getDamage() + 1);
                                        else {
                                            if (tileAnvilBase.getRecipe() instanceof AnvilRecipe.IExtendedRecipe) {
                                                ((AnvilRecipe.IExtendedRecipe<?>) tileAnvilBase.getRecipe()).onAnvilDurabilityExpired(this.ankylosaurus.world, tileAnvilBase, 0.5f, 0.4375f, 0.5f);
                                            }
                                            else {
                                                StackHelper.spawnStackHandlerContentsOnTop(this.ankylosaurus.world, tileAnvilBase.getStackHandler(), this.ankylosaurus.getWorkstationPos());
                                                this.ankylosaurus.world.destroyBlock(this.ankylosaurus.getWorkstationPos(), false);
                                            }
                                        }
                                    }
                                }

                                //give outputs
                                if (tileAnvilBase.getRecipeProgress() >= 0.9999) {
                                    if (tileAnvilBase.getRecipe() instanceof AnvilRecipe.IExtendedRecipe) {
                                        List<ItemStack> outputList = ((AnvilRecipe.IExtendedRecipe) tileAnvilBase.getRecipe()).onRecipeCompleted(tileAnvilBase, this.ankylosaurus.world, tileAnvilBase.getStackHandler(), tileAnvilBase.getRecipe(), new ItemStack(ModuleCore.Items.IRON_HAMMER));
                                        for (ItemStack itemOutput : outputList) this.ankylosaurus.creatureInventory.addItem(itemOutput);
                                    }
                                    else {
                                        this.ankylosaurus.creatureInventory.addItem(tileAnvilBase.getRecipe().getOutput());
                                        tileAnvilBase.getStackHandler().extractItem(0, tileAnvilBase.getStackHandler().getSlotLimit(0), false);
                                    }
                                    ((RiftMixinPyrotechTileEntityAnvil)tileAnvilBase).invokeAnvilSetRecipe(null);
                                }
                                tileAnvilBase.markDirty();
                                BlockHelper.notifyBlockUpdate(this.ankylosaurus.world, tileAnvilBase.getPos());

                                this.ankylosaurus.playSound(this.ankylosaurus.useAnimSound(), 2, 1);
                                ModuleTechBasic.PACKET_SERVICE.sendToAllAround(new SCPacketParticleAnvilHit(this.workstationPos, 0.5f, 0.4375f, 0.5f), tileAnvilBase);
                            }
                            if (this.animTime == this.animLength) {
                                this.ankylosaurus.setUsingWorkAnim(false);
                                this.ankylosaurus.energyActionMod++;
                                this.ankylosaurus.setXP(this.ankylosaurus.getXP() + 5);
                                this.animTime = -5;
                                System.out.println("end bonk");
                            }
                        }
                        else this.animTime = -5;

                        if (tileAnvilBase.getRecipe() != null || this.ankylosaurus.isUsingWorkAnim()) this.animTime++;
                    }
                }
            }
            else {
                //move to front of workstation
                this.ankylosaurus.getMoveHelper().setMoveTo(this.ankylosaurus.workstationUseFromPos().getX(), this.ankylosaurus.workstationUseFromPos().getY(), this.ankylosaurus.workstationUseFromPos().getZ(), 1);
            }
        }
        if (!this.ankylosaurus.isWorkstation(this.workstationPos)) this.destroyedFlag = true;
    }
}
