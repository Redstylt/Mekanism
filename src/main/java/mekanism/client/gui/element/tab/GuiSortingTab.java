package mekanism.client.gui.element.tab;

import mekanism.api.TileNetworkList;
import mekanism.client.gui.IGuiWrapper;
import mekanism.client.gui.element.GuiInsetElement;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.Mekanism;
import mekanism.common.MekanismLang;
import mekanism.common.network.PacketTileEntity;
import mekanism.common.tile.factory.TileEntityFactory;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.MekanismUtils.ResourceType;
import mekanism.common.util.text.BooleanStateDisplay.OnOff;
import net.minecraft.util.ResourceLocation;

public class GuiSortingTab extends GuiInsetElement<TileEntityFactory<?>> {

    private static final ResourceLocation HOLDER_LEFT = MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "extended_holder_left.png");

    public GuiSortingTab(IGuiWrapper gui, TileEntityFactory<?> tile, ResourceLocation def) {
        super(MekanismUtils.getResource(ResourceType.GUI_ELEMENT, "sorting.png"), gui, def, tile, -26, 62, 26, 35, 18);
    }

    @Override
    protected ResourceLocation getHolderTexture() {
        //TODO: Override the right tab holder at some point if we need it
        return left ? HOLDER_LEFT : GuiInsetElement.INSET_HOLDER_RIGHT;
    }

    @Override
    public void renderButton(int mouseX, int mouseY, float partialTicks) {
        super.renderButton(mouseX, mouseY, partialTicks);
        drawString(OnOff.of(tile.sorting).getTextComponent(), x + 5, y + 24, 0x0404040);
        //TODO: Check if needed
        MekanismRenderer.resetColor();
    }

    @Override
    public void renderToolTip(int mouseX, int mouseY) {
        displayTooltip(MekanismLang.AUTO_SORT.translate(), mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        Mekanism.packetHandler.sendToServer(new PacketTileEntity(tile, TileNetworkList.withContents(0)));
    }
}