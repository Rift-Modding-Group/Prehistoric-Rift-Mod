package com.anightdazingzoroark.rift.server.items;

import com.anightdazingzoroark.rift.RiftInitialize;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RiftInitialize.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RiftCreativeTab {
    public static CreativeModeTab RIFT_ITEMS_TAB;
    public static CreativeModeTab RIFT_ENTITIES_TAB;

    @SubscribeEvent
    public static void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
        RIFT_ITEMS_TAB = event.registerCreativeModeTab(new ResourceLocation(RiftInitialize.MODID, "rift_items_tab"),
                builder -> builder.icon(() -> new ItemStack(RiftItemRegistry.TYRANNOSAURUS_ARM.get()))
                        .title(Component.translatable("rift.items_tab")));

        RIFT_ENTITIES_TAB = event.registerCreativeModeTab(new ResourceLocation(RiftInitialize.MODID, "rift_entities_tab"),
                builder -> builder.icon(() -> new ItemStack(RiftItemRegistry.TYRANNOSAURUS_SPAWN_EGG.get()))
                        .title(Component.translatable("rift.entities_tab")));
    }

    public static void addCreative(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == RiftCreativeTab.RIFT_ITEMS_TAB) {
            //treats
            event.accept(RiftItemRegistry.BERRY_TREAT);
            event.accept(RiftItemRegistry.GRASSY_TREAT);
            event.accept(RiftItemRegistry.ROOTED_TREAT);
            event.accept(RiftItemRegistry.LEAFY_TREAT);
            event.accept(RiftItemRegistry.FLESH_TREAT);
            event.accept(RiftItemRegistry.FISH_TREAT);
            event.accept(RiftItemRegistry.CARRION_TREAT);
            event.accept(RiftItemRegistry.BUG_TREAT);

            //meat related stuff
            event.accept(RiftItemRegistry.RAW_EXOTIC_MEAT);
            event.accept(RiftItemRegistry.COOKED_EXOTIC_MEAT);
            event.accept(RiftItemRegistry.RAW_HEMOLYMPH);
            event.accept(RiftItemRegistry.BOILED_HEMOLYMPH);

            //tribute items
            event.accept(RiftItemRegistry.TYRANNOSAURUS_ARM);

            //eggs
            event.accept(RiftItemRegistry.TYRANNOSAURUS_EGG);
        }
        if (event.getTab() == RiftCreativeTab.RIFT_ENTITIES_TAB) {
            event.accept(RiftItemRegistry.TYRANNOSAURUS_SPAWN_EGG);
        }
    }
}
