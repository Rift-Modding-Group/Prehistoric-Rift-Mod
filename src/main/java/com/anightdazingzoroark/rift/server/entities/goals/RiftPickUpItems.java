package com.anightdazingzoroark.rift.server.entities.goals;

import com.anightdazingzoroark.rift.server.entities.RiftCreature;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class RiftPickUpItems extends Goal {
    protected final RiftCreature mob;
    protected final Predicate<ItemEntity> itemsToGet;

    public RiftPickUpItems(RiftCreature mob, Predicate<ItemEntity> itemsToGet) {
        this.mob = mob;
        this.itemsToGet = itemsToGet;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() == null && this.mob.getLastHurtByMob() == null) {
            List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), this.mob.getAttributeValue(Attributes.FOLLOW_RANGE)), this.itemsToGet);
            return !list.isEmpty();
        }
        else {
            return false;
        }
    }

    public void tick() {
        System.out.println("where item");
        List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), this.mob.getAttributeValue(Attributes.FOLLOW_RANGE)), this.itemsToGet);
        if (!list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.get(0), 1F);
        }
    }

    public void start() {
        List<ItemEntity> list = this.mob.level.getEntitiesOfClass(ItemEntity.class, this.mob.getBoundingBox().inflate(this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), this.mob.getAttributeValue(Attributes.FOLLOW_RANGE), this.mob.getAttributeValue(Attributes.FOLLOW_RANGE)), this.itemsToGet);
        if (!list.isEmpty()) {
            this.mob.getNavigation().moveTo(list.get(0), 1F);
        }
    }
}
