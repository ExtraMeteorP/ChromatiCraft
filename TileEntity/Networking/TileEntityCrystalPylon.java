/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.TileEntity.Networking;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.wands.IWandable;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Auxiliary.ChromaOverlays;
import Reika.ChromatiCraft.Auxiliary.ChromaStructures;
import Reika.ChromatiCraft.Auxiliary.ProgressionManager.ProgressStage;
import Reika.ChromatiCraft.Auxiliary.Event.PylonEvents.PylonDrainedEvent;
import Reika.ChromatiCraft.Auxiliary.Event.PylonEvents.PylonFullyChargedEvent;
import Reika.ChromatiCraft.Auxiliary.Event.PylonEvents.PylonRechargedEvent;
import Reika.ChromatiCraft.Base.TileEntity.CrystalTransmitterBase;
import Reika.ChromatiCraft.Entity.EntityBallLightning;
import Reika.ChromatiCraft.Magic.CrystalPotionController;
import Reika.ChromatiCraft.Magic.ElementTagCompound;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalReceiver;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalRepeater;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalTransmitter;
import Reika.ChromatiCraft.Magic.Interfaces.NaturalCrystalSource;
import Reika.ChromatiCraft.ModInterface.ChromaAspectManager;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.ChromatiCraft.Registry.ChromaOptions;
import Reika.ChromatiCraft.Registry.ChromaPackets;
import Reika.ChromatiCraft.Registry.ChromaSounds;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Registry.Chromabilities;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.Render.Particle.EntityBallLightningFX;
import Reika.ChromatiCraft.Render.Particle.EntityBlurFX;
import Reika.ChromatiCraft.Render.Particle.EntityFlareFX;
import Reika.ChromatiCraft.Render.Particle.EntityRuneFX;
import Reika.ChromatiCraft.TileEntity.TileEntityChromaCrystal;
import Reika.ChromatiCraft.World.PylonGenerator;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.ASM.DependentMethodStripper.ModDependent;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.BlockArray;
import Reika.DragonAPI.Instantiable.Data.BlockStruct.FilledBlockArray;
import Reika.DragonAPI.Instantiable.Data.Immutable.Coordinate;
import Reika.DragonAPI.Libraries.ReikaAABBHelper;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaThaumHelper;
import Reika.DragonAPI.ModInteract.ItemHandlers.BloodMagicHandler;
import Reika.RotaryCraft.TileEntities.Weaponry.TileEntityEMP;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
//Make player able to manufacture in the very late game, otherwise rare worldgen
@Strippable(value = {"thaumcraft.api.nodes.INode", "thaumcraft.api.wands.IWandable"})
public class TileEntityCrystalPylon extends CrystalTransmitterBase implements NaturalCrystalSource, INode, IWandable {

	private boolean hasMultiblock = false;
	private boolean enhanced = false;
	private CrystalElement color = CrystalElement.WHITE;
	public int randomOffset = rand.nextInt(360);
	public static final int MAX_ENERGY = 180000;
	public static final int MAX_ENERGY_ENHANCED = 900000;
	private int energy = MAX_ENERGY;
	private int energyStep = 1;
	private long lastWorldTick;

	public static final int RANGE = 48;

	private static final Collection<Coordinate> crystalPositions = new ArrayList();

	private static Class node;
	private static HashMap<String, ArrayList<Integer>> nodeCache;

	static {
		if (ModList.THAUMCRAFT.isLoaded()) {
			try {
				node = Class.forName("thaumcraft.common.tiles.TileNode");
				Field f = node.getDeclaredField("locations");
				f.setAccessible(true);
				nodeCache = (HashMap<String, ArrayList<Integer>>)f.get(null);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		crystalPositions.add(new Coordinate(-3, -3, -1));
		crystalPositions.add(new Coordinate(-1, -3, -3));
		crystalPositions.add(new Coordinate(3, -3, -1));
		crystalPositions.add(new Coordinate(1, -3, -3));
		crystalPositions.add(new Coordinate(-3, -3, 1));
		crystalPositions.add(new Coordinate(-1, -3, 3));
		crystalPositions.add(new Coordinate(3, -3, 1));
		crystalPositions.add(new Coordinate(1, -3, 3));
	}

	@Override
	public ChromaTiles getTile() {
		return ChromaTiles.PYLON;
	}

	@Override
	public boolean isConductingElement(CrystalElement e) {
		return e == color;
	}

	@Override
	public boolean needsLineOfSight() {
		return true;
	}

	public CrystalElement getColor() {
		return color;
	}

	public int getEnergy(CrystalElement e) {
		return e == color ? energy : 0;
	}

	public int getRenderColor() {
		return ReikaColorAPI.mixColors(color.getColor(), 0x888888, (float)energy/this.getCapacity());
	}

	@Override
	protected void onFirstTick(World world, int x, int y, int z) {
		super.onFirstTick(world, x, y, z);
		PylonGenerator.instance.cachePylon(this);
		if (ModList.THAUMCRAFT.isLoaded() && nodeCache != null) {
			ArrayList li = new ArrayList();
			li.add(world.provider.dimensionId);
			li.add(x);
			li.add(y);
			li.add(z);
			nodeCache.put(this.getId(), li);
		}
	}

	@Override
	public void updateEntity(World world, int x, int y, int z, int meta) {
		super.updateEntity(world, x, y, z, meta);

		if (DragonAPICore.debugtest) {
			if (!hasMultiblock) {
				CrystalElement e = CrystalElement.randomElement();
				FilledBlockArray b = ChromaStructures.getPylonStructure(world, x, y-9, z, e);
				b.place();
				//world.setBlock(x, y+9, z, this.getTile().getBlock(), this.getTile().getBlockMetadata(), 3);
				//TileEntityCrystalPylon te = (TileEntityCrystalPylon)world.getTileEntity(x, y+9, z);
				color = e;
				hasMultiblock = true;
				this.syncAllData(true);
			}
		}

		if (hasMultiblock) {
			//ReikaJavaLibrary.pConsole(energy, Side.SERVER, color == CrystalElement.BLUE);

			int max = this.getCapacity();
			if (world.getTotalWorldTime() != lastWorldTick) {
				this.charge(world, x, y, z, max);
				lastWorldTick = world.getTotalWorldTime();
			}
			energy = Math.min(energy, max);

			if (world.isRemote) {
				this.spawnParticle(world, x, y, z);
			}

			if (!world.isRemote && rand.nextInt(80) == 0) {
				int r = 8+rand.nextInt(8);
				AxisAlignedBB box = ReikaAABBHelper.getBlockAABB(x, y, z).expand(r, r, r);
				List<EntityLivingBase> li = world.getEntitiesWithinAABB(EntityLivingBase.class, box);
				for (EntityLivingBase e : li) {
					boolean attack = !e.isDead && e.getHealth() > 0;
					if (e instanceof EntityPlayer) {
						EntityPlayer ep = (EntityPlayer)e;
						attack = attack && !ep.capabilities.isCreativeMode && !Chromabilities.PYLON.enabledOn(ep);
					}
					else if (e instanceof EntityBallLightning) {
						attack = ((EntityBallLightning)e).getElement() != color;
					}
					if (attack) {
						this.attackEntity(e);
						this.sendClientAttack(this, e);
					}
				}
			}

			if (this.getTicksExisted()%72 == 0) {
				ChromaSounds.POWER.playSoundAtBlock(this);
			}

			if (world.isRemote && rand.nextInt(36) == 0) {
				this.spawnLightning(world, x, y, z);
			}

			if (!world.isRemote && energy >= this.getCapacity()/2 && rand.nextInt(24000) == 0 && this.isChunkLoaded()) {
				world.spawnEntityInWorld(new EntityBallLightning(world, color, x+0.5, y+0.5, z+0.5).setPylon().setNoDrops());
			}
		}
	}

	@SideOnly(Side.CLIENT)
	private void spawnLightning(World world, int x, int y, int z) {
		EntityBallLightningFX e = new EntityBallLightningFX(world, x+0.5, y+0.5, z+0.5, color);
		e.setVelocity(0.125, rand.nextInt(360), 0);
		Minecraft.getMinecraft().effectRenderer.addEffect(e);
	}

	private void charge(World world, int x, int y, int z, int max) {
		int laste = energy;
		boolean lastconn = this.canConduct();

		if (energy < max) {
			energy += energyStep;
		}
		if (energyStep > 1)
			energyStep--;

		int a = 1;
		if (energy <= max-a) {
			ArrayList<TileEntityChromaCrystal> blocks = this.getBoosterCrystals(world, x, y, z);
			int c = this.isEnhanced() ? 3 : 2;
			for (int i = 0; i < blocks.size(); i++) {
				energy += a;
				a *= c;
				if (i == 7) { //8 crystals
					energy += a*2;
				}
				if (energy >= max) {
					return;
				}
			}
			if (blocks.size() > 0 && this.getTicksExisted()%875 == 0) {
				ChromaSounds.POWERCRYS.playSoundAtBlock(this);
			}
			if (blocks.size() == 8) {
				ProgressStage.POWERCRYSTAL.stepPlayerTo(blocks.get(0).getPlacer());
			}
			if (world.isRemote && !blocks.isEmpty())
				this.spawnRechargeParticles(world, x, y, z, blocks);
		}

		if (energy == this.getCapacity() && laste != this.getCapacity()) {
			MinecraftForge.EVENT_BUS.post(new PylonFullyChargedEvent(this));
		}
		if (this.canConduct() && !lastconn) {
			MinecraftForge.EVENT_BUS.post(new PylonRechargedEvent(this));
		}
	}

	public void speedRegenShortly() {
		energyStep = 5;
	}

	@SideOnly(Side.CLIENT)
	private void spawnRechargeParticles(World world, int x, int y, int z, ArrayList<TileEntityChromaCrystal> blocks) {
		int i = 0;
		for (TileEntityChromaCrystal te : blocks) {
			int dx = te.xCoord;
			int dy = te.yCoord;
			int dz = te.zCoord;
			double ddx = dx-x;
			double ddy = dy-y-0.25;
			double ddz = dz-z;
			double dd = ReikaMathLibrary.py3d(ddx, ddy, ddz);
			double v = 0.125;
			double vx = -v*ddx/dd;
			double vy = -v*ddy/dd;
			double vz = -v*ddz/dd;
			double px = dx+0.5;
			double py = dy+0.125;
			double pz = dz+0.5;
			//EntityRuneFX fx = new EntityRuneFX(world, dx+0.5, dy+0.5, dz+0.5, vx, vy, vz, color);
			float sc = (float)(2F+Math.sin(4*Math.toRadians(this.getTicksExisted()+i*90/blocks.size())));
			EntityBlurFX fx = new EntityBlurFX(color, world, px, py, pz, vx, vy, vz).setScale(sc).setLife(38).setNoSlowdown();
			//EntityLaserFX fx = new EntityLaserFX(color, world, px, py, pz, vx, vy, vz).setScale(3);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
			i++;
		}
	}

	private BlockArray getRuneLocations(World world, int x, int y, int z) {
		BlockArray blocks = new BlockArray();
		blocks.addBlockCoordinate(x-3, y-4, z-1);
		blocks.addBlockCoordinate(x-1, y-4, z-3);

		blocks.addBlockCoordinate(x+3, y-4, z-1);
		blocks.addBlockCoordinate(x+1, y-4, z-3);

		blocks.addBlockCoordinate(x-3, y-4, z+1);
		blocks.addBlockCoordinate(x-1, y-4, z+3);

		blocks.addBlockCoordinate(x+3, y-4, z+1);
		blocks.addBlockCoordinate(x+1, y-4, z+3);
		return blocks;
	}

	public ArrayList<TileEntityChromaCrystal> getBoosterCrystals(World world, int x, int y, int z) {
		ArrayList<TileEntityChromaCrystal> li = new ArrayList();
		EntityPlayer owner = null;
		for (Coordinate c : crystalPositions) {
			if (ChromaTiles.getTile(world, x+c.xCoord, y+c.yCoord, z+c.zCoord) == ChromaTiles.CRYSTAL) {
				TileEntityChromaCrystal te = (TileEntityChromaCrystal)world.getTileEntity(x+c.xCoord, y+c.yCoord, z+c.zCoord); {
					EntityPlayer ep = te.getPlacer();
					if (ep != null && (owner == null || ep == owner)) {
						if (owner == null)
							owner = ep;
						li.add(te);
					}
				}
			}
		}
		return li;
	}

	@SideOnly(Side.CLIENT)
	public void particleAttack(int sx, int sy, int sz, int x, int y, int z) {
		int n = 8+rand.nextInt(24);
		for (int i = 0; i < n; i++) {
			float rx = sx+rand.nextFloat();
			float ry = sy+rand.nextFloat();
			float rz = sz+rand.nextFloat();
			double dx = x-sx;
			double dy = y-sy;
			double dz = z-sz;
			double dd = ReikaMathLibrary.py3d(dx, dy, dz);
			double vx = 2*dx/dd;
			double vy = 2*dy/dd;
			double vz = 2*dz/dd;
			EntityFlareFX f = new EntityFlareFX(color, worldObj, rx, ry, rz, vx, vy, vz).setNoGravity();
			Minecraft.getMinecraft().effectRenderer.addEffect(f);
		}

		ChromaOverlays.instance.triggerPylonEffect(color);
	}

	void attackEntityByProxy(EntityPlayer player, CrystalRepeater te) {
		this.attackEntity(player);
		this.sendClientAttack(te, player);
	}

	void attackEntity(EntityLivingBase e) {
		ChromaSounds.DISCHARGE.playSoundAtBlock(this);
		ChromaSounds.DISCHARGE.playSound(worldObj, e.posX, e.posY, e.posZ, 1, 1);

		int amt = 5;

		if (e instanceof EntityPlayer) {
			EntityPlayer ep = (EntityPlayer)e;
			ProgressStage.SHOCK.stepPlayerTo(ep);
			//DO NOT UNCOMMENT, AS ALLOWS DISCOVERY OF ALL COLORS BEFORE PREREQ//ProgressionManager.instance.setPlayerDiscoveredColor(ep, color, true);
			if (ModList.BLOODMAGIC.isLoaded()) {
				int drain = 5000;
				if (BloodMagicHandler.getInstance().isPlayerWearingFullBoundArmor(ep)) {
					amt *= 10; //counter the 90% reduction
					drain = 50000;
				}
				SoulNetworkHandler.syphonFromNetwork(ep.getCommandSenderName(), drain);
			}

			if (e.ticksExisted < 600) {
				amt = 1; //1/2 heart for first 30s
			}
			else if (e.ticksExisted <= 1000) {
				amt = 1+(e.ticksExisted-600)/100; //increase by 1/2 heart every 5 seconds, up to 2.5 hearts at 50 seconds
			}
		}

		float last = e.getHealth();

		e.attackEntityFrom(ChromatiCraft.pylon, amt);

		if (e.getHealth() > last-amt) {
			if (amt > last) { //kill
				e.setHealth(0.1F);
				e.attackEntityFrom(ChromatiCraft.pylon, Float.MAX_VALUE);
			}
			else
				e.setHealth(last-amt);
		}

		PotionEffect eff = CrystalPotionController.getEffectFromColor(color, 200, 2);
		if (eff != null) {
			e.addPotionEffect(eff);
		}
	}

	private void sendClientAttack(CrystalTransmitter te, EntityLivingBase e) {
		int tx = te.getX();
		int ty = te.getY();
		int tz = te.getZ();
		int x = MathHelper.floor_double(e.posX);
		int y = MathHelper.floor_double(e.posY)+1;
		int z = MathHelper.floor_double(e.posZ);
		ReikaPacketHelper.sendDataPacket(ChromatiCraft.packetChannel, ChromaPackets.PYLONATTACK.ordinal(), this, tx, ty, tz, x, y, z);
	}

	public void invalidateMultiblock() {
		if (hasMultiblock) {
			ChromaSounds.POWERDOWN.playSoundAtBlock(this);
			ChromaSounds.POWERDOWN.playSound(worldObj, xCoord, yCoord, zCoord, 1F, 2F);
			ChromaSounds.POWERDOWN.playSound(worldObj, xCoord, yCoord, zCoord, 1F, 0.5F);

			if (worldObj.isRemote)
				this.invalidatationParticles();
		}
		hasMultiblock = false;
		this.clearTargets(false);
		energy = 0;
		this.syncAllData(true);
	}

	@SideOnly(Side.CLIENT)
	private void invalidatationParticles() {
		double d = 1.25;
		int n = 64+rand.nextInt(64);
		for (int i = 0; i < n; i++) {
			double rx = ReikaRandomHelper.getRandomPlusMinus(xCoord+0.5, d);
			double ry = ReikaRandomHelper.getRandomPlusMinus(yCoord+0.5, d);
			double rz = ReikaRandomHelper.getRandomPlusMinus(zCoord+0.5, d);
			double vx = rand.nextDouble()-0.5;
			double vy = rand.nextDouble()-0.5;
			double vz = rand.nextDouble()-0.5;
			EntityRuneFX fx = new EntityRuneFX(worldObj, rx, ry, rz, vx, vy, vz, color);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	public void validateMultiblock() {
		hasMultiblock = true;
		this.syncAllData(true);
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticle(World world, int x, int y, int z) {
		int p = Minecraft.getMinecraft().gameSettings.particleSetting;
		if (rand.nextInt(1+p/2) == 0) {
			double d = 1.25;
			double rx = ReikaRandomHelper.getRandomPlusMinus(x+0.5, d);
			double ry = ReikaRandomHelper.getRandomPlusMinus(y+0.5, d);
			double rz = ReikaRandomHelper.getRandomPlusMinus(z+0.5, d);
			EntityFlareFX fx = new EntityFlareFX(color, world, rx, ry, rz);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	protected void readSyncTag(NBTTagCompound NBT) {
		super.readSyncTag(NBT);

		color = CrystalElement.elements[NBT.getInteger("color")];
		hasMultiblock = NBT.getBoolean("multi");
		energy = NBT.getInteger("energy");
		enhanced = NBT.getBoolean("enhance");
	}

	@Override
	protected void writeSyncTag(NBTTagCompound NBT) {
		super.writeSyncTag(NBT);

		NBT.setInteger("color", color.ordinal());
		NBT.setBoolean("multi", hasMultiblock);
		NBT.setInteger("energy", energy);
		NBT.setBoolean("enhance", enhanced);
	}

	@Override
	public int getSendRange() {
		return RANGE;
	}

	@Override
	public boolean canConduct() {
		return hasMultiblock && energy >= 5000;
	}

	@Override
	public int maxThroughput() {
		int base = this.isEnhanced() ? 15000 : 5000;
		int thresh = this.getCapacity()/4;
		return energy >= thresh ? base : this.getReducedThroughput(thresh, base);
	}

	private int getReducedThroughput(int thresh, int max) {
		int sigx = energy/(thresh/12)-6;
		int sig = (int)(max/(1+Math.pow(Math.E, -sigx)));
		return Math.max(0, Math.min(energy-1, sig-10));
	}

	@Override
	public int getTransmissionStrength() {
		return this.isEnhanced() ? 50000 : 10000;
	}

	public void generateColor(CrystalElement e) {
		color = e;
	}

	public void setColor(CrystalElement e) {
		if (worldObj.isRemote)
			return;
		color = e;
		BlockArray runes = this.getRuneLocations(worldObj, xCoord, yCoord, zCoord);
		for (int i = 0; i < runes.getSize(); i++) {
			Coordinate c = runes.getNthBlock(i);
			if (c.getBlock(worldObj) == ChromaBlocks.RUNE.getBlockInstance())
				worldObj.setBlockMetadataWithNotify(c.xCoord, c.yCoord, c.zCoord, color.ordinal(), 3);
		}
	}

	@Override
	public boolean drain(CrystalElement e, int amt) {
		if (e == color && energy >= amt && amt > 0) {
			energy -= amt;
			if (energy == 0) {
				MinecraftForge.EVENT_BUS.post(new PylonDrainedEvent(this));
			}
			return true;
		}
		return false;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public AspectList getAspects() {
		AspectList as = new AspectList();
		as.add(Aspect.AURA, 400);
		Collection<Aspect> li = ChromaAspectManager.instance.getAspects(this.getColor(), true);
		for (Aspect a : li) {
			as.add(a, 400);
		}
		return as;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void setAspects(AspectList aspects) {}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public boolean doesContainerAccept(Aspect tag) {
		return this.getAspects().getAmount(tag) > 0;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public int addToContainer(Aspect tag, int amount) {return 0;}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public boolean takeFromContainer(Aspect tag, int amount) {
		return this.doesContainerContainAmount(tag, amount);
	}

	@Override
	@Deprecated
	@ModDependent(ModList.THAUMCRAFT)
	public boolean takeFromContainer(AspectList ot) {
		return false;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public boolean doesContainerContainAmount(Aspect tag, int amount) {
		return this.getAspects().getAmount(tag) > amount;
	}

	@Override
	@Deprecated
	@ModDependent(ModList.THAUMCRAFT)
	public boolean doesContainerContain(AspectList ot) {
		return false;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public int containerContains(Aspect tag) {
		return this.getAspects().getAmount(tag);
	}

	@Override
	public String getId() { //Normally based on world coords, but uses just color to make each pylon color scannable once
		return "Pylon_"+color.toString();//"Pylon_"+worldObj.provider.dimensionId+":"+xCoord+":"+yCoord+":"+zCoord;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public AspectList getAspectsBase() {
		return this.getAspects();
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public NodeType getNodeType() {
		switch(color) {
		case BLACK:
			return NodeType.DARK;
		case GRAY:
			return NodeType.UNSTABLE;
		case WHITE:
			return NodeType.PURE;
		default:
			return NodeType.NORMAL;
		}
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void setNodeType(NodeType nodeType) {}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void setNodeModifier(NodeModifier nodeModifier) {}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public NodeModifier getNodeModifier() {
		return NodeModifier.BRIGHT;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public int getNodeVisBase(Aspect aspect) {
		return this.containerContains(aspect);
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void setNodeVisBase(Aspect aspect, short nodeVisBase) {}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int mode) {
		return -1;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player) {
		player.setItemInUse(wandstack, Integer.MAX_VALUE);
		ReikaThaumHelper.setWandInUse(wandstack, this);
		return wandstack;
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count) {
		if (!worldObj.isRemote && this.canConduct() && player.ticksExisted%5 == 0) {
			if (!ChromaOptions.HARDTHAUM.getState() || ReikaThaumHelper.isResearchComplete(player, "NODETAPPER2")) {
				AspectList al = ReikaThaumHelper.decompose(this.getAspects());
				for (Aspect a : al.aspects.keySet()) {
					int amt = 2;
					if (ReikaThaumHelper.isResearchComplete(player, "NODETAPPER1"))
						amt *= 2;
					if (ReikaThaumHelper.isResearchComplete(player, "NODETAPPER2"))
						amt *= 2;
					amt = Math.min(amt, al.getAmount(a));
					amt = Math.min(amt, ReikaThaumHelper.getWandSpaceFor(wandstack, a));
					int ret = ReikaThaumHelper.addVisToWand(wandstack, a, amt);
					int added = amt-ret;
					if (added > 0) {
						this.drain(color, Math.min(energy, energy-added*48));
					}
				}
			}
		}
	}

	@Override
	@ModDependent(ModList.THAUMCRAFT)
	public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count) {

	}

	public final ElementTagCompound getEnergy() {
		ElementTagCompound tag = new ElementTagCompound();
		tag.setTag(color, energy);
		return tag;
	}

	@Override
	public int getMaxStorage(CrystalElement e) {
		return this.getCapacity();
	}

	private int getCapacity() {
		return this.isEnhanced() ? MAX_ENERGY_ENHANCED : MAX_ENERGY;
	}

	public boolean isEnhanced() {
		return enhanced && this.canConduct();
	}

	@Override
	public int getSourcePriority() {
		return 0;
	}

	@Override
	public boolean canTransmitTo(CrystalReceiver te) {
		return true;
	}

	@Override
	public boolean regeneratesEnergy() {
		return true;
	}

	@ModDependent(ModList.ROTARYCRAFT)
	public void onEMP(TileEntityEMP te) {
		energy = rand.nextBoolean() ? 0 : this.getCapacity();
		worldObj.createExplosion(null, xCoord+0.5, yCoord+0.5, zCoord+0.5, 16, false);
		ChromaSounds.DISCHARGE.playSoundAtBlock(this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public final double getMaxRenderDistanceSquared() {
		return 65536D;
	}

}
