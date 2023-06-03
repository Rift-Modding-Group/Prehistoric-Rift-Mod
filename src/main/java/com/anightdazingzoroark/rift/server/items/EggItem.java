package com.anightdazingzoroark.rift.server.items;

import com.anightdazingzoroark.rift.server.entities.RiftEgg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class EggItem extends Item {
    private final Supplier<? extends EntityType<? extends Mob>> entity;

    public EggItem(Supplier<? extends EntityType<? extends Mob>> entity, Properties properties) {
        super(properties);
        this.entity = entity;
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        else {
            ItemStack itemstack = context.getItemInHand();
            BlockPos blockpos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            BlockState blockstate = level.getBlockState(blockpos);
            BlockPos blockpos1;

            if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
                blockpos1 = blockpos;
            } else {
                blockpos1 = blockpos.relative(direction);
            }

            EntityType<?> entitytype = this.getType(itemstack.getTag());
            if (entitytype.spawn((ServerLevel)level, itemstack, context.getPlayer(), blockpos1, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
                itemstack.shrink(1);
                level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
            }

            return InteractionResult.CONSUME;
        }
    }

    public EntityType<?> getType(@Nullable CompoundTag p_43229_) {
        if (p_43229_ != null && p_43229_.contains("EntityTag", 10)) {
            CompoundTag compoundtag = p_43229_.getCompound("EntityTag");
            if (compoundtag.contains("id", 8)) {
                return EntityType.byString(compoundtag.getString("id")).orElse(this.getDefaultType());
            }
        }

        return this.getDefaultType();
    }

    protected EntityType<?> getDefaultType() {
        return entity.get();
    }
}
