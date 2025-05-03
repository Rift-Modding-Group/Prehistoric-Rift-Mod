package anightdazingzoroark.prift.server.blocks;

import anightdazingzoroark.prift.RiftUtil;
import anightdazingzoroark.prift.compat.mysticalmechanics.items.RiftMMItems;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public abstract class RiftBerryBush extends BlockBush implements IGrowable {
    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 3);

    public RiftBerryBush() {
        this.setDefaultState(this.blockState.getBaseState().withProperty(this.getAgeProperty(), Integer.valueOf(0)));
        this.setTickRandomly(true);
        this.setHardness(0.0F);
        this.setSoundType(SoundType.PLANT);
    }

    public abstract Item berryItem();

    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this.berryItem());
    }

    @Override
    public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, net.minecraft.world.IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        super.getDrops(drops, world, pos, state, 0);
        int age = this.getAge(state);
        int dropAmnt = ((age == 2) ? RiftUtil.randomInRange(1, 2) : ((age == 3) ? RiftUtil.randomInRange(2, 3) : 1)) + fortune;
        drops.add(new ItemStack(this.berryItem(), dropAmnt, 0));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        int age = this.getAge(state);
        if (!worldIn.isRemote) {
            if (age >= 2 && playerIn.getHeldItem(hand).getItem() != Items.DYE) {
                int dropAmnt = (age == 2) ? RiftUtil.randomInRange(1, 2) : ((age == 3) ? RiftUtil.randomInRange(2, 3) : 0);
                EntityItem droppedItem = new EntityItem(worldIn);
                droppedItem.setPosition(pos.getX(), pos.getY() + 1, pos.getZ());
                droppedItem.setItem(new ItemStack(this.berryItem(), dropAmnt));
                worldIn.spawnEntity(droppedItem);
                worldIn.setBlockState(pos, this.withAge(1), 2);
            }
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {AGE});
    }

    public IBlockState getStateFromMeta(int meta)
    {
        return this.withAge(meta);
    }

    public int getMetaFromState(IBlockState state)
    {
        return this.getAge(state);
    }

    protected PropertyInteger getAgeProperty() {
        return AGE;
    }

    protected int getAge(IBlockState state)
    {
        return ((Integer)state.getValue(this.getAgeProperty())).intValue();
    }

    public IBlockState withAge(int age) {
        return this.getDefaultState().withProperty(this.getAgeProperty(), Integer.valueOf(age));
    }

    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);

        if (!worldIn.isAreaLoaded(pos, 1)) return;
        if (worldIn.getLightFromNeighbors(pos.up()) >= 9) {
            int i = this.getAge(state);
            if (i < 3 && RiftUtil.randomInRange(0, 7) == 0) {
                worldIn.setBlockState(pos, this.withAge(i + 1), 2);
            }
        }
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        if ((state.getValue(this.getAgeProperty())).intValue() == 0) {
            return new AxisAlignedBB(0.3D, 0.0D, 0.3D, 0.7D, 0.6D, 0.7D);
        }
        return new AxisAlignedBB(0.15D, 0.15D, 0D, 0.85D, 1D, 0.85D);
    }

    @Override
    protected void checkAndDropBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.checkAndDropBlock(worldIn, pos, state);
        if (!this.canBlockStay(worldIn, pos, state)) {
            worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        worldIn.setBlockState(pos, this.withAge(this.getAge(state) + 1), 2);
    }
}
