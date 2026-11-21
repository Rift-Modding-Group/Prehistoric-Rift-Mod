package anightdazingzoroark.prift;

import anightdazingzoroark.prift.server.block.PlatformFloorBlock;
import anightdazingzoroark.prift.server.config.RiftDebugFlags;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.server.item.RiftItems;
import mcp.mobius.waila.api.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import valkyrienwarfare.api.RiftedValkyriumAPI;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

@WailaPlugin(RiftInitialize.MODID)
public class RiftHWYLA implements IWailaPlugin {
    @Override
    public void register(IWailaRegistrar registrar) {
        registrar.registerStackProvider(
                new IWailaDataProvider() {
                    @Nonnull
                    @Override
                    public ItemStack getWailaStack(@Nonnull IWailaDataAccessor accessor, @Nonnull IWailaConfigHandler config) {
                        if (!Loader.isModLoaded("valkyrienskies")) return ItemStack.EMPTY;

                        if (!(accessor.getPlayer().getRidingEntity() instanceof RiftCreature creature) || !creature.supportsPlatformSaddle() || !creature.isPlatformEquipped()) {
                            return new ItemStack(RiftItems.CREATURE_PLATFORM);
                        }

                        UUID shipId = creature.getPlatformShipId();
                        if (shipId == null) return ItemStack.EMPTY;

                        if (RiftedValkyriumAPI.getShipManagingBlock(accessor.getWorld(), accessor.getPosition()).filter(ship -> ship.getUuid().equals(shipId)).isPresent()) {
                            return ItemStack.EMPTY;
                        }

                        return new ItemStack(RiftItems.CREATURE_PLATFORM);
                    }
                },
                PlatformFloorBlock.class
        );
        registrar.registerBodyProvider(
                new IWailaEntityProvider() {
                    @Nonnull
                    @Override
                    public List<String> getWailaBody(Entity entity, List<String> currenttip, IWailaEntityAccessor accessor, IWailaConfigHandler config) {
                        RiftCreature creature = (RiftCreature) entity;
                        if (RiftDebugFlags.showStaminaInHWYLA) {
                            currenttip.add("Stamina: " + String.format("%.1f", creature.getStamina()) + "/" + creature.getMaxStamina());
                        }

                        //creature owner should go first
                        if (creature.getOwner() != null) {
                            currenttip.addFirst(I18n.format("info.owner_name", creature.getOwner().getName()));
                        }

                        return currenttip;
                    }
                },
                RiftCreature.class
        );
    }
}
