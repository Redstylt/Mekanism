package mekanism.common.util;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import mekanism.api.TileNetworkList;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.GasTank;
import mekanism.common.PacketHandler;
import mekanism.common.tile.base.TileEntityMekanism;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

//TODO: Move this and factor out the parts into proper classes. This is mainly just temp to make organization not as needed
public class TileUtils {

    // N.B. All the tank I/O functions rely on the fact that an empty NBT Compound is a singular
    // byte and that the Gas/Fluid Stacks initialize to null if they are de-serialized from an
    // empty tag.
    private static final CompoundNBT EMPTY_TAG_COMPOUND = new CompoundNBT();

    public static void addTankData(TileNetworkList data, GasTank tank) {
        if (tank.getGas() != null) {
            data.add(tank.getGas().write(new CompoundNBT()));
        } else {
            data.add(EMPTY_TAG_COMPOUND);
        }
    }

    public static void addTankData(TileNetworkList data, FluidTank tank) {
        addFluidStack(data, tank.getFluid());
    }

    public static void addFluidStack(TileNetworkList data, FluidStack stack) {
        if (stack != null) {
            data.add(stack.writeToNBT(new CompoundNBT()));
        } else {
            data.add(EMPTY_TAG_COMPOUND);
        }
    }

    public static void readTankData(ByteBuf dataStream, GasTank tank) {
        tank.setGas(GasStack.readFromNBT(PacketHandler.readNBT(dataStream)));
    }

    public static void readTankData(ByteBuf dataStream, FluidTank tank) {
        tank.setFluid(readFluidStack(dataStream));
    }

    public static FluidStack readFluidStack(ByteBuf dataStream) {
        return FluidStack.loadFluidStackFromNBT(PacketHandler.readNBT(dataStream));
    }


    //Returns true if it entered the if statement, basically for use by TileEntityGasTank
    public static boolean receiveGas(ItemStack stack, GasTank tank) {
        if (!stack.isEmpty() && (tank.getGas() == null || tank.getStored() < tank.getMaxGas())) {
            tank.receive(GasUtils.removeGas(stack, tank.getGasType(), tank.getNeeded()), true);
            return true;
        }
        return false;
    }

    public static void drawGas(ItemStack stack, GasTank tank) {
        drawGas(stack, tank, true);
    }

    public static void drawGas(ItemStack stack, GasTank tank, boolean doDraw) {
        if (!stack.isEmpty() && tank.getGas() != null) {
            tank.draw(GasUtils.addGas(stack, tank.getGas()), doDraw);
        }
    }

    public static void emitGas(TileEntityMekanism tile, GasTank tank, int gasOutput, Direction facing) {
        if (tank.getGas() != null) {
            GasStack toSend = new GasStack(tank.getGas().getGas(), Math.min(tank.getStored(), gasOutput));
            tank.draw(GasUtils.emit(toSend, tile, EnumSet.of(facing)), true);
        }
    }
}