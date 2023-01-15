package xerca.xercapaint.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import xerca.xercapaint.Mod;
import xerca.xercapaint.packets.PaletteUpdatePacket;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class GuiPalette extends BasePalette {

    protected GuiPalette(CompoundTag paletteTag, Component title) {
        super(title, paletteTag);
    }

    @Override
    public void init() {
        paletteX = paletteXs[paletteXs.length - 1];
        paletteY = paletteYs[paletteYs.length - 1];
        if(paletteX == -1000 || paletteY == -1000){
            paletteX = 140;
            paletteY = 40;
        }
        updatePalettePos(0, 0);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float f) {
        super.render(matrixStack, mouseX, mouseY, f);

        renderCursor(matrixStack, mouseX, mouseY);
    }

    private void renderCursor(PoseStack matrixStack, int mouseX, int mouseY){
        if(isCarryingColor){
            carriedColor.setGLColor();
            blit(matrixStack, mouseX-brushSpriteSize/2, mouseY-brushSpriteSize/2, brushSpriteX+brushSpriteSize, brushSpriteY, dropSpriteWidth, brushSpriteSize);

        }else if(isCarryingWater){
            waterColor.setGLColor();
            blit(matrixStack, mouseX-brushSpriteSize/2, mouseY-brushSpriteSize/2, brushSpriteX+brushSpriteSize, brushSpriteY, dropSpriteWidth, brushSpriteSize);
        }
    }

    @Override
    public boolean mouseDragged(double posX, double posY, int mouseButton, double deltaX, double deltaY) {
        if(isCarryingPalette){
            boolean ret = super.mouseDragged(posX, posY, mouseButton, deltaX, deltaY);
            updatePalettePos(deltaX, deltaY);
            return ret;
        }
        return super.mouseDragged(posX, posY, mouseButton, deltaX, deltaY);
    }

    private void updatePalettePos(double deltaX, double deltaY){
        paletteX += deltaX;
        paletteY += deltaY;

        paletteXs[paletteXs.length - 1] = paletteX;
        paletteYs[paletteYs.length - 1] = paletteY;
    }

    @Override
    public void removed() {
        if (paletteDirty) {
            PaletteUpdatePacket pack = new PaletteUpdatePacket(customColors);
            ClientPlayNetworking.send(Mod.PALETTE_UPDATE_PACKET_ID, pack.encode());
        }
    }
}