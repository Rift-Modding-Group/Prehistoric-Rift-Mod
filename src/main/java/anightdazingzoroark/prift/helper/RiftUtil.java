package anightdazingzoroark.prift.helper;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.enums.MobSize;
import anightdazingzoroark.prift.server.enums.EggTemperature;
import anightdazingzoroark.prift.server.message.RiftMessages;
import anightdazingzoroark.prift.server.message.RiftNewRemoveCreature;
import anightdazingzoroark.prift.server.message.RiftRemoveCreature;
import anightdazingzoroark.riftlib.hitboxLogic.EntityHitbox;
import com.teamderpy.shouldersurfing.client.ShoulderInstance;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RiftUtil {
    public static final double gravity = 0.08D;
    public static final UUID nilUUID = new UUID(0L, 0L);
    public static final int funnyNumber = 69420666;

    public static EntityLivingBase getEntityFromUUID(World world, UUID uuid) {
        if (world != null) {
            for (Entity entity : world.getLoadedEntityList()) {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                    if (entityLivingBase.getUniqueID().equals(uuid)) {
                        return entityLivingBase;
                    }
                }
            }
        }
        return null;
    }

    public static boolean checkForEntityWithUUID(World world, UUID uuid) {
        if (world != null) {
            for (Entity entity : world.getLoadedEntityList()) {
                if (entity instanceof EntityLivingBase) {
                    EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                    if (entityLivingBase.getUniqueID().equals(uuid)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static double highestWaterPos(RiftCreature creature) {
        double maxHeight = creature.world.getActualHeight() - creature.getPosition().getY();
        if (creature.isInWater()) {
            for (int i = 0; i <= maxHeight; i++) {
                BlockPos pos = creature.getPosition().add(0, i, 0);
                if (!creature.world.getBlockState(pos).getMaterial().isLiquid()) return creature.getPosition().getY() + i - 1;
            }
        }
        return 0D;
    }

    public static boolean isUsingSSR() {
        if (Loader.isModLoaded(RiftInitialize.SSR_MOD_ID)) {
            return ShoulderInstance.getInstance().doShoulderSurfing();
        }
        return false;
    }

    public static boolean isRidingBoat(EntityLivingBase entity) {
        return entity.isRiding() && (entity.getRidingEntity() instanceof EntityBoat);
    }

    public static boolean entityAtLocation(EntityLivingBase entityLivingBase, BlockPos pos, double radius) {
        return entityLivingBase.getEntityBoundingBox().grow(radius).contains(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));
    }

    public static boolean blockInOreDicType(Block block, String oreDicType) {
        NonNullList<ItemStack> blocksInOreDicType = OreDictionary.getOres(oreDicType);
        for (ItemStack item : blocksInOreDicType) {
            if (item.getItem().equals(new ItemStack(block).getItem())) return true;
        }
        return false;
    }

    public static boolean itemInOreDicType(Item item, String oreDicType) {
        List<Item> itemsInOreDicType = new ArrayList<>();
        for (ItemStack itemStack : OreDictionary.getOres(oreDicType)) {
            itemsInOreDicType.add(itemStack.getItem());
        }
        for (Item itemToFind : itemsInOreDicType) {
            if (itemToFind.equals(item)) return true;
        }
        return false;
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static boolean itemCanOverrideMoveControls(Item item) {
        return checkInMountItemWhitelist(item)
                || (GeneralConfig.mountConsumablesCanOverride && ((item instanceof ItemFood) || (item instanceof ItemPotion)));
    }

    public static boolean checkInMountItemWhitelist(Item item) {
        List<String> oreDicList = new ArrayList<>();
        List<String> itemList = new ArrayList<>();
        boolean flag = false;
        for (String entry : GeneralConfig.mountOverrideItems) {
            if (entry.contains("oreDic:")) {
                oreDicList.add(entry.replace("oreDic:", ""));
            }
            if (entry.contains("item:")) {
                itemList.add(entry.replace("item:", ""));
            }
        }
        for (String oreDicEntry : oreDicList) {
            if (!flag) flag = RiftUtil.itemInOreDicType(item, oreDicEntry);
        }
        for (String itemEntry : itemList) {
            int itemIdFirst = itemEntry.indexOf(":");
            int itemIdSecond = itemEntry.indexOf(":", itemIdFirst + 1);
            int itemData = Integer.parseInt(itemEntry.substring(itemIdSecond + 1));
            if (!flag) flag = Item.getByNameOrId(itemEntry.substring(0, itemIdSecond)).equals(item) && (itemData == -1 || itemData == new ItemStack(item).getMetadata());
        }
        return flag;
    }

    public static void drawTexturedModalRect(float x, float y, int texX, int texY, int width, int height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        double z = 0.0D;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, z).tex((texX * f), (texY + height) * f1).endVertex();
        bufferbuilder.pos((x + width), y + height, z).tex((texX + width) * f, (texY + height) * f1).endVertex();
        bufferbuilder.pos((x + width), y, z).tex((texX + width) * f,(texY * f1)).endVertex();
        bufferbuilder.pos(x, y, z).tex((texX * f), (texY * f1)).endVertex();
        tessellator.draw();
    }

    public static float setModelScale(RiftCreature creature, float min, float max) {
        return clamp(((max - min)/(24000)) * (creature.getAgeInTicks() - 24000f) + max, min, max);
    }

    public static int randomInRange(int min, int max) {
        return new Random().nextInt(max - min + 1) + min;
    }

    public static double randomInRange(double min, double max) {
        return min + new Random().nextDouble() * (max - min);
    }

    public static EntityLivingBase findClosestEntity(EntityLivingBase baseEntity, List<EntityLivingBase> entities) {
        if (baseEntity == null || entities == null || entities.isEmpty()) return null;

        Optional<EntityLivingBase> closest = entities.stream()
                .min((entity1, entity2) -> {
                    double dist1 = baseEntity.getDistanceSq(entity1);
                    double dist2 = baseEntity.getDistanceSq(entity2);
                    return Double.compare(dist1, dist2);
                });

        return closest.orElse(null);
    }

    public static EggTemperature getCorrespondingTempFromBiome(World world, BlockPos blockPos) {
        float temperature = world.getBiome(blockPos).getTemperature(blockPos);
        if (temperature < 0f) return EggTemperature.VERY_COLD;
        else if (temperature >= 0f && temperature < 0.5f) return EggTemperature.COLD;
        else if (temperature >= 0.5f && temperature < 1f) return EggTemperature.NEUTRAL;
        else if (temperature >= 1f && temperature < 1.5f) return EggTemperature.WARM;
        else if (temperature >= 1.5f) return EggTemperature.VERY_WARM;
        else return EggTemperature.NEUTRAL;
    }

    public static boolean biomeTagMatchFromList(List<String> list, Biome biome) {
        for (String tag : list) {
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(tag))) return true;
        }
        return false;
    }

    public static List<String> creatureTargets(List<String> whiteList) {
        return creatureTargets(whiteList, new ArrayList<>(), false);
    }

    public static List<String> creatureTargets(List<String> whiteList, List<String> blackList, boolean useCUniversal) {
        List<String> finalTargets = new ArrayList<>();
        List<String> baseTargetList = new ArrayList<>(Arrays.asList(GeneralConfig.universalCarnivoreTargets));
        if (useCUniversal) {
            baseTargetList.removeIf(blackList::contains);
            finalTargets = Stream.concat(whiteList.stream(), baseTargetList.stream()).collect(Collectors.toList());
        }
        else return whiteList;
        return finalTargets;
    }

    public static MobSize getMobSize(Entity entity) {
        List<String> verySmallSize = Arrays.asList(GeneralConfig.verySmallMobs);
        List<String> smallSize = Arrays.asList(GeneralConfig.smallMobs);
        List<String> mediumSize = Arrays.asList(GeneralConfig.mediumMobs);
        List<String> largeSize = Arrays.asList(GeneralConfig.largeMobs);
        List<String> veryLargeSize = Arrays.asList(GeneralConfig.veryLargeMobs);

        if (entity instanceof EntityPlayer) {
            if (verySmallSize.contains("minecraft:player")) return MobSize.VERY_SMALL;
            else if (smallSize.contains("minecraft:player")) return MobSize.SMALL;
            else if (mediumSize.contains("minecraft:player")) return MobSize.MEDIUM;
            else if (largeSize.contains("minecraft:player")) return MobSize.LARGE;
            else if (veryLargeSize.contains("minecraft:player")) return MobSize.VERY_LARGE;
        }
        else {
            String mobString = EntityList.getKey(entity).toString();
            if (verySmallSize.contains(mobString)) return MobSize.VERY_SMALL;
            else if (smallSize.contains(mobString)) return MobSize.SMALL;
            else if (mediumSize.contains(mobString)) return MobSize.MEDIUM;
            else if (largeSize.contains(mobString)) return MobSize.LARGE;
            else if (veryLargeSize.contains(mobString)) return MobSize.VERY_LARGE;
        }

        return MobSize.MEDIUM;
    }

    public static boolean isAppropriateSize(Entity entity, MobSize size) {
        //if hitbox is hit, perform recursion involving the parent
        if (entity instanceof MultiPartEntityPart) {
            Entity parent = (Entity) ((MultiPartEntityPart) entity).parent;
            return isAppropriateSize(parent, size);
        }
        if (size != null) return getMobSize(entity).ordinal() <= size.ordinal();
        else return true;
    }

    public static boolean isAppropriateSizeNotEqual(Entity entity, MobSize size) {
        //if hitbox is hit, perform recursion involving the parent
        if (entity instanceof MultiPartEntityPart) {
            Entity parent = (Entity) ((MultiPartEntityPart) entity).parent;
            return isAppropriateSize(parent, size);
        }
        if (size != null) return getMobSize(entity).ordinal() < size.ordinal();
        else return true;
    }

    public static void drawCenteredString(FontRenderer fontRenderer, String string, int guiWidth, int guiHeight, int xOff, int yOff, int textColor) {
        int wrapWidth = fontRenderer.getStringWidth(string);
        fontRenderer.drawString(string, (guiWidth - wrapWidth)/2 + xOff, (guiHeight - fontRenderer.FONT_HEIGHT)/2 - yOff, textColor);
    }

    public static void drawMultiLineString(FontRenderer fontRenderer, String string, int xPos, int yPos, int wrapWidth, int textColor) {
        List<String> lines = fontRenderer.listFormattedStringToWidth(string, wrapWidth);
        int currentY = yPos;

        for (String line : lines) {
            int lineWidth = fontRenderer.getStringWidth(line);
            int lineX = xPos + (wrapWidth - lineWidth) / 2;
            fontRenderer.drawString(line, lineX, currentY, textColor);
            currentY += fontRenderer.FONT_HEIGHT;
        }
    }

    public static boolean entityIsUnderwater(EntityLivingBase entityLivingBase) {
        BlockPos highestWaterPos = entityLivingBase.getPosition().add(0, Math.ceil(entityLivingBase.height), 0);
        return entityLivingBase.world.getBlockState(highestWaterPos).getMaterial() == Material.WATER;
    }

    public static ItemStack fillBucketWithFluid(ItemStack emptyBucket, Fluid fluid) {
        if (emptyBucket.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            FluidStack fluidStack = new FluidStack(fluid, Fluid.BUCKET_VOLUME);
            IFluidHandlerItem fluidHandler = emptyBucket.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (fluidHandler != null) {
                int filled = fluidHandler.fill(fluidStack, true);
                if (filled == Fluid.BUCKET_VOLUME) {
                    return fluidHandler.getContainer().copy();
                }
            }
        }
        return emptyBucket;
    }

    public static boolean itemStackEqualToString(ItemStack itemStack, String string) {
        int colonOne = string.indexOf(":");
        int colonTwo = string.indexOf(":", colonOne + 1);
        if (colonTwo != -1) {
            Item item = Item.getByNameOrId(string.substring(0, colonTwo));
            int metadata = Integer.parseInt(string.substring(colonTwo + 1));
            return itemStack.getItem().equals(item) && (itemStack.getMetadata() == metadata || metadata == -1);

        }
        else {
            Item item = Item.getByNameOrId(string);
            return itemStack.getItem().equals(item) && itemStack.getMetadata() == 0;
        }
    }

    public static ItemStack getItemStackFromString(String string) {
        int colonOne = string.indexOf(":");
        int colonTwo = string.indexOf(":", colonOne + 1);
        if (colonTwo != -1) {
            Item item = Item.getByNameOrId(string.substring(0, colonTwo));
            int metadata = Integer.parseInt(string.substring(colonTwo + 1));
            return new ItemStack(item, 1, metadata);

        }
        else {
            Item item = Item.getByNameOrId(string);
            return new ItemStack(item);
        }
    }

    public static boolean blockstateEqualToString(IBlockState state, String string) {
        int colData = string.indexOf(":", string.indexOf(":") + 1);
        int blockData = Integer.parseInt(string.substring(colData + 1));
        String blockName = string.substring(0, colData);
        return Block.getBlockFromName(blockName) == state.getBlock() && (blockData == - 1 || state.getBlock().getMetaFromState(state) == blockData);
    }

    public static String getStringIdFromBlock(Block block) {
        ResourceLocation registryName = ForgeRegistries.BLOCKS.getKey(block);
        if (registryName != null) return registryName.toString();
        return "";
    }

    public static boolean checkForNoAssociations(RiftCreature user, Entity target) {
        if (user == null || target == null) return true;

        //if user and target are somehow the same, return false, as by logic they're associated
        //with each other
        if (user.equals(target)) return false;

        if (target instanceof EntityLivingBase) {
            EntityLivingBase entityLivingBase = (EntityLivingBase)target;
            if (entityLivingBase instanceof EntityPlayer) return !user.isOwner(entityLivingBase);
            else if (entityLivingBase instanceof EntityTameable) {
                EntityTameable tameable = (EntityTameable) entityLivingBase;
                if (tameable.isTamed()) {
                    if (user.getOwner() != null) return !tameable.isOwner(user.getOwner());
                }
                else return true;
            }
        }
        //if target is a hitbox, get parent and call function recursively but this time w the parent
        else if (target instanceof MultiPartEntityPart) {
            Entity parentOfPart = (Entity) ((MultiPartEntityPart)target).parent;
            return checkForNoAssociations(user, parentOfPart);
        }
        //target being anything else returns true, as by logic they're not associated with user
        return true;
    }

    public static boolean checkForNoHerdAssociations(RiftCreature user, Entity target) {
        //if the user herder isn't a herder or doesn't exist, or if target doesnt exist, return true
        if (user == null || !user.canDoHerding() || target == null) return true;

        //if user and target are somehow the same, return false, as by logic they're associated
        //with each other
        if (user.equals(target)) return false;

        if (target instanceof RiftCreature) {
            RiftCreature targetCreature = (RiftCreature) target;

            //if both herders have no leaders, return true
            if (user.getHerdLeader() == null || targetCreature.getHerdLeader() == null) return true;

            //if herders have same leader, return false, else true
            return !user.getHerdLeader().equals(targetCreature.getHerdLeader());
        }
        //if target is a hitbox, get parent and call function recursively but this time w the parent
        else if (target instanceof MultiPartEntityPart) {
            Entity parentOfPart = (Entity) ((MultiPartEntityPart)target).parent;
            return checkForNoHerdAssociations(user, parentOfPart);
        }
        //target being anything else returns true, as by logic they're not associated with user
        else return true;
    }

    public static <T> List<T> uniteTwoLists(List<T> listOne, List<T> listTwo) {
        if (listTwo != null && !listTwo.isEmpty()) listOne.addAll(listTwo);
        return listOne;
    }

    public static double getDistNoHeight(BlockPos pos1, BlockPos pos2) {
        double xDiff = pos1.getX() - pos2.getX();
        double zDiff = pos1.getZ() - pos2.getZ();
        return Math.sqrt(xDiff * xDiff + zDiff * zDiff);
    }

    public static boolean posInBiomeListString(List<String> biomeStringList, World world, BlockPos pos) {
        //turn data from string list into array
        List<Biome> biomeList = new ArrayList<>();
        for (String biomeEntry : biomeStringList) {
            if (biomeEntry.charAt(0) != '-') {
                int partOne = biomeEntry.indexOf(":");
                String spawnerType = partOne != -1 ? biomeEntry.substring(0, partOne) : biomeEntry;
                String entry = biomeEntry.substring(partOne + 1);
                if (spawnerType.equals("biome")) {
                    for (Biome biome : Biome.REGISTRY) {
                        if (biome.getRegistryName().toString().equals(entry)) {
                            biomeList.add(biome);
                        }
                    }
                }
                else if (spawnerType.equals("tag")) {
                    for (Biome biome : Biome.REGISTRY) {
                        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(entry))) {
                            biomeList.add(biome);
                        }
                    }
                }
                else if (spawnerType.equals("all")) {
                    for (Biome biome : Biome.REGISTRY) {
                        biomeList.add(biome);
                    }
                }
            }
            else {
                int partOne = biomeEntry.indexOf(":");
                String spawnerType = biomeEntry.substring(1, partOne);
                String entry = biomeEntry.substring(partOne + 1);
                if (spawnerType.equals("biome")) {
                    for (Biome biome : Biome.REGISTRY) {
                        if (biome.getRegistryName().equals(entry) && biomeList.contains(biome)) {
                            biomeList.remove(biome);
                        }
                    }
                }
                else if (spawnerType.equals("tag")) {
                    for (Biome biome : Biome.REGISTRY) {
                        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.getType(entry)) && biomeList.contains(biome)) {
                            biomeList.remove(biome);
                        }
                    }
                }
            }
        }
        for (Biome biomeToSpawn : biomeList) {
            if (world.getBiome(pos).equals(biomeToSpawn)) return true;
        }
        return false;
    }

    public static void removeCreature(RiftCreature creature) {
        if (creature == null) return;
        if (creature.getParts() == null) return;
        RiftCreature subCreature = creature;
        World world = creature.world;

        if (world.isRemote) {
            //remove creature
            world.removeEntityDangerously(creature);
            RiftMessages.WRAPPER.sendToServer(new RiftRemoveCreature(creature, true));

            //remove hitboxes
            for (Entity part : subCreature.getParts()) {
                EntityHitbox creaturePart = (EntityHitbox) part;

                if (creaturePart != null) {
                    world.removeEntityDangerously(creaturePart);
                    RiftMessages.WRAPPER.sendToServer(new RiftRemoveCreature(creaturePart, false));
                }
            }
        }
        else {
            //remove creature
            world.removeEntityDangerously(creature);
            RiftMessages.WRAPPER.sendToAll(new RiftRemoveCreature(creature, true));

            //remove hitboxes
            for (Entity part : subCreature.getParts()) {
                EntityHitbox creaturePart = (EntityHitbox) part;

                if (creaturePart != null) {
                    RiftMessages.WRAPPER.sendToAll(new RiftRemoveCreature(creaturePart, false));
                    world.removeEntityDangerously(creaturePart);
                }
            }
        }
    }

    public static <K, V> List<K> getKeysByValue(Map<K, V> map, V targetValue) {
        List<K> matchingKeys = new ArrayList<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (targetValue.equals(entry.getValue())) matchingKeys.add(entry.getKey());
        }
        return matchingKeys;
    }

    public static float getCreatureModelScale(RiftCreature creature) {
        switch (creature.creatureType) {
            case TYRANNOSAURUS:
                return 20f;
            case STEGOSAURUS:
                return 20f;
            case TRICERATOPS:
                return 25f;
            case UTAHRAPTOR:
                return 30f;
            case APATOSAURUS:
                return 20f;
            case PARASAUROLOPHUS:
                return 25f;
            case DIMETRODON:
                return 30f;
            case SARCOSUCHUS:
                return 30f;
            case ANOMALOCARIS:
                return 30f;
            case SAUROPHAGANAX:
                return 25f;
            case DIREWOLF:
                return 30f;
            case MEGALOCEROS:
                return 25f;
            case BARYONYX:
                return 20f;
            case PALAEOCASTOR:
                return 50f;
            case ANKYLOSAURUS:
                return 20f;
            case DILOPHOSAURUS:
                return 30f;
            case GALLIMIMUS:
                return 30f;
        }
        return 1f;
    }

    public static double slopeResult(int x, boolean clamped, double xMin, double xMax, double yMin, double yMax) {
        double slope = (yMax - yMin)/(xMax - xMin);
        if (clamped) {
            if (yMin <= yMax) return clamp(slope * (x - xMin) + yMin, yMin, yMax);
            else return clamp(slope * (x - xMin) + yMin, yMax, yMin);
        }
        return slope * (x - xMin) + yMin;
    }

    public static float slopeResult(int x, boolean clamped, float xMin, float xMax, float yMin, float yMax) {
        float slope = (yMax - yMin)/(xMax - xMin);
        if (clamped) {
            if (yMin <= yMax) return clamp(slope * (x - xMin) + yMin, yMin, yMax);
            else return clamp(slope * (x - xMin) + yMin, yMax, yMin);
        }
        return slope * (x - xMin) + yMin;
    }

    public static boolean hasPotionEffect(EntityLivingBase entity, Potion potion) {
        if (entity == null || potion == null) {
            return false;
        }

        for (PotionEffect effect : entity.getActivePotionEffects()) {
            if (effect.getPotion() == potion) {
                return true;
            }
        }

        return false;
    }

    public static <T> T getRandomFromList(List<T> list, Predicate<T> predicate) {
        if (list.isEmpty()) return null;

        if (predicate != null) {
            list = list.stream().filter(predicate).collect(Collectors.toList());
            if (list.isEmpty()) return null;
        }

        int listSize = list.size();
        return list.get(new Random().nextInt(listSize));
    }
}
