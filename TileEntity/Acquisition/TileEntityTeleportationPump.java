/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.TileEntity.Acquisition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import Reika.ChromatiCraft.Auxiliary.ChromaStacks;
import Reika.ChromatiCraft.Base.TileEntity.ChargedCrystalPowered;
import Reika.ChromatiCraft.Magic.ElementTagCompound;
import Reika.ChromatiCraft.Registry.ChromaItems;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.DragonAPI.Instantiable.HybridTank;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.Registry.ReikaItemHelper;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;

public class TileEntityTeleportationPump extends ChargedCrystalPowered implements IFluidHandler {

	private final HybridTank tank = new HybridTank("telepump", 4000);
	private HashMap<Fluid, ArrayList<Coordinate>> fluids = new HashMap();
	private HashMap<Fluid, Integer> counts = new HashMap();

	private Fluid selected = null;

	private boolean scanning = true;
	private boolean fastscan = false;
	private int scanY = 0;

	private static final ElementTagCompound required = new ElementTagCompound();

	private static final Comparator comparator = new PosComparator();

	private static class PosComparator implements Comparator<Coordinate> {

		private PosComparator() {

		}

		@Override
		public int compare(Coordinate o1, Coordinate o2) {
			return o2.yCoord-o1.yCoord; //higher first
		}

	}

	static {
		required.addTag(CrystalElement.CYAN, 250);
		required.addTag(CrystalElement.BLACK, 100);
		required.addTag(CrystalElement.LIME, 500);
	}

	public static ElementTagCompound getRequiredEnergy() {
		return required.copy();
	}

	@Override
	public int getSizeInventory() {
		return 4;
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		return tank.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		return new FluidTankInfo[]{tank.getInfo()};
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack is, int side) {
		switch(slot) {
		case 0:
			return this.getStoredEnergy() == 0;
		case 1:
			return FluidContainerRegistry.isFilledContainer(is);
		default:
			return false;
		}
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack is) {
		switch(slot) {
		case 0:
			return ChromaItems.STORAGE.matchWith(is);
		case 1:
			return is.getItem() == Items.bucket;
		default:
			return false;
		}
	}

	public Map<Fluid, Integer> getFluidCount() {
		return Collections.unmodifiableMap(counts);
	}

	public int getFluidCount(Fluid f) {
		return counts.containsKey(f) ? counts.get(f) : 0;//fluids.containsKey(f) ? fluids.get(f).size() : 0;
	}

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.TELEPUMP;
	}

	public void setTargetedFluid(int fid) {
		selected = FluidRegistry.getFluid(fid);
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		if (scanning) {
			this.onScan(world, x, y, z);
		}
		if (world.isRemote)
			return;
		if (!scanning && fluids.isEmpty() && this.getTicksExisted() == 0) {
			fastscan = true;
		}
		if (scanning || fastscan) {
			int n = 0;
			while (n == 0 || (fastscan && n < 32)) {
				int r = this.getRange();
				for (int i = -r; i <= r; i++) {
					for (int k = -r; k <= r; k++) {
						int dx = x+i;
						int dy = scanY;
						int dz = z+k;
						Block b = world.getBlock(dx, dy, dz);
						if (world.getBlockMetadata(dx, dy, dz) == 0) {
							Fluid f = FluidRegistry.lookupFluidForBlock(b);
							if (f != null) {
								this.addFluidBlock(dx, dy, dz, f);
								//ReikaJavaLibrary.pConsole(f.getName()+" @ "+dx+","+dy+","+dz, Side.SERVER, f.getName().startsWith("o"));
							}
						}
					}
				}
				scanY++;
				if (scanY > 255) {
					scanning = false;
					fastscan = false;
					Collection<ArrayList<Coordinate>> vals = fluids.values();
					for (ArrayList<Coordinate> li : vals) {
						Collections.shuffle(li);
						Collections.sort(li, comparator);
					}
				}
				n++;
			}
		}
		else {
			int n = this.hasSpeed() ? 4 : 1;
			if (selected != null) {
				ArrayList<Coordinate> li = fluids.get(selected);
				for (int i = 0; i < n; i++) {
					if (li != null && !li.isEmpty() && this.canAddFluid(selected) && this.hasEnergy(required)) {
						Coordinate c = li.get(0);
						tank.addLiquid(1000, selected);
						c.setBlock(world, Blocks.air);
						this.useEnergy(required.copy().scale(this.hasEfficiency() ? 0.25F : 1));
						li.remove(0);
						this.decrFluid(selected);
					}

					Fluid f = tank.getActualFluid();
					if (f != null) {
						if (tank.getLevel() >= 1000) {
							this.fillBucket(f);
						}
						if (!tank.isEmpty()) {
							for (int k = 0; k < 6; k++) {
								ForgeDirection dir = dirs[k];
								TileEntity te = this.getAdjacentTileEntity(dir);
								if (te instanceof IFluidHandler) {
									IFluidHandler ifl = (IFluidHandler)te;
									int amt = ifl.fill(dir.getOpposite(), tank.getFluid(), true);
									if (amt > 0) {
										tank.removeLiquid(amt);
										if (tank.isEmpty())
											break;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void fillBucket(Fluid f) {
		if (inv[0] != null) {
			int amt = FluidContainerRegistry.getContainerCapacity(inv[0]);
			ItemStack is = FluidContainerRegistry.fillFluidContainer(new FluidStack(f, amt), inv[1]);
			if (is != null) {
				inv[1] = is;
				tank.removeLiquid(amt);
			}
		}
	}

	private void decrFluid(Fluid f) {
		Integer i = counts.get(f);
		int amt = i != null ? i.intValue() : 0;
		if (amt > 1)
			counts.put(f, amt-1);
		else
			counts.remove(i);
		//ReikaJavaLibrary.pConsole(counts.get(f));
	}

	@Override
	public boolean isPlayerAccessible(EntityPlayer var1) {
		return !scanning && super.isPlayerAccessible(var1);
	}

	private void onScan(World world, int x, int y, int z) {
		ReikaParticleHelper.PORTAL.spawnAroundBlock(world, x, y, z, 8);
	}

	public int getRange() {
		return 256;
	}

	private void addFluidBlock(int x, int y, int z, Fluid f) {
		ArrayList<Coordinate> li = fluids.get(f);
		if (li == null) {
			li = new ArrayList();
			fluids.put(f, li);
			counts.put(f, 0);
		}
		li.add(new Coordinate(x, y, z));
		counts.put(f, counts.get(f)+1);
	}

	public int getLiquidScaled(int a) {
		return a * tank.getLevel() / tank.getCapacity();
	}

	public Fluid getTankFluid() {
		return tank.getActualFluid();
	}

	private boolean canAddFluid(Fluid f) {
		return tank.canTakeIn(1000);
	}

	private boolean hasSpeed() {
		return ReikaItemHelper.matchStacks(inv[2], ChromaStacks.speedUpgrade);
	}

	private boolean hasEfficiency() {
		return ReikaItemHelper.matchStacks(inv[3], ChromaStacks.efficiencyUpgrade);
	}

	@Override
	protected void animateWithTick(World world, int x, int y, int z) {

	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		tank.writeToNBT(NBT);

		NBT.setBoolean("scan", scanning);
		NBT.setString("fluid", selected != null ? selected.getName() : "null");

		NBTTagCompound tag = new NBTTagCompound();
		for (Fluid f : counts.keySet()) {
			String s = f.getName();
			int c = counts.get(f);
			tag.setInteger(s, c);
		}
		NBT.setTag("counts", tag);
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		tank.readFromNBT(NBT);

		scanning = NBT.getBoolean("scan");
		String fid = NBT.hasKey("fluid") ? NBT.getString("fluid") : null;
		selected = fid == null || fid.equals("null") ? null : FluidRegistry.getFluid(fid);

		NBTTagCompound tag = NBT.getCompoundTag("counts");
		for (Object o : tag.func_150296_c()) {
			String s = (String)o;
			Fluid f = FluidRegistry.getFluid(s);
			if (f != null) {
				counts.put(f, tag.getInteger(s));
			}
		}
		//ReikaJavaLibrary.pConsole(counts);
	}

}
