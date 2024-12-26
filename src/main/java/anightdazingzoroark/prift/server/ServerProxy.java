package anightdazingzoroark.prift.server;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.compat.crafttweaker.RiftCrafttweaker;
import anightdazingzoroark.prift.compat.mysticalmechanics.inventory.*;
import anightdazingzoroark.prift.compat.mysticalmechanics.recipes.RiftMMRecipes;
import anightdazingzoroark.prift.compat.mysticalmechanics.tileentities.*;
import anightdazingzoroark.prift.compat.simpledifficulty.ModifierDimetrodon;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.blocks.RiftBlocks;
import anightdazingzoroark.prift.server.capabilities.CapabilityHandler;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.INonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffects;
import anightdazingzoroark.prift.server.capabilities.nonPotionEffects.NonPotionEffectsStorage;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.IPlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgress;
import anightdazingzoroark.prift.server.capabilities.playerJournalProgress.PlayerJournalProgressStorage;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesStorage;
import anightdazingzoroark.prift.server.creatureSpawning.RiftCreatureSpawning;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.entity.RiftEntities;
import anightdazingzoroark.prift.server.entity.largeWeapons.RiftLargeWeapon;
import anightdazingzoroark.prift.server.events.RiftCreatureBoxBorder;
import anightdazingzoroark.prift.server.events.ServerEvents;
import anightdazingzoroark.prift.server.fluids.RiftFluids;
import anightdazingzoroark.prift.server.inventory.CreatureContainer;
import anightdazingzoroark.prift.server.inventory.FeedingTroughContainer;
import anightdazingzoroark.prift.server.inventory.WeaponContainer;
import anightdazingzoroark.prift.server.items.RiftItems;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.recipes.RiftRecipes;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntities;
import anightdazingzoroark.prift.server.tileentities.RiftTileEntityFeedingTrough;
import anightdazingzoroark.prift.server.world.RiftPlantGenerator;
import anightdazingzoroark.prift.server.world.RiftStructureGenerator;
import com.charles445.simpledifficulty.api.temperature.TemperatureRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber
public class ServerProxy implements IGuiHandler {
    public static final int GUI_EGG = 0;
    public static final int GUI_DIAL = 1;
    public static final int GUI_CREATURE_INVENTORY = 2;
    public static final int GUI_MENU_FROM_RADIAL = 3;
    public static final int GUI_WEAPON_INVENTORY = 4;
    public static final int GUI_JOURNAL = 5;
    public static final int GUI_FEEDING_TROUGH = 6;
    public static final int GUI_SEMI_MANUAL_EXTRACTOR = 7;
    public static final int GUI_SEMI_MANUAL_PRESSER = 8;
    public static final int GUI_SEMI_MANUAL_EXTRUDER = 9;
    public static final int GUI_SEMI_MANUAL_HAMMERER = 10;
    public static final int GUI_MILLSTONE = 11;
    public static final int GUI_MECHANICAL_FILTER = 12;
    public static final int GUI_CREATURE_BOX = 13;
    public static final int GUI_MENU_FROM_CREATURE_BOX = 14;

    public void preInit(FMLPreInitializationEvent e) {
        CapabilityManager.INSTANCE.register(IPlayerTamedCreatures.class, new PlayerTamedCreaturesStorage(), PlayerTamedCreatures::new);
        CapabilityManager.INSTANCE.register(IPlayerJournalProgress.class, new PlayerJournalProgressStorage(), PlayerJournalProgress::new);
        CapabilityManager.INSTANCE.register(INonPotionEffects.class, new NonPotionEffectsStorage(), NonPotionEffects::new);
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());

        RiftMessages.registerMessages();
        NetworkRegistry.INSTANCE.registerGuiHandler(RiftInitialize.instance, this);
        RiftFluids.registerFluids();
        RiftBlocks.registerBlocks();
        RiftItems.registerItems();
        RiftTileEntities.registerTileEntities();
        RiftItems.registerOreDicTags();
        RiftBlocks.registerOreDicTags();
        RiftRecipes.registerSmelting();
        if (GeneralConfig.canUseMM()) RiftMMRecipes.registerRecipes();
        RiftCrafttweaker.loadCrafttweakerCompat();
        MinecraftForge.EVENT_BUS.register(new RiftItems());
        MinecraftForge.EVENT_BUS.register(new RiftBlocks());
        RiftEntities.registerEntities();
        if (GeneralConfig.canUseSimpleDiff()) loadTemperatureRegistry();

        //load modded recipes
        if (GeneralConfig.canUseMM() && Loader.isModLoaded(RiftInitialize.HARVESTCRAFT_MOD_ID)) RiftMMRecipes.registerHarvestCraftRecipes();
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ServerEvents());
        MinecraftForge.EVENT_BUS.register(new RiftCreatureBoxBorder.RiftCreatureBorderHandler());
        MinecraftForge.EVENT_BUS.register(new RiftCreatureSpawning());
        GameRegistry.registerWorldGenerator(new RiftPlantGenerator(), 0);
        GameRegistry.registerWorldGenerator(new RiftStructureGenerator(), 0);
    }

    public void postInit(FMLPostInitializationEvent event) {}

    public void spawnParticle(String name, double x, double y, double z, double motX, double motY, double motZ) {}

    @Optional.Method(modid = RiftInitialize.SIMPLE_DIFFICULTY_MOD_ID)
    private void loadTemperatureRegistry() {
        TemperatureRegistry.registerModifier(new ModifierDimetrodon());
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        Entity entity = world.getEntityByID(x);
        TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
        if (id == GUI_CREATURE_INVENTORY) {
            if (entity instanceof RiftCreature) {
                return new CreatureContainer((RiftCreature) entity, player);
            }
        }
        else if (id == GUI_WEAPON_INVENTORY) {
            if (entity instanceof RiftLargeWeapon) {
                return new WeaponContainer((RiftLargeWeapon) entity, player);
            }
        }
        else if (id == GUI_FEEDING_TROUGH) {
            if (tileEntity instanceof RiftTileEntityFeedingTrough) {
                return new FeedingTroughContainer((RiftTileEntityFeedingTrough)tileEntity, player);
            }
        }
        else if (id == GUI_SEMI_MANUAL_EXTRACTOR) {
            if (tileEntity instanceof TileEntitySemiManualExtractor) {
                return new SemiManualExtractorContainer((TileEntitySemiManualExtractor)tileEntity, player);
            }
        }
        else if (id == GUI_SEMI_MANUAL_PRESSER) {
            if (tileEntity instanceof TileEntitySemiManualPresser) {
                return new SemiManualPresserContainer((TileEntitySemiManualPresser)tileEntity, player);
            }
        }
        else if (id == GUI_SEMI_MANUAL_EXTRUDER) {
            if (tileEntity instanceof TileEntitySemiManualExtruder) {
                return new SemiManualExtruderContainer((TileEntitySemiManualExtruder)tileEntity, player);
            }
        }
        else if (id == GUI_SEMI_MANUAL_HAMMERER) {
            if (tileEntity instanceof TileEntitySemiManualHammerer) {
                return new SemiManualHammererContainer((TileEntitySemiManualHammerer)tileEntity, player);
            }
        }
        else if (id == GUI_MILLSTONE) {
            if (tileEntity instanceof TileEntityMillstone) {
                return new MillstoneContainer((TileEntityMillstone)tileEntity, player);
            }
        }
        else if (id == GUI_MECHANICAL_FILTER) {
            if (tileEntity instanceof TileEntityMechanicalFilter) {
                return new MechanicalFilterContainer((TileEntityMechanicalFilter)tileEntity, player);
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    public int get3rdPersonView() {
        return 0;
    }

    public void set3rdPersonView(int view) {}

    public void setPreviousViewType(int view) {}

    public int getPreviousViewType() {
        return 0;
    }
}
