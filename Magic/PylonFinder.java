package Reika.ChromatiCraft.Magic;

import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.DragonAPI.Instantiable.RayTracer;
import Reika.DragonAPI.Instantiable.WorldLocation;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.world.World;

public class PylonFinder {

	private final LinkedList<WorldLocation> nodes = new LinkedList();
	private final CrystalNetworker net;
	private static final RayTracer tracer;

	private final int stepRange;
	private final World world;
	private final int targetX;
	private final int targetY;
	private final int targetZ;
	private final CrystalElement element;

	PylonFinder(CrystalElement e, World world, int x, int y, int z, int r) {
		element = e;
		this.world = world;
		targetX = x;
		targetY = y;
		targetZ = z;
		stepRange = r;
		net = CrystalNetworker.instance;
	}

	CrystalPath findPylon() {
		this.findFrom(targetX, targetY, targetZ);
		return this.isComplete() ? new CrystalPath(element, nodes) : null;
	}

	CrystalFlow findPylon(CrystalReceiver r, int amount) {
		this.findFrom(targetX, targetY, targetZ);
		return this.isComplete() ? new CrystalFlow(r, element, amount, nodes) : null;
	}

	public boolean isComplete() {
		return nodes.size() >= 2 && nodes.getLast().getTileEntity() instanceof CrystalTransmitter;
	}

	private void findFrom(int x, int y, int z) {
		if (nodes.contains(new WorldLocation(world, x, y, z))) {
			return;
		}
		nodes.add(new WorldLocation(world, x, y, z));
		ArrayList<CrystalNetworkTile> li = net.getTilesWithinDofXYZ(world, x, y, z, stepRange, element);
		for (int i = 0; i < li.size(); i++) {
			CrystalNetworkTile te = li.get(i);
			if (this.lineOfSight(world, x, y, z, te)) {
				if (te instanceof CrystalTransmitter) {
					nodes.add(new WorldLocation(world, te.getX(), te.getY(), te.getZ()));
					return;
				}
				else if (te instanceof CrystalRepeater) {
					this.findFrom(te.getX(), te.getY(), te.getZ());
				}
			}
		}
		if (!this.isComplete())
			nodes.removeLast();
	}

	private boolean lineOfSight(World world, int x, int y, int z, CrystalNetworkTile te) {
		RayTracer ray = tracer.setOrigins(x, y, z, te.getX(), te.getY(), te.getZ());
		return ray.isClearLineOfSight(world);
	}

	static {
		tracer = new RayTracer(0, 0, 0, 0, 0, 0);
		tracer.setInternalOffsets(0.5, 0.5, 0.5);
		tracer.softBlocksOnly = true;/*
		tracer.addOpaqueBlock(Blocks.standing_sign);
		tracer.addOpaqueBlock(Blocks.reeds);
		tracer.addOpaqueBlock(Blocks.carpet);
		tracer.addOpaqueBlock(Blocks.tallgrass);
		tracer.addOpaqueBlock(Blocks.deadbush);
		tracer.addOpaqueBlock(Blocks.rail);
		tracer.addOpaqueBlock(Blocks.web);
		tracer.addOpaqueBlock(Blocks.torch);
		tracer.addOpaqueBlock(Blocks.redstone_torch);
		tracer.addOpaqueBlock(Blocks.unlit_redstone_torch);
		tracer.addOpaqueBlock(Blocks.powered_comparator);
		tracer.addOpaqueBlock(Blocks.unpowered_comparator);
		tracer.addOpaqueBlock(Blocks.powered_repeater);
		tracer.addOpaqueBlock(Blocks.unpowered_repeater);
		tracer.addOpaqueBlock(Blocks.wheat);
		tracer.addOpaqueBlock(Blocks.carrots);
		tracer.addOpaqueBlock(Blocks.potatoes);*/
	}

}