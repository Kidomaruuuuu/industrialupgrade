package com.denfop.render.base;


import com.denfop.Constants;
import com.denfop.tiles.base.IIsMolecular;
import ic2.core.block.TileEntityBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderCoreProcess<T extends TileEntityBlock> extends TileEntitySpecialRenderer<T> {

    private static final ResourceLocation plazmaTextloc = new ResourceLocation(Constants.MOD_ID, "textures/models/plazma.png");

    private static final ResourceLocation particlesTextloc = new ResourceLocation(
            Constants.MOD_ID,
            "textures/models/particles.png"
    );

    private static final Map<List<Serializable>, Integer> textureSizeCache = new HashMap<>();


    public int ticker;

    public static int getTextureSize(String s, int dv) {
        Integer textureSize = textureSizeCache.get(Arrays.asList(s, dv));
        if (textureSize != null) {
            return textureSize;
        }
        try {
            InputStream inputstream = Minecraft
                    .getMinecraft()
                    .getResourceManager()
                    .getResource(new ResourceLocation(Constants.MOD_ID, s))
                    .getInputStream();
            BufferedImage bi = ImageIO.read(inputstream);
            int size = bi.getWidth() / dv;
            textureSizeCache.put(Arrays.asList(new Serializable[]{s, dv}), size);
            return size;
        } catch (Exception var5) {
            var5.printStackTrace();
            return 16;
        }
    }

    public void renderCore(T te, double x, double y, double z) {
        this.ticker++;
        if (this.ticker > 161) {
            this.ticker = 1;
        }
        int size1 = getTextureSize("textures/models/plazma.png", 64);
        int size2 = getTextureSize("textures/models/particles.png", 32);
        if (te instanceof IIsMolecular) {
            if (((IIsMolecular) te).getMode() != 0) {
                size2 = getTextureSize("textures/models/particles" + ((IIsMolecular) te).getMode() + ".png", 32);
            }
        }
        float f1 = ActiveRenderInfo.getRotationX();
        float f2 = ActiveRenderInfo.getRotationXZ();
        float f3 = ActiveRenderInfo.getRotationZ();
        float f4 = ActiveRenderInfo.getRotationYZ();
        float f5 = ActiveRenderInfo.getRotationXY();
        float scaleCore = 0.35F;
        float posX = (float) x + 0.5F;
        float posY = (float) y + 0.5F;
        float posZ = (float) z + 0.5F;
        Color color = new Color(12648447);
        GL11.glPushMatrix();
        GL11.glDepthMask(false);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 1);
        (FMLClientHandler.instance().getClient()).renderEngine.bindTexture(plazmaTextloc);
        int i = this.ticker % 16;
        float size4 = (size1 * 4);
        float float_sizeMinus0_01 = size1 - 0.01F;
        float x0 = ((i % 4 * size1) + 0.0F) / size4;
        float x1 = ((i % 4 * size1) + float_sizeMinus0_01) / size4;
        float x2 = ((i / 4F * size1) + 0.0F) / size4;
        float x3 = ((i / 4F * size1) + float_sizeMinus0_01) / size4;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer
                .pos((posX - f1 * scaleCore - f4 * scaleCore), (posY - f2 * scaleCore), (posZ - f3 * scaleCore - f5 * scaleCore))
                .tex(x1, x3)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();
        buffer
                .pos((posX - f1 * scaleCore + f4 * scaleCore), (posY + f2 * scaleCore), (posZ - f3 * scaleCore + f5 * scaleCore))
                .tex(x1, x2)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();
        buffer
                .pos((posX + f1 * scaleCore + f4 * scaleCore), (posY + f2 * scaleCore), (posZ + f3 * scaleCore + f5 * scaleCore))
                .tex(x0, x2)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();
        buffer
                .pos((posX + f1 * scaleCore - f4 * scaleCore), (posY - f2 * scaleCore), (posZ + f3 * scaleCore - f5 * scaleCore))
                .tex(x0, x3)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();
        tessellator.draw();
        GL11.glDisable(3042);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glDepthMask(false);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 1);
        if (te instanceof IIsMolecular) {
            if (((IIsMolecular) te).getMode() != 0) {
                (FMLClientHandler.instance().getClient()).renderEngine.bindTexture(new ResourceLocation(
                        Constants.MOD_ID,
                        "textures/models/particles" + ((IIsMolecular) te).getMode() + ".png"
                ));
            } else {
                (FMLClientHandler.instance().getClient()).renderEngine.bindTexture(particlesTextloc);
            }
        } else {
            (FMLClientHandler.instance().getClient()).renderEngine.bindTexture(particlesTextloc);
        }

        int qq = this.ticker % 16;
        i = 24 + qq;
        float size8 = (size2 * 8);
        float_sizeMinus0_01 = size2 - 0.01F;
        x0 = ((i % 8 * size2) + 0.0F) / size8;
        x1 = ((i % 8 * size2) + float_sizeMinus0_01) / size8;
        x2 = ((i / 8F * size2) + 0.0F) / size8;
        x3 = ((i / 8F * size2) + float_sizeMinus0_01) / size8;
        float var11 = MathHelper.sin(this.ticker / 10.0F) * 0.1F;
        scaleCore = 0.4F + var11;
        buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer
                .pos((posX - f1 * scaleCore - f4 * scaleCore), (posY - f2 * scaleCore), (posZ - f3 * scaleCore - f5 * scaleCore))
                .tex(x1, x3)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();
        buffer
                .pos((posX - f1 * scaleCore + f4 * scaleCore), (posY + f2 * scaleCore), (posZ - f3 * scaleCore + f5 * scaleCore))
                .tex(x1, x2)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();
        buffer
                .pos((posX + f1 * scaleCore + f4 * scaleCore), (posY + f2 * scaleCore), (posZ + f3 * scaleCore + f5 * scaleCore))
                .tex(x0, x2)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();
        buffer
                .pos((posX + f1 * scaleCore - f4 * scaleCore), (posY - f2 * scaleCore), (posZ + f3 * scaleCore - f5 * scaleCore))
                .tex(x0, x3)
                .color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                .endVertex();
        tessellator.draw();
        GL11.glDisable(3042);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
        GL11.glBlendFunc(770, 0);
    }


    public void render(T te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

        GL11.glPushMatrix();
        GL11.glTranslatef((float) x + 0.5F, (float) y + 1.5F, (float) z + 0.5F);
        GL11.glPushMatrix();
        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
        if (te.getActive()) {
            GL11.glPushMatrix();
            renderCore(te, x, y, z);
            GL11.glPopMatrix();


        }
    }

}
