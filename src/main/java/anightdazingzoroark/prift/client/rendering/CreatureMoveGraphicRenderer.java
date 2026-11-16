package anightdazingzoroark.prift.client.rendering;

import anightdazingzoroark.prift.RiftInitialize;
import anightdazingzoroark.prift.api.creature.CreatureMoveGraphic;
import anightdazingzoroark.prift.api.creature.builder.CreatureMoveChargeupBuilder.ChargeupPhase;
import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.core.builder.Animation;
import anightdazingzoroark.riftlib.core.builder.LoopType;
import anightdazingzoroark.riftlib.model.provider.GeoModelProvider;
import anightdazingzoroark.riftlib.renderers.geo.IGeoRenderer;
import anightdazingzoroark.riftlib.resource.client.RiftLibCacheClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Draws move graphics directly in world space, without creating game entities.
 * */
@SideOnly(Side.CLIENT)
public class CreatureMoveGraphicRenderer implements IGeoRenderer<CreatureMoveGraphic> {
    @NotNull
    private static final GeoModelProvider<CreatureMoveGraphic> modelProvider = new GeoModelProvider<>() {
        @Override
        @NotNull
        public String getModId() {
            return RiftInitialize.MODID;
        }

        @Override
        @NotNull
        public String getModelIdentifier(@NotNull CreatureMoveGraphic graphic) {
            return "geometry." + graphic.name();
        }

        @Override
        @NotNull
        public String getTextureLocation(@NotNull CreatureMoveGraphic graphic) {
            return "entities/move_graphics/" + graphic.name() + ".png";
        }
    };

    @NotNull
    private static final List<MoveGraphicAnimation> activeGraphics = new ArrayList<>();

    public static void addGraphic(@NotNull World world, @NotNull CreatureMoveGraphic.Playback playback) {
        GeoModel model = modelProvider.getModel(modelProvider.getModelIdentifier(playback.graphic()));
        if (model != null) activeGraphics.add(new MoveGraphicAnimation(world, playback, model));
    }

    @SubscribeEvent
    public void onWorldUnload(@NotNull WorldEvent.Unload event) {
        if (event.getWorld().isRemote) activeGraphics.removeIf(graphic -> graphic.world == event.getWorld());
    }

    @SubscribeEvent
    public void onRenderWorldLast(@NotNull RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null) {
            activeGraphics.clear();
            return;
        }

        if (minecraft.getRenderViewEntity() == null) return;

        float partialTicks = event.getPartialTicks();
        RenderManager renderManager = minecraft.getRenderManager();
        ListIterator<MoveGraphicAnimation> graphics = activeGraphics.listIterator();
        while (graphics.hasNext()) {
            MoveGraphicAnimation visual = graphics.next();
            if (visual.world != minecraft.world) {
                graphics.remove();
                continue;
            }
            CreatureMoveGraphic.Playback playback = visual.playback;
            CreatureMoveGraphic graphic = playback.graphic();

            double age = minecraft.world.getTotalWorldTime() - playback.startTime() + partialTicks;
            if (age < 0D) continue;
            GeoModel model = modelProvider.getModel(modelProvider.getModelIdentifier(graphic));
            Animation animation = RiftLibCacheClient.getInstance().getAnimations().getOrDefault(RiftInitialize.MODID, Map.of()).get(graphic.animation());
            if (model == null || animation == null || animation.animationLength <= 0D
                    || animation.loop == LoopType.PLAY_ONCE && age >= animation.animationLength
            ) {
                graphics.remove();
                continue;
            }
            double tick = Math.min(age, animation.animationLength);
            if (animation.loop == LoopType.LOOP) tick = age % animation.animationLength;
            if (visual.template != model) {
                MoveGraphicAnimation reloaded = new MoveGraphicAnimation(minecraft.world, playback, model);
                reloaded.attached = visual.attached;
                visual = reloaded;
                graphics.set(reloaded);
            }
            Vec3d origin = playback.origin();
            if (graphic.locator() != null) {
                if (!(minecraft.world.getEntityByID(playback.sourceEntityId()) instanceof RiftCreature source)
                        || !source.isEntityAlive()) {
                    graphics.remove();
                    continue;
                }
                if (source.getCreatureMoves().getCurrentMoveUseId() != playback.moveUseId()
                        || !source.getCreatureMoves().currentMoveMatches(source.getCurrentMove(), ChargeupPhase.RELEASING)) {
                    // The graphic packet can arrive just before the move's synced release state.
                    if (!visual.attached && age < 5D) continue;
                    graphics.remove();
                    continue;
                }
                visual.attached = true;
                origin = source.getLocatorWorldPos(graphic.locator()).add(
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, source.lastTickPosX, source.posX) - source.posX,
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, source.lastTickPosY, source.posY) - source.posY,
                        MathUtil.slopeResult(partialTicks, true, 0D, 1D, source.lastTickPosZ, source.posZ) - source.posZ
                );
                double yaw = Math.toRadians(source.rotationYaw);
                visual.getAnimationData().setVariable("variable.graphic_target_x", Math.sin(yaw));
                visual.getAnimationData().setVariable("variable.graphic_target_y", 0D);
                visual.getAnimationData().setVariable("variable.graphic_target_z", Math.cos(yaw));
                visual.getAnimationData().setVariable("variable.graphic_scale", source.scale());
            }
            visual.sample(animation, tick);

            boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
            boolean culling = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

            GlStateManager.pushMatrix();
            GlStateManager.translate(origin.x - renderManager.viewerPosX, origin.y - renderManager.viewerPosY, origin.z - renderManager.viewerPosZ);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            minecraft.getTextureManager().bindTexture(this.getTextureLocation(graphic));
            this.renderModel(visual.model, 1f, 1f, 1f, 1f);
            GlStateManager.bindTexture(texture);
            if (lighting) GlStateManager.enableLighting();
            if (culling) GlStateManager.enableCull();
            GlStateManager.popMatrix();
        }
    }

    @Override
    @NotNull
    public GeoModelProvider<CreatureMoveGraphic> getGeoModelProvider() {
        return modelProvider;
    }
}
