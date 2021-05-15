package com.anightdazingzoroark.rift.items;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public class FoodItems {
    public static final FoodComponent RAW_EXOTIC_MEAT = new FoodComponent.Builder()
            .hunger(4)
            .saturationModifier(2)
            .build();
    public static final FoodComponent COOKED_EXOTIC_MEAT = new FoodComponent.Builder()
            .hunger(8)
            .saturationModifier(6)
            .build();
    public static final FoodComponent RAW_DODO_MEAT = new FoodComponent.Builder()
            .hunger(2)
            .saturationModifier(2)
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 30 * 20), 1f)
            .build();
    public static final FoodComponent COOKED_DODO_MEAT = new FoodComponent.Builder()
            .hunger(4)
            .saturationModifier(12)
            .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 30 * 20, 3), 1f)
            .build();
    public static final FoodComponent RAW_HADROSAUR_MEAT = new FoodComponent.Builder()
            .hunger(3)
            .saturationModifier(2)
            .statusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 30 * 20), 1f)
            .build();
    public static final FoodComponent COOKED_HADROSAUR_MEAT = new FoodComponent.Builder()
            .hunger(6)
            .saturationModifier(12)
            .statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 60 * 20, 3), 1f)
            .build();
}
