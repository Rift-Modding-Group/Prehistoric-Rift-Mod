package anightdazingzoroark.prift.server.message;

import anightdazingzoroark.prift.config.GeneralConfig;
import anightdazingzoroark.prift.config.RiftConfigHandler;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.IPlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreatures;
import anightdazingzoroark.prift.server.capabilities.playerTamedCreatures.PlayerTamedCreaturesProvider;
import anightdazingzoroark.prift.server.entity.RiftCreatureType;
import anightdazingzoroark.prift.server.entity.creatureMoves.CreatureMove;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class RiftUpdatePartyAfterOpenPartyScreen implements IMessage {
    private int playerId;
    private int lastOpenedTime;

    public RiftUpdatePartyAfterOpenPartyScreen() {}

    public RiftUpdatePartyAfterOpenPartyScreen(EntityPlayer player, int lastOpenedTime) {
        this.playerId = player.getEntityId();
        this.lastOpenedTime = lastOpenedTime;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerId = buf.readInt();
        this.lastOpenedTime = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.playerId);
        buf.writeInt(this.lastOpenedTime);
    }

    public static class Handler implements IMessageHandler<RiftUpdatePartyAfterOpenPartyScreen, IMessage> {
        @Override
        public IMessage onMessage(RiftUpdatePartyAfterOpenPartyScreen message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(RiftUpdatePartyAfterOpenPartyScreen message, MessageContext ctx) {
            if (ctx.side == Side.SERVER) {
                EntityPlayer messagePlayer = ctx.getServerHandler().player;

                EntityPlayer player = (EntityPlayer) messagePlayer.world.getEntityByID(message.playerId);
                IPlayerTamedCreatures playerTamedCreatures = player.getCapability(PlayerTamedCreaturesProvider.PLAYER_TAMED_CREATURES_CAPABILITY, null);

                if (playerTamedCreatures != null) {
                    int timeToSubtract = message.lastOpenedTime - playerTamedCreatures.getPartyLastOpenedTime();
                    for (int x = 0; x < playerTamedCreatures.getPartyNBT().size(); x++) {
                        NBTTagCompound partyMemNBT = playerTamedCreatures.getPartyNBT().get(x);

                        if (partyMemNBT.isEmpty()) continue; //ignore any empty spaces

                        RiftCreatureType creatureType = RiftCreatureType.values()[partyMemNBT.getByte("CreatureType")];
                        PlayerTamedCreatures.DeploymentType deploymentType = PlayerTamedCreatures.DeploymentType.values()[partyMemNBT.getByte("DeploymentType")];

                        //only update those that are not deployed
                        if (deploymentType == PlayerTamedCreatures.DeploymentType.PARTY) continue;

                        float newHealthLevel = partyMemNBT.getFloat("Health");
                        int newEnergyLevel = partyMemNBT.getInteger("Energy");

                        NBTTagList partyMemInvNBT = partyMemNBT.getTagList("Items", 10);

                        //get health
                        float creatureHealth = partyMemNBT.getFloat("Health");

                        //if creature is dead (no hp), skip
                        if (creatureHealth <= 0) continue;

                        //get max health
                        float creatureMaxHealth = creatureHealth;
                        for (NBTBase nbtBase: partyMemNBT.getTagList("Attributes", 10).tagList) {
                            if (nbtBase instanceof NBTTagCompound) {
                                NBTTagCompound tagCompound = (NBTTagCompound) nbtBase;

                                if (!tagCompound.hasKey("Name") || !tagCompound.getString("Name").equals("generic.maxHealth")) continue;

                                creatureMaxHealth = (float) tagCompound.getDouble("Base");
                            }
                        }

                        //now regen health based on how much time had passed
                        //and food items in its inventory that it may have eaten
                        if (creatureHealth < creatureMaxHealth) {
                            for (int j = 0; j < timeToSubtract; j++) {
                                //natural regen
                                if (GeneralConfig.naturalCreatureRegen && j % 100 == 0) {
                                    newHealthLevel += 2f;
                                    if (newHealthLevel >= creatureMaxHealth) break;
                                }

                                //food based regen
                                if (GeneralConfig.creatureEatFromInventory && j % 60 == 0) {
                                    //manipulate based on item edibility
                                    for (int i = partyMemInvNBT.tagCount() - 1; i >= 0; i--) {
                                        NBTTagCompound itemNBT = (NBTTagCompound) partyMemInvNBT.get(i);

                                        //skip if slot is at 0, which is reserved for saddles only
                                        if (creatureType.canBeSaddled && itemNBT.getByte("Slot") == 0) continue;

                                        ItemStack itemStack = new ItemStack(itemNBT);
                                        if (creatureType.isFavoriteFood(itemStack) && !creatureType.isEnergyRegenItem(itemStack)) {
                                            newHealthLevel += creatureType.getFavoriteFoodHeal(new ItemStack(itemNBT), creatureMaxHealth);
                                            itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                                            break;
                                        }
                                    }
                                    if (newHealthLevel >= creatureMaxHealth) break;
                                }
                            }
                        }

                        //energy regen
                        //first get energy and max energy
                        int energy = partyMemNBT.getInteger("Energy");
                        int maxEnergy = RiftConfigHandler.getConfig(creatureType).stats.maxEnergy;

                        //now regen energy based on how much time had passed
                        //and food items in its inventory that it may have eaten
                        if (energy < maxEnergy) {
                            for (int j = 0; j < timeToSubtract; j++) {
                                //natural regen
                                if (j % creatureType.energyRechargeSpeed() == 0) {
                                    newEnergyLevel += 1;
                                    if (newEnergyLevel >= maxEnergy) break;
                                }

                                //food based regen
                                if (GeneralConfig.creatureEatFromInventory && j % 60 == 0) {
                                    //manipulate based on item edibility
                                    for (int i = partyMemInvNBT.tagCount() - 1; i >= 0; i--) {
                                        NBTTagCompound itemNBT = (NBTTagCompound) partyMemInvNBT.get(i);

                                        //skip if slot is at 0, which is reserved for saddles only
                                        if (creatureType.canBeSaddled && itemNBT.getByte("Slot") == 0) continue;

                                        ItemStack itemStack = new ItemStack(itemNBT);
                                        if (creatureType.isEnergyRegenItem(itemStack)) {
                                            newEnergyLevel += creatureType.getEnergyRegenItemValue(new ItemStack(itemNBT));
                                            itemNBT.setByte("Count", (byte) Math.max(0, itemNBT.getByte("Count") - 1));
                                            break;
                                        }
                                    }
                                    if (newEnergyLevel >= maxEnergy) break;
                                }
                            }
                        }

                        //move cooldowns
                        NBTTagList movesList = partyMemNBT.getTagList("LearnedMoves", 10);
                        for (int i = 0; i < movesList.tagCount(); i++) {
                            //get cooldown for move
                            int moveCooldown = 0;
                            switch (i) {
                                case 0:
                                    moveCooldown = partyMemNBT.getInteger("CooldownMoveOne");
                                    break;
                                case 1:
                                    moveCooldown = partyMemNBT.getInteger("CooldownMoveTwo");
                                    break;
                                case 2:
                                    moveCooldown = partyMemNBT.getInteger("CooldownMoveThree");
                                    break;
                            }

                            if (moveCooldown > 0) {
                                //now change cooldown depending on time
                                moveCooldown = Math.max(moveCooldown - timeToSubtract, 0);

                                //update nbt for move cooldown after this
                                switch (i) {
                                    case 0:
                                        partyMemNBT.setInteger("CooldownMoveOne", moveCooldown);
                                        break;
                                    case 1:
                                        partyMemNBT.setInteger("CooldownMoveTwo", moveCooldown);
                                        break;
                                    case 2:
                                        partyMemNBT.setInteger("CooldownMoveThree", moveCooldown);
                                        break;
                                }
                            }
                        }

                        //update nbt for health and energy and items after all this
                        partyMemNBT.setTag("Items", partyMemInvNBT);
                        partyMemNBT.setFloat("Health", Math.min(newHealthLevel, creatureMaxHealth));
                        partyMemNBT.setInteger("Energy", Math.min(newEnergyLevel, maxEnergy));
                        playerTamedCreatures.setPartyMemNBT(x, partyMemNBT);
                    }
                    playerTamedCreatures.setPartyLastOpenedTime(message.lastOpenedTime);
                }
            }
        }
    }
}