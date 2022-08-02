package com.anightdazingzoroark.rift.items;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.FoodComponent;

public class FoodItems {
    public static final FoodComponent TRUFFLE = new FoodComponent.Builder()
            .hunger(2)
            .saturationModifier(2)
            .build();
    public static final FoodComponent RAW_EXOTIC_MEAT = new FoodComponent.Builder()
            .hunger(4)
            .saturationModifier(2)
            .build();
    public static final FoodComponent COOKED_EXOTIC_MEAT = new FoodComponent.Builder()
            .hunger(8)
            .saturationModifier(8)
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
    public static final FoodComponent SMALL_FRIED_EGG = new FoodComponent.Builder()
            .hunger(3)
            .saturationModifier(2)
            .build();
    public static final FoodComponent MEDIUM_FRIED_EGG = new FoodComponent.Builder()
            .hunger(5)
            .saturationModifier(2)
            .build();
    public static final FoodComponent LARGE_FRIED_EGG = new FoodComponent.Builder()
            .hunger(7)
            .saturationModifier(2)
            .build();
    public static final FoodComponent MIXED_MEAT = new FoodComponent.Builder()
            .hunger(6)
            .saturationModifier(12)
            .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 120 * 20), 1f)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 5 * 20, 1), 1f)
            .alwaysEdible()
            .build();
    public static final FoodComponent ENCHANTED_MIXED_MEAT = new FoodComponent.Builder()
            .hunger(6)
            .saturationModifier(12)
            .statusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 120 * 20, 3), 1f)
            .statusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 20 * 20, 1), 1f)
            .statusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 300 * 20), 1f)
            .statusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 300 * 20), 1f)
            .alwaysEdible()
            .build();
    public static final FoodComponent TREAT = new FoodComponent.Builder()
            .hunger(2)
            .saturationModifier(1)
            .build();
}
