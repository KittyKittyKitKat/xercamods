package xerca.xercapaint.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import xerca.xercapaint.CanvasType;
import xerca.xercapaint.entity.EntityEasel;

import java.util.Arrays;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class GuiCanvasView extends Screen {
    private int canvasX; // = 140;
    private int canvasY = 40;
    private int canvasWidth;
    private int canvasHeight;
    private int canvasPixelScale;
    private int canvasPixelWidth;
    private int canvasPixelHeight;
    private CanvasType canvasType;

    private boolean isSigned = false;
    private int[] pixels;
    private String authorName = "";
    private String canvasTitle = "";
    private String name = "";
    private int version = 0;
    private int generation = 0;
    private EntityEasel easel;
    private Player player;

    protected GuiCanvasView(CompoundTag canvasTag, Component title, CanvasType canvasType, EntityEasel easel) {
        super(title);

        this.canvasType = canvasType;
        this.canvasPixelScale = canvasType == CanvasType.SMALL ? 10 : 5;
        this.canvasPixelWidth = CanvasType.getWidth(canvasType);
        this.canvasPixelHeight = CanvasType.getHeight(canvasType);
        int canvasPixelArea = canvasPixelHeight*canvasPixelWidth;
        this.canvasWidth = this.canvasPixelWidth * this.canvasPixelScale;
        this.canvasHeight = this.canvasPixelHeight * this.canvasPixelScale;
        this.easel = easel;
        this.player = Minecraft.getInstance().player;

        if (canvasTag != null && !canvasTag.isEmpty()) {
            int[] nbtPixels = canvasTag.getIntArray("pixels");
            this.authorName = canvasTag.getString("author");
            this.canvasTitle = canvasTag.getString("title");
            this.name = canvasTag.getString("name");
            this.version = canvasTag.getInt("v");
            this.generation = canvasTag.getInt("generation");

            this.pixels =  Arrays.copyOfRange(nbtPixels, 0, canvasPixelArea);
        } else {
            this.isSigned = false;
        }
    }

    @Override
    public void init() {
        canvasX = (this.width - canvasWidth) / 2;
        if(canvasType.equals(CanvasType.LONG)){
            canvasY += 40;
        }
    }

    private int getPixelAt(int x, int y){
        return (this.pixels == null) ? 0xFFF9FFFE : this.pixels[y*canvasPixelWidth + x];
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float f) {
        for(int i=0; i<canvasPixelHeight; i++){
            for(int j=0; j<canvasPixelWidth; j++){
                int x = canvasX + j*canvasPixelScale;
                int y = canvasY + i*canvasPixelScale;
                fill(matrixStack, x, y, x+canvasPixelScale, y+canvasPixelScale, getPixelAt(j, i));
            }
        }
        
        if(generation > 0 && !canvasTitle.isEmpty()){
            String title = canvasTitle + " " + I18n.get("canvas.byAuthor", authorName);
            String gen = "(" + I18n.get("canvas.generation." + (generation - 1)) + ")";

            int titleWidth = this.font.width(title);
            int genWidth = this.font.width(gen);

            float titleX = (canvasX + (canvasWidth - titleWidth) / 2.0f);
            float genX = (canvasX + (canvasWidth - genWidth) / 2.0f);
            float minX = Math.min(genX, titleX);
            float maxX = Math.max(genX + genWidth, titleX + titleWidth);

            fill(matrixStack, (int)(minX - 10), canvasY - 30, (int)(maxX + 10), canvasY - 4, 0xFFEEEEEE);

            this.font.draw(matrixStack, title, titleX, canvasY - 25, 0xFF111111);
            this.font.draw(matrixStack, gen, genX, canvasY - 14, 0xFF444444);
        }
    }

    @Override
    public void tick() {
        if(easel != null){
            if(easel.getItem().isEmpty() || easel.isRemoved() || easel.distanceToSqr(player) > 64){
                this.onClose();
            }
        }
        super.tick();
    }
}