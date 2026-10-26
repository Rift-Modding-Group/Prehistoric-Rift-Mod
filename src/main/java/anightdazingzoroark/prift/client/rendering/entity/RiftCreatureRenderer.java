package anightdazingzoroark.prift.client.rendering.entity;

import anightdazingzoroark.prift.api.util.MathUtil;
import anightdazingzoroark.prift.client.rendering.block.CreatureBoxRangeRenderer;
import anightdazingzoroark.prift.server.entity.creatureMoves.aiming.RiddenAimArea;
import anightdazingzoroark.riftlib.geo.GeoBone;
import anightdazingzoroark.riftlib.geo.GeoCube;
import anightdazingzoroark.riftlib.geo.GeoQuad;
import anightdazingzoroark.riftlib.geo.GeoVertex;
import anightdazingzoroark.riftlib.util.MatrixStack;
import anightdazingzoroark.prift.server.entity.creature.RiftCreature;
import anightdazingzoroark.prift.client.CameraHandler;
import anightdazingzoroark.prift.server.entity.model.CreatureModel;
import anightdazingzoroark.riftlib.geo.GeoModel;
import anightdazingzoroark.riftlib.renderers.geo.GeoEntityLivingRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;
import org.lwjgl.BufferUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * This renderer is for rendering stuff on the client for the common creature model.
 * */
public class RiftCreatureRenderer extends GeoEntityLivingRenderer<RiftCreature> {
    public static float AIM_TRANSLUCENT_OPACITY = 0.5f;

    @NotNull
    private FadeState aimVisibility = new FadeState(false, 1f);
    @Nullable
    private DepthPass aimDepthPass;
    private int fadingCreatureId = -1;
    private long lastVisibilityFrame;
    private boolean renderingTranslucentAim;

    public RiftCreatureRenderer(RenderManager renderManager) {
        super(renderManager, new CreatureModel());
    }

    @Override
    public void doRender(@NotNull RiftCreature creature, double x, double y, double z, float entityYaw, float partialTicks) {
        // RiftLibrary draws the model itself, so enable vanilla's solid outline color around that draw.
        boolean outlines = this.renderOutlines;
        if (outlines) {
            int color = this.getTeamColor(creature);
            if (CreatureBoxRangeRenderer.isHighlighted(creature)) color = 0xFFFF00;
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(color);
        }
        super.doRender(creature, x, y, z, entityYaw, partialTicks);
        if (outlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
    }

    @Override
    protected boolean setDoRenderBrightness(@NotNull RiftCreature creature, float partialTicks) {
        return !this.renderOutlines && super.setDoRenderBrightness(creature, partialTicks);
    }

    @Override
    public void render(@NotNull GeoModel model, @NotNull RiftCreature animatable, float partialTicks, float red, float green, float blue, float alpha) {
        //-----hide saddle bones-----
        boolean saddled = animatable.isSaddled();
        if (model.getAllBones().get("saddle") != null) model.getAllBones().get("saddle").setHidden(!saddled);
        if (model.getAllBones().get("headSaddle") != null) model.getAllBones().get("headSaddle").setHidden(!saddled);
        if (model.getAllBones().get("hiddenBySaddle") != null) model.getAllBones().get("hiddenBySaddle").setHidden(saddled);

        //-----manage opacity when the wide crosshair appears over the model-----
        float modelOpacity = 1f;
        if (!animatable.getCurrentMove().isEmpty()) {
            if (this.fadingCreatureId == animatable.getEntityId()) {
                this.fadingCreatureId = -1;
                this.aimVisibility = new FadeState(false, 1f);
            }
        }
        else if (CameraHandler.isRidingJawAim(animatable, Minecraft.getMinecraft().player)) {
            long now = System.nanoTime();
            if (this.fadingCreatureId != animatable.getEntityId()) {
                this.aimVisibility = new FadeState(false, 1f);
                this.lastVisibilityFrame = now - 16_666_667L;
                this.fadingCreatureId = animatable.getEntityId();
            }
            FloatBuffer values = BufferUtils.createFloatBuffer(16);
            float[] matrixValues = new float[16];
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, values);
            values.get(matrixValues);
            Matrix4f modelView = new Matrix4f(matrixValues);
            modelView.transpose();
            values.clear();
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, values);
            values.get(matrixValues);
            Matrix4f modelToClip = new Matrix4f(matrixValues);
            modelToClip.transpose();
            modelToClip.mul(modelView);
            Minecraft minecraft = Minecraft.getMinecraft();
            ModelProjection projection = ModelProjection.projectModel(
                    model, modelToClip,
                    (double) minecraft.displayWidth / Math.max(1, minecraft.displayHeight)
            );
            this.aimVisibility = FadeState.next(
                    this.aimVisibility, projection, false,
                    (now - this.lastVisibilityFrame) / 1_000_000_000D, AIM_TRANSLUCENT_OPACITY
            );
            modelOpacity = this.aimVisibility.opacity();
            this.lastVisibilityFrame = now;
        }
        else if (this.fadingCreatureId == animatable.getEntityId()) {
            this.fadingCreatureId = -1;
            this.aimVisibility = new FadeState(false, 1f);
        }
        if (modelOpacity < 1f) {
            // Keep the ordinary opaque draw on contexts without a copyable depth buffer.
            boolean canCopyDepth = OpenGlHelper.isFramebufferEnabled()
                    && (GL.getCapabilities().OpenGL30 || GL.getCapabilities().GL_ARB_framebuffer_object);
            if (!canCopyDepth || GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING) == 0
                    || GL30.glGetFramebufferAttachmentParameteri(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                    GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE) != GL30.GL_RENDERBUFFER
            ) {
                modelOpacity = 1f;
                this.aimVisibility = new FadeState(false, 1f);
            }
        }
        //Restore through GlStateManager so its cached state and native OpenGL agree.
        //RiftLibrary changes culling and rescaled normals even for an opaque draw.
        boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean culling = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        boolean rescaledNormals = GL11.glIsEnabled(GL12.GL_RESCALE_NORMAL);
        boolean depthTesting = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        boolean depthWrites = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        int depthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
        boolean alphaTesting = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        int alphaFunction = GL11.glGetInteger(GL11.GL_ALPHA_TEST_FUNC);
        float alphaReference = GL11.glGetFloat(GL11.GL_ALPHA_TEST_REF);
        int sourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        int destinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int sourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        int destinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        FloatBuffer color = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_CURRENT_COLOR, color);

        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.enableAlpha();
        //Apply the texture's cutout threshold before the fade multiplier. Otherwise
        //partially covered edge texels disappear at different points of the transition.
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1f * alpha * modelOpacity);
        GlStateManager.resetColor();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableCull();
        this.renderingTranslucentAim = modelOpacity < 1f;
        if (this.renderingTranslucentAim) {
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            GlStateManager.depthMask(false);
        }
        else {
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        }
        super.render(model, animatable, partialTicks, red, green, blue, alpha * modelOpacity);
        this.renderingTranslucentAim = false;

        GlStateManager.depthMask(depthWrites);
        GlStateManager.depthFunc(depthFunction);
        if (depthTesting) GlStateManager.enableDepth();
        else GlStateManager.disableDepth();
        GlStateManager.alphaFunc(alphaFunction, alphaReference);
        if (alphaTesting) GlStateManager.enableAlpha();
        else GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
        if (blending) GlStateManager.enableBlend();
        else GlStateManager.disableBlend();
        if (culling) GlStateManager.enableCull();
        else GlStateManager.disableCull();
        if (rescaledNormals) GlStateManager.enableRescaleNormal();
        else GlStateManager.disableRescaleNormal();
        GlStateManager.resetColor();
        GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
    }

    @Override
    public void renderModel(@NotNull GeoModel model, float red, float green, float blue, float alpha) {
        if (this.renderingTranslucentAim) {
            this.aimDepthPass = DepthPass.render(
                    this.aimDepthPass, this.aimVisibility.fading(),
                    () -> super.renderModel(model, red, green, blue, alpha)
            );
        }
        else super.renderModel(model, red, green, blue, alpha);
    }

    @NotNull
    public static FadeState getRiderAimVisibility(@NotNull EntityPlayer player) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (player == minecraft.player && player.getRidingEntity() instanceof RiftCreature creature
                && creature.getCurrentMove().isEmpty() && CameraHandler.isRidingJawAim(creature, player)
                && minecraft.getRenderManager().<RiftCreature>getEntityRenderObject(creature) instanceof RiftCreatureRenderer renderer
                && renderer.fadingCreatureId == creature.getEntityId()
        ) {
            return renderer.aimVisibility;
        }
        return new FadeState(false, 1f);
    }

    public record DepthPass(int framebuffer, int depthBuffer, int width, int height, int format, int samples) {
        @NotNull
        public static DepthPass capture(@Nullable DepthPass previous) {
            int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            int renderbuffer = GL11.glGetInteger(GL30.GL_RENDERBUFFER_BINDING);

            // Match the live depth attachment, including packed stencil and multisampling.
            int sourceDepth = GL30.glGetFramebufferAttachmentParameteri(
                    GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME
            );
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, sourceDepth);
            int width = GL30.glGetRenderbufferParameteri(GL30.GL_RENDERBUFFER, GL30.GL_RENDERBUFFER_WIDTH);
            int height = GL30.glGetRenderbufferParameteri(GL30.GL_RENDERBUFFER, GL30.GL_RENDERBUFFER_HEIGHT);
            int format = GL30.glGetRenderbufferParameteri(GL30.GL_RENDERBUFFER, GL30.GL_RENDERBUFFER_INTERNAL_FORMAT);
            int samples = GL30.glGetRenderbufferParameteri(GL30.GL_RENDERBUFFER, GL30.GL_RENDERBUFFER_SAMPLES);
            DepthPass pass = previous;
            if (pass == null || pass.width() != width || pass.height() != height
                    || pass.format() != format || pass.samples() != samples
            ) {
                if (pass != null) {
                    GL30.glDeleteFramebuffers(pass.framebuffer());
                    GL30.glDeleteRenderbuffers(pass.depthBuffer());
                }
                pass = new DepthPass(GL30.glGenFramebuffers(), GL30.glGenRenderbuffers(), width, height, format, samples);
                GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, pass.depthBuffer());
                if (samples > 0) GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, samples, format, width, height);
                else GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, format, width, height);
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, pass.framebuffer());
                GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, pass.depthBuffer());
                GL11.glDrawBuffer(GL11.GL_NONE);
                GL11.glReadBuffer(GL11.GL_NONE);
            }
            GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderbuffer);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, drawFramebuffer);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pass.framebuffer());
            GL30.glBlitFramebuffer(0, 0, width, height, 0, 0, width, height, GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            return pass;
        }

        public void restore() {
            int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, this.framebuffer());
            GL30.glBlitFramebuffer(
                    0, 0, this.width(), this.height(), 0, 0, this.width(), this.height(),
                    GL11.GL_DEPTH_BUFFER_BIT, GL11.GL_NEAREST
            );
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
        }

        @NotNull
        public static DepthPass render(@Nullable DepthPass previous, boolean revealEntitiesBehind, @NotNull Runnable drawModel) {
            int depthFunction = GL11.glGetInteger(GL11.GL_DEPTH_FUNC);
            boolean depthWrites = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
            ByteBuffer colorMask = BufferUtils.createByteBuffer(16);
            GL11C.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, colorMask);
            DepthPass pass = DepthPass.capture(previous);

            //select the nearest textured surface per pixel, rather than approximating
            //visibility with face-center sorting. Both draws use RiftLibrary's geometry.
            GlStateManager.colorMask(false, false, false, false);
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            drawModel.run();
            GlStateManager.colorMask(
                    colorMask.get(0) != 0, colorMask.get(1) != 0,
                    colorMask.get(2) != 0, colorMask.get(3) != 0
            );
            GlStateManager.enableBlend();
            GlStateManager.depthMask(false);
            GlStateManager.depthFunc(GL11.GL_EQUAL);
            drawModel.run();

            //reveal targets only while aiming through the mount. As soon as it starts
            //returning to opaque, keep its depth so later entities are covered immediately.
            if (revealEntitiesBehind) pass.restore();
            GlStateManager.depthFunc(depthFunction);
            GlStateManager.depthMask(depthWrites);
            if (blending) GlStateManager.enableBlend();
            else GlStateManager.disableBlend();
            return pass;
        }
    }

    public record RiderPass(int framebuffer, int texture, int width, int height, @NotNull DepthPass depth) {
        @Nullable
        public static RiderPass capture(@Nullable RiderPass previous) {
            if (!(GL.getCapabilities().OpenGL30 || GL.getCapabilities().GL_ARB_framebuffer_object)
                    || GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING) == 0
                    || GL30.glGetFramebufferAttachmentParameteri(GL30.GL_DRAW_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT,
                    GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE) != GL30.GL_RENDERBUFFER
            ) {
                return null;
            }
            int drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            int readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            int boundTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            DepthPass depth = DepthPass.capture(previous == null ? null : previous.depth());
            RiderPass pass = previous;
            if (pass == null || pass.width() != depth.width() || pass.height() != depth.height()) {
                if (pass != null) {
                    GL30.glDeleteFramebuffers(pass.framebuffer());
                    GlStateManager.deleteTexture(pass.texture());
                }
                pass = new RiderPass(
                        GL30.glGenFramebuffers(), GlStateManager.generateTexture(),
                        depth.width(), depth.height(), depth
                );
                GlStateManager.bindTexture(pass.texture());
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, pass.width(), pass.height(), 0,
                        GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, pass.framebuffer());
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0,
                        GL11.GL_TEXTURE_2D, pass.texture(), 0);
                GL11.glDrawBuffer(GL30.GL_COLOR_ATTACHMENT0);
                GL11.glReadBuffer(GL30.GL_COLOR_ATTACHMENT0);
            }
            else pass = new RiderPass(pass.framebuffer(), pass.texture(), pass.width(), pass.height(), depth);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, drawFramebuffer);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, pass.framebuffer());
            GL30.glBlitFramebuffer(
                    0, 0, pass.width(), pass.height(), 0, 0, pass.width(), pass.height(),
                    GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST
            );
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
            GlStateManager.bindTexture(boundTexture);
            return pass;
        }

        public void composite(float opacity, boolean revealEntitiesBehind) {
            //fade the completed player draw, including armor, skin overlays and held items.
            //their renderers may change blending and color independently of the body.
            boolean blending = GL11.glIsEnabled(GL11.GL_BLEND);
            boolean depthTesting = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
            boolean depthWrites = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
            boolean alphaTesting = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
            boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
            boolean fog = GL11.glIsEnabled(GL11.GL_FOG);
            boolean culling = GL11.glIsEnabled(GL11.GL_CULL_FACE);
            int sourceRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
            int destinationRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
            int sourceAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
            int destinationAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
            int matrixMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
            int activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
            FloatBuffer color = BufferUtils.createFloatBuffer(16);
            FloatBuffer blendColor = BufferUtils.createFloatBuffer(16);
            IntBuffer viewport = BufferUtils.createIntBuffer(16);
            GL11C.glGetFloatv(GL11.GL_CURRENT_COLOR, color);
            GL11C.glGetFloatv(GL14.GL_BLEND_COLOR, blendColor);
            GL11C.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
            boolean lightmap = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE2);
            boolean brightnessTexture = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE0);
            boolean texturing = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);
            int texture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            int textureEnvironment = GL11.glGetTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE);
            FloatBuffer textureCoordinates = BufferUtils.createFloatBuffer(16);
            GL11C.glGetFloatv(GL11.GL_CURRENT_TEXTURE_COORDS, textureCoordinates);
            GlStateManager.enableTexture2D();
            GlStateManager.bindTexture(this.texture());
            GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.disableAlpha();
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GL14.glBlendColor(0f, 0f, 0f, 1f - Math.clamp(opacity, 0f, 1f));
            GlStateManager.tryBlendFuncSeparate(
                    GlStateManager.SourceFactor.CONSTANT_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA,
                    GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE
            );
            GlStateManager.viewport(0, 0, this.width(), this.height());
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.pushMatrix();
            GlStateManager.loadIdentity();
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0f, 0f);
            GL11.glVertex2f(-1f, -1f);
            GL11.glTexCoord2f(1f, 0f);
            GL11.glVertex2f(1f, -1f);
            GL11.glTexCoord2f(1f, 1f);
            GL11.glVertex2f(1f, 1f);
            GL11.glTexCoord2f(0f, 1f);
            GL11.glVertex2f(-1f, 1f);
            GL11.glEnd();
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_PROJECTION);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(matrixMode);
            GlStateManager.viewport(viewport.get(0), viewport.get(1), viewport.get(2), viewport.get(3));
            GL11.glTexCoord4f(textureCoordinates.get(0), textureCoordinates.get(1), textureCoordinates.get(2), textureCoordinates.get(3));
            if (revealEntitiesBehind) this.depth().restore();
            GlStateManager.bindTexture(texture);
            GlStateManager.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, textureEnvironment);
            if (!texturing) GlStateManager.disableTexture2D();
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE1);
            if (lightmap) GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(GL13.GL_TEXTURE2);
            if (brightnessTexture) GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(activeTexture);
            GlStateManager.depthMask(depthWrites);
            if (depthTesting) GlStateManager.enableDepth();
            if (alphaTesting) GlStateManager.enableAlpha();
            if (lighting) GlStateManager.enableLighting();
            if (fog) GlStateManager.enableFog();
            if (culling) GlStateManager.enableCull();
            GlStateManager.tryBlendFuncSeparate(sourceRgb, destinationRgb, sourceAlpha, destinationAlpha);
            GL14.glBlendColor(blendColor.get(0), blendColor.get(1), blendColor.get(2), blendColor.get(3));
            if (!blending) GlStateManager.disableBlend();
            GlStateManager.resetColor();
            GlStateManager.color(color.get(0), color.get(1), color.get(2), color.get(3));
        }
    }

    public record FadeState(boolean fading, float opacity) {
        @NotNull
        public static FadeState next(
                @NotNull FadeState current, @NotNull ModelProjection projection,
                boolean executingMove, double elapsedSeconds, float translucentOpacity
        ) {
            // A move always cancels the fade, regardless of where the model appears.
            if (executingMove) return new FadeState(false, 1f);
            boolean fading = current.fading();
            if (projection.distanceSquared() <= 0.64D) fading = true;
            else if (projection.distanceSquared() >= 1D) fading = false;
            float targetOpacity = fading ? Math.clamp(translucentOpacity, 0f, 1f) : 1f;
            float opacity = (float) MathUtil.slopeResult(
                    Math.clamp(elapsedSeconds, 0D, 0.1D) / 0.1D,
                    true, 0D, 1D, current.opacity(), targetOpacity
            );
            if (Math.abs(opacity - targetOpacity) < 0.001f) opacity = targetOpacity;
            return new FadeState(fading, opacity);
        }
    }

    public record ModelProjection(double distanceSquared, boolean belowReticle) {
        @NotNull
        @SuppressWarnings("SuspiciousNameCombination")
        public static ModelProjection projectModel(@NotNull GeoModel model, @NotNull Matrix4f modelToClip, double aspectRatio) {
            double nearestDistanceSquared = Double.MAX_VALUE;
            double highestY = -Double.MAX_VALUE;
            for (GeoBone bone : model.getAllBones().values()) {
                if (bone.isHidden() || bone.cubesAreHidden() || bone.childCubes.isEmpty()) continue;
                ArrayDeque<GeoBone> ancestors = new ArrayDeque<>();
                boolean hiddenByParent = false;
                for (GeoBone ancestor = bone; ancestor != null; ancestor = ancestor.parent) {
                    if (ancestor != bone && ancestor.childBonesAreHiddenToo()) {
                        hiddenByParent = true;
                        break;
                    }
                    ancestors.addFirst(ancestor);
                }
                if (hiddenByParent) continue;
                MatrixStack transforms = new MatrixStack();
                for (GeoBone ancestor : ancestors) {
                    transforms.translate(ancestor);
                    transforms.moveToPivot(ancestor);
                    transforms.rotate(ancestor);
                    transforms.scale(ancestor);
                    transforms.moveBackFromPivot(ancestor);
                }
                for (GeoCube cube : bone.childCubes) {
                    transforms.push();
                    transforms.moveToPivot(cube);
                    transforms.rotate(cube);
                    transforms.moveBackFromPivot(cube);
                    Matrix4f cubeToClip = new Matrix4f();
                    cubeToClip.mul(modelToClip, transforms.getModelMatrix());
                    for (GeoQuad quad : cube.getGeoQuads()) {
                        List<Vector4f> vertices = new ArrayList<>(4);
                        for (GeoVertex vertex : quad.geoVertices()) {
                            Vector4f clip = new Vector4f(vertex.position.x, vertex.position.y, vertex.position.z, 1f);
                            cubeToClip.transform(clip);
                            vertices.add(clip);
                        }

                        // Clip faces at the actual near plane, including cubes crossing the camera.
                        List<Vector4f> clipped = new ArrayList<>(5);
                        Vector4f previous = vertices.getLast();
                        double previousDepth = previous.z + previous.w;
                        for (Vector4f vertex : vertices) {
                            double depth = vertex.z + vertex.w;
                            if ((depth >= 0D) != (previousDepth >= 0D)) {
                                double fraction = previousDepth / (previousDepth - depth);
                                clipped.add(new Vector4f(
                                        (float) MathUtil.slopeResult(fraction, true, 0D, 1D, previous.x, vertex.x),
                                        (float) MathUtil.slopeResult(fraction, true, 0D, 1D, previous.y, vertex.y),
                                        (float) MathUtil.slopeResult(fraction, true, 0D, 1D, previous.z, vertex.z),
                                        (float) MathUtil.slopeResult(fraction, true, 0D, 1D, previous.w, vertex.w)
                                ));
                            }
                            if (depth >= 0D) clipped.add(vertex);
                            previous = vertex;
                            previousDepth = depth;
                        }
                        if (clipped.size() < 3 || clipped.stream().anyMatch(vertex -> vertex.w <= 1E-6f)) continue;
                        double[] x = new double[clipped.size()];
                        double[] y = new double[clipped.size()];
                        for (int index = 0; index < clipped.size(); index++) {
                            Vector4f vertex = clipped.get(index);
                            x[index] = vertex.x / vertex.w * aspectRatio / (2D * RiddenAimArea.HORIZONTAL_SCREEN_RADIUS);
                            y[index] = vertex.y / vertex.w / (2D * RiddenAimArea.VERTICAL_SCREEN_RADIUS);
                            highestY = Math.max(highestY, y[index]);
                        }
                        boolean positiveSide = false;
                        boolean negativeSide = false;
                        for (int index = 0; index < x.length; index++) {
                            int next = (index + 1) % x.length;
                            double deltaX = x[next] - x[index];
                            double deltaY = y[next] - y[index];
                            double cross = deltaX * -y[index] - deltaY * -x[index];
                            positiveSide |= cross > 1E-10D;
                            negativeSide |= cross < -1E-10D;
                            double lengthSquared = deltaX * deltaX + deltaY * deltaY;
                            if (lengthSquared <= 1E-12D) continue;
                            double fraction = Math.clamp(-(x[index] * deltaX + y[index] * deltaY) / lengthSquared, 0D, 1D);
                            double nearestX = MathUtil.slopeResult(fraction, true, 0D, 1D, x[index], x[next]);
                            double nearestY = MathUtil.slopeResult(fraction, true, 0D, 1D, y[index], y[next]);
                            nearestDistanceSquared = Math.min(nearestDistanceSquared, nearestX * nearestX + nearestY * nearestY);
                        }
                        if (positiveSide != negativeSide) return new ModelProjection(0D, false);
                    }
                    transforms.pop();
                }
            }
            return new ModelProjection(nearestDistanceSquared, highestY < -1D);
        }
    }
}
