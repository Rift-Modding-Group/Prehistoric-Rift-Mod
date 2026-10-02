package anightdazingzoroark.prift.server.block;

import anightdazingzoroark.prift.server.item.RiftItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import java.util.Random;

public class SmokenutBush extends BlockCrops {
    public static final PropertyBool HAS_SMOKENUTS = PropertyBool.create("has_smokenuts");
    private final AxisAlignedBB[] boundingBoxes = {
            new AxisAlignedBB(0.25D, 0D, 0.25D, 0.75D, 0.375D, 0.75D),
            new AxisAlignedBB(0.1875D, 0D, 0.1875D, 0.8125D, 0.5625D, 0.8125D),
            new AxisAlignedBB(0.125D, 0D, 0.125D, 0.875D, 0.75D, 0.875D),
            new AxisAlignedBB(0.125D, 0D, 0.125D, 0.875D, 0.75D, 0.875D)
    };

    public SmokenutBush() {
        super();
        this.setDefaultState(this.getDefaultState().withProperty(HAS_SMOKENUTS, false));
    }

    @Override
    public int getMaxAge() {
        return 3;
    }

    @Override
    protected Item getSeed() {
        return RiftItems.SMOKENUT_SEEDS;
    }

    @Override
    protected Item getCrop() {
        return RiftItems.SMOKENUT;
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(RiftItems.SMOKENUT);
    }

    @Override
    protected int getBonemealAgeIncrease(World world) {
        return 1;
    }

    @Override
    protected boolean canSustainBush(IBlockState state) {
        Block block = state.getBlock();
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return this.getAge(state) < this.getMaxAge() || !state.getValue(HAS_SMOKENUTS);
    }

    @Override
    public void grow(World world, BlockPos pos, IBlockState state) {
        int age = Math.min(this.getAge(state) + 1, this.getMaxAge());
        world.setBlockState(pos, state.withProperty(AGE, age).withProperty(HAS_SMOKENUTS, age >= 2), 2);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!this.canBlockStay(world, pos, state)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            return;
        }

        int age = this.getAge(state);
        boolean canGrow = age < this.getMaxAge() || !state.getValue(HAS_SMOKENUTS);
        if (canGrow && world.getLightFromNeighbors(pos.up()) >= 9
                && ForgeHooks.onCropsGrowPre(world, pos, state, random.nextInt(5) == 0)) {
            int grownAge = Math.min(age + 1, this.getMaxAge());
            IBlockState grownState = state.withProperty(AGE, grownAge).withProperty(HAS_SMOKENUTS, grownAge >= 2);
            world.setBlockState(pos, grownState, 2);
            ForgeHooks.onCropsGrowPost(world, pos, state, grownState);
        }
    }

    @Override
    public boolean onBlockActivated(
            World world, BlockPos pos, IBlockState state, EntityPlayer player,
            EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ
    ) {
        int age = this.getAge(state);
        if (!state.getValue(HAS_SMOKENUTS)) return false;

        if (!world.isRemote) {
            int smokenutCount = 1 + world.rand.nextInt(2) + (age == this.getMaxAge() ? 1 : 0);
            spawnAsEntity(world, pos, new ItemStack(RiftItems.SMOKENUT, smokenutCount));
            world.setBlockState(pos, state.withProperty(HAS_SMOKENUTS, false), 2);
            world.playSound(
                    null, pos, SoundEvents.BLOCK_GRASS_BREAK, SoundCategory.BLOCKS,
                    1f, 0.8f + world.rand.nextFloat() * 0.4f
            );
        }
        return true;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int age = this.getAge(state);
        if (!state.getValue(HAS_SMOKENUTS)) return;
        Random random = new Random();
        drops.add(new ItemStack(RiftItems.SMOKENUT, 1 + random.nextInt(2) + (age == this.getMaxAge() ? 1 : 0)));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return this.boundingBoxes[Math.clamp(this.getAge(state), 0, this.getMaxAge())];
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, meta & 7).withProperty(HAS_SMOKENUTS, (meta & 8) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return this.getAge(state) | (state.getValue(HAS_SMOKENUTS) ? 8 : 0);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE, HAS_SMOKENUTS);
    }
}
