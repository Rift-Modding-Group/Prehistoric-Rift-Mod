package anightdazingzoroark.prift.server.item;

import anightdazingzoroark.prift.client.RiftCreativeTabs;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.creature.RiftCreatureRegistry;
import anightdazingzoroark.prift.api.creature.builder.RiftCreatureBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//note to self: use ItemMonsterPlacer as reference
public class RiftCreatureSpawnEggItem extends ItemMonsterPlacer {
    private static final String CREATURE_KEY = "CreatureType";

    public RiftCreatureSpawnEggItem() {
        this.setCreativeTab(RiftCreativeTabs.spawnEggsTab);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    //static helper class for creature name in use by stack
    public static String getCreatureName(ItemStack stack) {
        if (!stack.hasTagCompound()) return RiftCreatureRegistry.DEFAULT_CREATURE;

        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(CREATURE_KEY)) return RiftCreatureRegistry.DEFAULT_CREATURE;

        String creatureName = tag.getString(CREATURE_KEY);
        return RiftCreatureRegistry.hasCreatureBuilder(creatureName) ? creatureName : RiftCreatureRegistry.DEFAULT_CREATURE;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(getCreatureName(stack));
        String creatureName = builder != null ? builder.getLocalizedName() : "???";
        return I18n.format("item.spawn_egg.name", creatureName);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag tooltipFlag) {
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(getCreatureName(stack));
        String creatureName = builder != null ? builder.getName() : "unknown";
        tooltip.add(TextFormatting.GRAY + I18n.format("entity."+creatureName+".spawn_description"));
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;

        List<String> creatureNames = new ArrayList<>(RiftCreatureRegistry.getCreatureNames());
        Collections.sort(creatureNames);

        for (String creatureName : creatureNames) {
            //define stack for creature and add it
            ItemStack stack = new ItemStack(RiftItems.CREATURE_SPAWN_EGG);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString(CREATURE_KEY, creatureName);
            stack.setTagCompound(tag);
            items.add(stack);
        }
    }

    @Override
    public EnumActionResult onItemUse(
            EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing,
            float hitX, float hitY, float hitZ
    ) {
        if (worldIn.isRemote) return EnumActionResult.SUCCESS;

        ItemStack itemstack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos.offset(facing), facing, itemstack)) return EnumActionResult.FAIL;

        /*
        note to self: idk if this should be implemented lmao
        IBlockState iblockstate = worldIn.getBlockState(pos);
        Block block = iblockstate.getBlock();

        if (block == Blocks.MOB_SPAWNER) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof TileEntityMobSpawner) {
                MobSpawnerBaseLogic mobspawnerbaselogic = ((TileEntityMobSpawner)tileentity).getSpawnerBaseLogic();
                mobspawnerbaselogic.setEntityId(getNamedIdFrom(itemstack));
                tileentity.markDirty();
                worldIn.notifyBlockUpdate(pos, iblockstate, iblockstate, 3);

                if (!player.capabilities.isCreativeMode) itemstack.shrink(1);

                return EnumActionResult.SUCCESS;
            }
        }
         */

        BlockPos spawnPos = pos.offset(facing);
        this.spawnCreature(worldIn, player, itemstack, spawnPos.getX() + 0.5D, spawnPos.getY() + this.getYOffset(worldIn, spawnPos), spawnPos.getZ() + 0.5D);

        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        if (worldIn.isRemote) return new ActionResult<>(EnumActionResult.PASS, itemstack);

        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(getCreatureName(itemstack));
        if (builder == null) return new ActionResult<>(EnumActionResult.FAIL, itemstack);

        RayTraceResult raytraceresult = this.rayTrace(worldIn, playerIn, true);

        if (raytraceresult != null && raytraceresult.typeOfHit == RayTraceResult.Type.BLOCK) {
            BlockPos spawnPos = raytraceresult.getBlockPos();

            if (!(worldIn.getBlockState(spawnPos).getBlock() instanceof BlockLiquid)) {
                return new ActionResult<>(EnumActionResult.PASS, itemstack);
            }
            else if (worldIn.isBlockModifiable(playerIn, spawnPos) && playerIn.canPlayerEdit(spawnPos, raytraceresult.sideHit, itemstack)) {
                this.spawnCreature(worldIn, playerIn, itemstack, spawnPos.getX() + 0.5D, spawnPos.getY() + 0.5D, spawnPos.getZ() + 0.5D);
                playerIn.addStat(StatList.getObjectUseStats(this));
                return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
            }
            else return new ActionResult<>(EnumActionResult.FAIL, itemstack);
        }
        else return new ActionResult<>(EnumActionResult.PASS, itemstack);
    }

    private void spawnCreature(World worldIn, EntityPlayer player, ItemStack itemstack, double x, double y, double z) {
        RiftCreatureBuilder builder = RiftCreatureRegistry.getCreatureBuilder(getCreatureName(itemstack));
        if (builder == null) return;

        RiftCreature creature = RiftCreatureRegistry.createCreature(worldIn, builder.getName());
        creature.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(worldIn.rand.nextFloat() * 360f), 0f);
        creature.onInitialSpawn(worldIn.getDifficultyForLocation(new BlockPos(creature)), null);
        creature.enablePersistence();
        worldIn.spawnEntity(creature);
        if (itemstack.hasDisplayName()) creature.setCustomNameTag(itemstack.getDisplayName());
        if (!player.capabilities.isCreativeMode) itemstack.shrink(1);
    }
}
