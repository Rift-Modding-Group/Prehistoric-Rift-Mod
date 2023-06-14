package com.anightdazingzoroark.rift.client.ui;

import com.anightdazingzoroark.rift.server.entities.RiftEgg;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class EggInfo extends Screen {
    RiftEgg egg;
    private int leftPos;
    private int topPos;
    protected int xSize = 176;
    protected int ySize = 166;
    private static final ResourceLocation texture = new ResourceLocation("rift:textures/ui/generic_screen.png");


    public EggInfo(RiftEgg egg) {
        super(Component.translatable("rift.egg_status.title"));
        this.egg = egg;
    }

    @Override
    public void render(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(ms);
        super.render(ms, mouseX, mouseY, partialTicks);

    }

    @Override
    public void renderBackground(PoseStack ms) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderTexture(0, texture);
        this.blit(ms, this.leftPos, this.topPos, 0, 0, this.xSize, this.ySize, this.xSize, this.ySize);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    @Override
    public boolean keyPressed(int key, int b, int c) {
        if (key == 256) {
            this.minecraft.player.closeContainer();
            return true;
        }

        return super.keyPressed(key, b, c);
    }

    @Override
    public void init() {
        this.leftPos = (this.width - this.xSize) / 2;
        this.topPos = (this.height - this.ySize) / 2;
        super.init();
    }

}
