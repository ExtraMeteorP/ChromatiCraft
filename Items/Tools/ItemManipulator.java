/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.Items.Tools;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.IScribeTools;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.nodes.INode;
import Reika.ChromatiCraft.Auxiliary.ChromaFX;
import Reika.ChromatiCraft.Auxiliary.ProgressionManager;
import Reika.ChromatiCraft.Auxiliary.ProgressionManager.ProgressStage;
import Reika.ChromatiCraft.Auxiliary.Interfaces.SneakPop;
import Reika.ChromatiCraft.Base.ItemChromaTool;
import Reika.ChromatiCraft.Block.Crystal.BlockCrystalGlow.TileEntityCrystalGlow;
import Reika.ChromatiCraft.Block.Crystal.BlockPowerTree.TileEntityPowerTreeAux;
import Reika.ChromatiCraft.Magic.PlayerElementBuffer;
import Reika.ChromatiCraft.Magic.Interfaces.CrystalSource;
import Reika.ChromatiCraft.ModInterface.NodeRecharger;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.ChromatiCraft.Registry.ChromaSounds;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.Render.Particle.EntityRuneFX;
import Reika.ChromatiCraft.TileEntity.TileEntityPowerTree;
import Reika.ChromatiCraft.TileEntity.Acquisition.TileEntityMiner;
import Reika.ChromatiCraft.TileEntity.Networking.TileEntityCompoundRepeater;
import Reika.ChromatiCraft.TileEntity.Networking.TileEntityCrystalPylon;
import Reika.ChromatiCraft.TileEntity.Networking.TileEntityCrystalRepeater;
import Reika.ChromatiCraft.TileEntity.Recipe.TileEntityCastingTable;
import Reika.ChromatiCraft.TileEntity.Recipe.TileEntityRitualTable;
import Reika.ChromatiCraft.TileEntity.Transport.TileEntityItemRift;
import Reika.ChromatiCraft.TileEntity.Transport.TileEntityRift;
import Reika.DragonAPI.APIPacketHandler.PacketIDs;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.DragonAPIInit;
import Reika.DragonAPI.ModList;
import Reika.DragonAPI.ASM.APIStripper.Strippable;
import Reika.DragonAPI.Libraries.ReikaPlayerAPI;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaSoundHelper;
import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
import Reika.DragonAPI.ModInteract.DeepInteract.ReikaThaumHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Strippable("thaumcraft.api.IScribeTools")
public class ItemManipulator extends ItemChromaTool implements IScribeTools {

	public ItemManipulator(int index) {
		super(index);
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer ep, World world, int x, int y, int z, int s, float a, float b, float c) {
		if (ReikaPlayerAPI.isFakeOrNotInteractable(ep, x+0.5, y+0.5, z+0.5, 8))
			return false;
		ChromaTiles t = ChromaTiles.getTile(world, x, y, z);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof SneakPop && ep.isSneaking()) {
			((SneakPop)tile).drop();
			ChromaSounds.RIFT.playSoundAtBlock(tile);
			return true;
		}
		if (t == ChromaTiles.RIFT) {
			TileEntityRift te = (TileEntityRift)tile;
			te.setDirection(ForgeDirection.VALID_DIRECTIONS[s]);
			return true;
		}
		if (t == ChromaTiles.TABLE) {
			boolean flag = ((TileEntityCastingTable)tile).triggerCrafting(ep);
			return flag;
		}
		if (t == ChromaTiles.RITUAL) {
			boolean flag = ((TileEntityRitualTable)tile).triggerRitual(ep);
			return flag;
		}
		if (t == ChromaTiles.MINER) {
			((TileEntityMiner)tile).triggerDigging();
			return true;
		}
		if (t == ChromaTiles.ITEMRIFT) {
			TileEntityItemRift ir = (TileEntityItemRift)tile;
			ir.isEmitting = !ir.isEmitting;
			return true;
		}
		//if (t == ChromaTiles.FIBERSINK) {
		//	TileEntityFiberTransmitter ft = (TileEntityFiberTransmitter)tile;
		//	ft.setFacing(ForgeDirection.VALID_DIRECTIONS[s]);
		//	return true;
		//}

		if (t == ChromaTiles.PYLON && ep.capabilities.isCreativeMode && DragonAPICore.debugtest) {
			TileEntityCrystalPylon cp = (TileEntityCrystalPylon)tile;
			cp.setColor(CrystalElement.elements[(cp.getColor().ordinal()+1)%16]);
			return true;
		}

		if (t == ChromaTiles.COMPOUND) {
			TileEntityCompoundRepeater te = (TileEntityCompoundRepeater)tile;
			if (ep.isSneaking()) {
				if (te.isPlacer(ep)) {
					//world.setBlock(x, y, z, Blocks.air);
					//ReikaItemHelper.dropItem(world, x+0.5, y+0.5, z+0.5, ChromaTiles.COMPOUND.getCraftedProduct());
					ReikaSoundHelper.playSoundAtBlock(world, x, y, z, Block.soundTypeStone.getStepResourcePath(), 2, 0.5F);
					te.redirect(s);
				}
			}
			else if (!world.isRemote) {
				if (te.checkConnectivity()) {
					CrystalElement e = te.getActiveColor();
					ChromaSounds.CAST.playSoundAtBlock(world, x, y, z);
					int rd = e.getRed();
					int gn = e.getGreen();
					int bl = e.getBlue();
					ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.COLOREDPARTICLE.ordinal(), te, rd, gn, bl, 32, 8);
					ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.NUMBERPARTICLE.ordinal(), te, te.getSignalDepth(e));
				}
				else {
					ChromaSounds.ERROR.playSoundAtBlock(world, x, y, z);
				}
			}
			return true;
		}
		if (t == ChromaTiles.REPEATER) {
			TileEntityCrystalRepeater te = (TileEntityCrystalRepeater)tile;
			if (ep.isSneaking()) {
				if (te.isPlacer(ep)) {
					//world.setBlock(x, y, z, Blocks.air);
					//ReikaItemHelper.dropItem(world, x+0.5, y+0.5, z+0.5, ChromaTiles.REPEATER.getCraftedProduct());
					ReikaSoundHelper.playSoundAtBlock(world, x, y, z, Block.soundTypeStone.getStepResourcePath(), 2, 0.5F);
					te.redirect(s);
				}
			}
			else if (!world.isRemote) {
				if (te.checkConnectivity()) {
					CrystalElement e = te.getActiveColor();
					ChromaSounds.CAST.playSoundAtBlock(world, x, y, z);
					int rd = e.getRed();
					int gn = e.getGreen();
					int bl = e.getBlue();
					ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.COLOREDPARTICLE.ordinal(), te, rd, gn, bl, 32, 8);
					ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.NUMBERPARTICLE.ordinal(), te, te.getSignalDepth(e));
				}
				else {
					ChromaSounds.ERROR.playSoundAtBlock(world, x, y, z);
				}
			}
			return true;
		}
		if (tile instanceof TileEntityCrystalGlow) {
			if (ep.isSneaking())
				((TileEntityCrystalGlow)tile).toggle();
			else
				((TileEntityCrystalGlow)tile).rotate();
			ReikaSoundHelper.playBreakSound(world, x, y, z, Blocks.stone, 0.35F, 0.05F);
		}

		if (ModList.THAUMCRAFT.isLoaded() && tile instanceof INode) {
			if (ProgressStage.CTM.isPlayerAtStage(ep) && ReikaThaumHelper.isResearchComplete(ep, "NODESTABILIZERADV")) { //CC and TC progression
				if (!world.isRemote) {
					NodeRecharger.instance.addNode((INode)tile);
					ReikaSoundHelper.playSoundFromServer(world, x+0.5, y+0.5, z+0.5, "thaumcraft:runicShieldEffect", 1, 1, false);
					for (Aspect asp : ((INode)tile).getAspects().aspects.keySet()) {
						int color = asp.getColor();
						int rd = ReikaColorAPI.getRed(color);
						int gn = ReikaColorAPI.getGreen(color);
						int bl = ReikaColorAPI.getBlue(color);
						ReikaPacketHelper.sendDataPacket(DragonAPIInit.packetChannel, PacketIDs.COLOREDPARTICLE.ordinal(), tile, rd, gn, bl, 8, 2);
					}
					for (Aspect asp : ((INode)tile).getAspects().aspects.keySet()) {
						((INode)tile).takeFromContainer(asp, 1);
					}
				}
			}
		}

		return false;
	}

	@Override
	public ItemStack onEaten(ItemStack is, World world, EntityPlayer ep)
	{
		return is;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack is) {
		return 72000;
	}

	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count) {
		MovingObjectPosition mov = ReikaPlayerAPI.getLookedAtBlock(player, 24, false);
		if (DragonAPICore.debugtest) {
			if (player.isSneaking())
				player.getEntityData().removeTag("CrystalBuffer");
			else {
				PlayerElementBuffer.instance.addToPlayer(player, CrystalElement.elements[count%16], PlayerElementBuffer.instance.getElementCap(player)/2);
				PlayerElementBuffer.instance.checkUpgrade(player, true);
			}
		}

		if (ProgressStage.PYLON.isPlayerAtStage(player)) {
			if (mov != null) {
				World world = player.worldObj;
				int x = mov.blockX;
				int y = mov.blockY;
				int z = mov.blockZ;
				ChromaTiles c = ChromaTiles.getTile(world, x, y, z);
				if (c == ChromaTiles.PYLON) {
					TileEntityCrystalPylon te = (TileEntityCrystalPylon)world.getTileEntity(x, y, z);
					CrystalElement e = te.getColor();
					this.chargeFromPylon(player, te, e, count);
				}
				else if (world.getBlock(x, y, z) == ChromaBlocks.POWERTREE.getBlockInstance()) {
					TileEntityPowerTree te = ((TileEntityPowerTreeAux)world.getTileEntity(x, y, z)).getCenter();
					if (te != null && this.chargeFromPylon(player, te, CrystalElement.elements[world.getBlockMetadata(x, y, z)], count)) {

					}
				}
			}
		}
	}

	private boolean chargeFromPylon(EntityPlayer player, CrystalSource te, CrystalElement e, int count) {
		int add = PlayerElementBuffer.instance.getChargeSpeed(player);
		int drain = add*4;
		int energy = te.getEnergy(e);
		if (drain > energy) {
			drain = energy;
			add = drain/4;
		}
		if (te.canConduct() && add > 0 && PlayerElementBuffer.instance.canPlayerAccept(player, e, add)) {
			if (PlayerElementBuffer.instance.addToPlayer(player, e, add))
				te.drain(e, drain);
			PlayerElementBuffer.instance.checkUpgrade(player, true);
			ProgressStage.CHARGE.stepPlayerTo(player);
			if (te instanceof TileEntityCrystalPylon)
				ProgressionManager.instance.setPlayerDiscoveredColor(player, ((TileEntityCrystalPylon)te).getColor(), true);
			if (player.worldObj.isRemote) {
				//this.spawnParticles(player, e);
				ChromaFX.createPylonChargeBeam(te, player, (count%20)/20D, e);
			}
			return true;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	private void spawnParticles(EntityPlayer player, CrystalElement e) {
		double rx = ReikaRandomHelper.getRandomPlusMinus(player.posX, 0.8);
		double ry = ReikaRandomHelper.getRandomPlusMinus(player.posY, 1.5);
		double rz = ReikaRandomHelper.getRandomPlusMinus(player.posZ, 0.8);
		Minecraft.getMinecraft().effectRenderer.addEffect(new EntityRuneFX(player.worldObj, rx, ry, rz, e));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack is, World world, EntityPlayer ep)
	{
		ep.setItemInUse(is, this.getMaxItemUseDuration(is));
		return is;
	}

	@Override
	public EnumAction getItemUseAction(ItemStack is) {
		return EnumAction.bow;
	}

}
