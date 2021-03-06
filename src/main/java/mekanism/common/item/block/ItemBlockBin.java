package mekanism.common.item.block;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.block.basic.BlockBin;
import mekanism.common.inventory.InventoryBin;
import mekanism.common.item.ITieredItem;
import mekanism.common.registration.impl.ItemDeferredRegister;
import mekanism.common.tier.BinTier;
import mekanism.common.util.ItemDataUtils;
import mekanism.common.util.text.TextComponentUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ItemBlockBin extends ItemBlockTooltip<BlockBin> implements ITieredItem<BinTier> {

    public ItemBlockBin(BlockBin block) {
        super(block, ItemDeferredRegister.getMekBaseProperties().maxStackSize(1));
    }

    @Nullable
    @Override
    public BinTier getTier(@Nonnull ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemBlockBin) {
            return ((ItemBlockBin) item).getBlock().getTier();
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addStats(@Nonnull ItemStack stack, World world, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flag) {
        InventoryBin inv = new InventoryBin(stack);
        if (inv.getItemCount() > 0) {
            tooltip.add(TextComponentUtil.build(EnumColor.BRIGHT_GREEN, inv.getItemType().getDisplayName()));
            tooltip.add(MekanismLang.ITEM_AMOUNT.translateColored(EnumColor.PURPLE, EnumColor.GRAY,
                  inv.getItemCount() == Integer.MAX_VALUE ? MekanismLang.INFINITE : inv.getItemCount()));
        } else {
            tooltip.add(MekanismLang.EMPTY.translateColored(EnumColor.DARK_RED));
        }
        BinTier tier = getTier(stack);
        if (tier != null) {
            int cap = tier.getStorage();
            if (cap == Integer.MAX_VALUE) {
                tooltip.add(MekanismLang.CAPACITY.translateColored(EnumColor.INDIGO, EnumColor.GRAY, MekanismLang.INFINITE));
            } else {
                tooltip.add(MekanismLang.CAPACITY_ITEMS.translateColored(EnumColor.INDIGO, EnumColor.GRAY, cap));
            }
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return ItemDataUtils.hasData(stack, "newCount");
    }

    @Nonnull
    @Override
    public ItemStack getContainerItem(@Nonnull ItemStack stack) {
        if (!ItemDataUtils.hasData(stack, "newCount")) {
            return ItemStack.EMPTY;
        }
        int newCount = ItemDataUtils.getInt(stack, "newCount");
        ItemDataUtils.removeData(stack, "newCount");
        ItemStack ret = stack.copy();
        ItemDataUtils.setInt(ret, "itemCount", newCount);
        return ret;
    }
}