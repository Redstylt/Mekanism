package mekanism.common.item;

import java.util.List;
import javax.annotation.Nonnull;
import mekanism.api.Coord4D;
import mekanism.api.EnumColor;
import mekanism.api.Pos3D;
import mekanism.common.entity.EntityBalloon;
import mekanism.common.util.LangUtils;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemBalloon extends ItemMekanism {

    private final EnumColor color;

    public ItemBalloon(EnumColor color) {
        super(color.registry_prefix + "_balloon");
        this.color = color;
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new DispenserBehavior());
    }

    @Override
    public void registerOreDict() {
        OreDictionary.registerOre("balloon", new ItemStack(this));
        if (color.dyeName != null) {
            //As of the moment none of the colors used have a null dye name but if the other ones get used this is needed
            OreDictionary.registerOre("balloon" + color.dyeName, new ItemStack(this));
        }
    }

    public EnumColor getColor() {
        return color;
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity entityplayer, @Nonnull Hand hand) {
        if (!world.isRemote) {
            Pos3D pos = new Pos3D(hand == Hand.MAIN_HAND ? -0.4 : 0.4, 0, 0.3).rotateYaw(entityplayer.renderYawOffset).translate(new Pos3D(entityplayer));
            world.spawnEntity(new EntityBalloon(world, pos.x - 0.5, pos.y - 0.25, pos.z - 0.5, color));
        }
        ItemStack itemstack = entityplayer.getHeldItem(hand);
        if (!entityplayer.capabilities.isCreativeMode) {
            itemstack.shrink(1);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, itemstack);
    }

    @Nonnull
    @Override
    public String getItemStackDisplayName(@Nonnull ItemStack stack) {
        //TODO: Remove this method??
        String dyeName = color.getDyedName();
        if (LangUtils.canLocalize(getTranslationKey(stack) + "." + color.dyeName)) {
            return LangUtils.localize(getTranslationKey(stack) + "." + color.dyeName);
        }
        if (color == EnumColor.BLACK) {
            dyeName = EnumColor.DARK_GREY + color.getDyeName();
        }
        return dyeName + " " + LangUtils.localize("tooltip.balloon");
    }

    @Nonnull
    @Override
    public EnumActionResult onItemUse(PlayerEntity player, World world, BlockPos pos, Hand hand, Direction side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.isSneaking()) {
            AxisAlignedBB bound = new AxisAlignedBB(pos, pos.add(1, 3, 1));
            List<EntityBalloon> balloonsNear = player.world.getEntitiesWithinAABB(EntityBalloon.class, bound);
            if (balloonsNear.size() > 0) {
                return EnumActionResult.FAIL;
            }
            if (world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
                pos = pos.down();
            }
            if (!world.isSideSolid(pos, Direction.UP)) {
                return EnumActionResult.FAIL;
            }
            if (canReplace(world, pos.up()) && canReplace(world, pos.up(2))) {
                world.setBlockToAir(pos.up());
                world.setBlockToAir(pos.up(2));
                if (!world.isRemote) {
                    world.spawnEntity(new EntityBalloon(world, new Coord4D(pos, world), color));
                    stack.shrink(1);
                }
                return EnumActionResult.SUCCESS;
            }
            return EnumActionResult.FAIL;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, PlayerEntity player, EntityLivingBase entity, Hand hand) {
        if (player.isSneaking()) {
            if (!player.world.isRemote) {
                AxisAlignedBB bound = new AxisAlignedBB(entity.posX - 0.2, entity.posY - 0.5, entity.posZ - 0.2, entity.posX + 0.2,
                      entity.posY + entity.height + 4, entity.posZ + 0.2);
                List<EntityBalloon> balloonsNear = player.world.getEntitiesWithinAABB(EntityBalloon.class, bound);
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        return true;
                    }
                }
                player.world.spawnEntity(new EntityBalloon(entity, color));
                stack.shrink(1);
            }
            return true;
        }
        return false;
    }

    private boolean canReplace(World world, BlockPos pos) {
        return world.isAirBlock(pos) || world.getBlockState(pos).getBlock().isReplaceable(world, pos);
    }

    public class DispenserBehavior extends BehaviorDefaultDispenseItem {

        @Nonnull
        @Override
        public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
            Coord4D coord = new Coord4D(source.getX(), source.getY(), source.getZ(), source.getWorld().provider.getDimension());
            Direction side = source.getBlockState().getValue(BlockDispenser.FACING);

            List<EntityLivingBase> entities = source.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, coord.offset(side).getBoundingBox());
            boolean latched = false;

            for (EntityLivingBase entity : entities) {
                AxisAlignedBB bound = new AxisAlignedBB(entity.posX - 0.2, entity.posY - 0.5, entity.posZ - 0.2, entity.posX + 0.2,
                      entity.posY + entity.height + 4, entity.posZ + 0.2);
                List<EntityBalloon> balloonsNear = source.getWorld().getEntitiesWithinAABB(EntityBalloon.class, bound);
                boolean hasBalloon = false;
                for (EntityBalloon balloon : balloonsNear) {
                    if (balloon.latchedEntity == entity) {
                        hasBalloon = true;
                        break;
                    }
                }
                if (!hasBalloon) {
                    source.getWorld().spawnEntity(new EntityBalloon(entity, color));
                    latched = true;
                }
            }
            if (!latched) {
                Pos3D pos = new Pos3D(coord).translate(0, -0.5, 0);
                switch (side) {
                    case DOWN:
                        pos = pos.translate(0, -2.5, 0);
                        break;
                    case UP:
                        pos = pos.translate(0, 0, 0);
                        break;
                    case NORTH:
                        pos = pos.translate(0, -1, -0.5);
                        break;
                    case SOUTH:
                        pos = pos.translate(0, -1, 0.5);
                        break;
                    case WEST:
                        pos = pos.translate(-0.5, -1, 0);
                        break;
                    case EAST:
                        pos = pos.translate(0.5, -1, 0);
                        break;
                    default:
                        break;
                }
                if (!source.getWorld().isRemote) {
                    source.getWorld().spawnEntity(new EntityBalloon(source.getWorld(), pos.x, pos.y, pos.z, color));
                }
            }
            stack.shrink(1);
            return stack;
        }
    }
}