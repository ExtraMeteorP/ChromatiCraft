/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.World.Dimension;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.client.IRenderHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class WorldProviderChroma extends WorldProvider {

	private ChunkProviderChroma chunkGen;

	public WorldProviderChroma() {

	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean getWorldHasVoidParticles()
	{
		return false;
	}

	@Override
	protected void generateLightBrightnessTable()
	{
		super.generateLightBrightnessTable();
		for (int i = 0; i < lightBrightnessTable.length; i++) {
			//lightBrightnessTable[i] = Math.max(0, lightBrightnessTable[i]*4F-3);
		}
	}

	@Override
	public float calculateCelestialAngle(long time, float ptick)
	{
		return 0.5F;//super.calculateCelestialAngle(time, ptick);
	}

	@Override
	public double getVoidFogYFactor()
	{
		return 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean doesXZShowFog(int x, int z)
	{
		return false;
	}

	@Override
	public boolean shouldMapSpin(String entity, double x, double y, double z)
	{
		return true;
	}

	@Override
	public boolean isDaytime()
	{
		return false;
	}

	@Override
	public float getSunBrightnessFactor(float par1)
	{
		return worldObj.getSunBrightnessFactor(par1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Vec3 getSkyColor(Entity cameraEntity, float partialTicks)
	{
		return worldObj.getSkyColorBody(cameraEntity, partialTicks);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getSunBrightness(float par1)
	{
		return worldObj.getSunBrightnessBody(par1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getStarBrightness(float par1)
	{
		return worldObj.getStarBrightnessBody(par1);
	}

	@Override
	public void calculateInitialWeather()
	{

	}

	@Override
	public void updateWeather()
	{

	}

	@Override
	public long getSeed()
	{
		return worldObj.getWorldInfo().getSeed();
	}

	@Override
	public long getWorldTime()
	{
		return worldObj.getWorldInfo().getWorldTime();
	}

	@Override
	public boolean canMineBlock(EntityPlayer player, int x, int y, int z)
	{
		return worldObj.canMineBlockBody(player, x, y, z);
	}

	@Override
	public String getWelcomeMessage()
	{
		return "";
	}

	@Override
	public String getDepartMessage()
	{
		return "";
	}

	@Override
	public double getMovementFactor()
	{
		return 1;//ReikaRandomHelper.getRandomPlusMinus(4, 3.5);
	}

	@Override
	public int getHeight()
	{
		return 256; //1024?
	}

	@Override
	public IChunkProvider createChunkGenerator()
	{
		return this.getChunkGenerator();
	}

	public ChunkProviderChroma getChunkGenerator() {
		if (chunkGen == null)
			chunkGen = new ChunkProviderChroma(worldObj);
		return chunkGen;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getSkyRenderer()
	{
		return ChromaSkyRenderer.instance;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getCloudRenderer()
	{
		return ChromaCloudRenderer.instance;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IRenderHandler getWeatherRenderer()
	{
		return super.getWeatherRenderer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getCloudHeight()
	{
		double d = Math.sin(System.currentTimeMillis()/250000D);
		return 128+(float)(64*d);//512;
	}

	@Override
	public Vec3 getFogColor(float celang, float ptick)
	{
		return Vec3.createVectorHelper(0, 0, 0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float[] calcSunriseSunsetColors(float celang, float ptick)
	{
		float[] ret = super.calcSunriseSunsetColors(celang, ptick);
		return ret;
	}

	@Override
	public boolean isSurfaceWorld()
	{
		return true;//false;//false; //return false makes sun and the like not render
	}

	@Override
	public boolean canRespawnHere()
	{
		return false;
	}

	@Override
	public String getDimensionName() {
		return "Chroma";
	}

}
