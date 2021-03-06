/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.ChromatiCraft.GUI.Book;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import Reika.ChromatiCraft.Auxiliary.ChromaDescriptions;
import Reika.ChromatiCraft.Base.CrystalBlock;
import Reika.ChromatiCraft.Base.GuiBookSection;
import Reika.ChromatiCraft.Registry.ChromaBlocks;
import Reika.ChromatiCraft.Registry.ChromaIcons;
import Reika.ChromatiCraft.Registry.ChromaResearch;
import Reika.ChromatiCraft.Registry.CrystalElement;
import Reika.ChromatiCraft.Render.ISBRH.CrystalRenderer;
import Reika.DragonAPI.Libraries.IO.ReikaColorAPI;
import Reika.DragonAPI.Libraries.IO.ReikaRenderHelper;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;
import Reika.DragonAPI.Libraries.Java.ReikaGLHelper.BlendMode;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;

public class GuiBasicInfo extends GuiBookSection {

	private Collection<PylonParticle> particles = new ArrayList();

	public GuiBasicInfo(EntityPlayer ep, ChromaResearch r) {
		super(ep, r, 256, 220, false);
	}

	@Override
	protected int getMaxSubpage() {
		return page == ChromaResearch.ELEMENTS ? CrystalElement.elements.length : 0;
	}

	@Override
	protected PageType getGuiLayout() {
		return this.isElementPage() ? PageType.ELEMENT : PageType.PLAIN;
	}

	@Override
	public void drawScreen(int mx, int my, float f) {
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2 - 8;
		super.drawScreen(mx, my, f);

		int c = 0xffffff;
		int px = posX+descX;
		if (subpage == 0 || page.sameTextAllSubpages()) {
			fontRendererObj.drawSplitString(String.format("%s", page.getData()), px, posY+descY, 242, c);
		}
		if (this.isElementPage()) {
			this.renderElementPage(CrystalElement.elements[subpage-1], posX, posY, px, c);
		}
		else if (page == ChromaResearch.CRYSTALS) {
			this.renderCrystal(posX, posY);
		}
		else if (page == ChromaResearch.PYLONS) {
			this.renderPylon(posX, posY);
		}
	}

	private void renderPylon(int posX, int posY) {
		float mod = 2000F;
		int tick = (int)((System.currentTimeMillis()/(double)mod)%16);
		CrystalElement e1 = CrystalElement.elements[tick];
		CrystalElement e2 = CrystalElement.elements[(tick+1)%16];
		float mix = (float)(System.currentTimeMillis()%(double)mod)/mod;
		mix = Math.min(mix*2, 1);
		int c1 = e1.getColor();
		int c2 = e2.getColor();
		int color = ReikaColorAPI.mixColors(c2, c1, mix);
		ReikaTextureHelper.bindTerrainTexture();
		Tessellator v5 = Tessellator.instance;
		v5.setBrightness(240);
		v5.startDrawingQuads();
		v5.setColorOpaque_I(color);
		IIcon ico = ChromaIcons.ROUNDFLARE.getIcon();
		GL11.glEnable(GL11.GL_BLEND);
		BlendMode.ADDITIVEDARK.apply();
		float u = ico.getMinU();
		float v = ico.getMinV();
		float du = ico.getMaxU();
		float dv = ico.getMaxV();
		int w = 96;
		int x = posX+115;
		int y = posY-4;
		v5.addVertexWithUV(x, y+w, 0, u, dv);
		v5.addVertexWithUV(x+w, y+w, 0, du, dv);
		v5.addVertexWithUV(x+w, y, 0, du, v);
		v5.addVertexWithUV(x, y, 0, u, v);

		ico = ChromaIcons.BIGFLARE.getIcon();
		u = ico.getMinU();
		v = ico.getMinV();
		du = ico.getMaxU();
		dv = ico.getMaxV();
		w = 8;

		Iterator<PylonParticle> it = particles.iterator();
		while (it.hasNext()) {
			PylonParticle p = it.next();
			v5.addVertexWithUV(p.posX, p.posY+w, 0, u, dv);
			v5.addVertexWithUV(p.posX+w, p.posY+w, 0, du, dv);
			v5.addVertexWithUV(p.posX+w, p.posY, 0, du, v);
			v5.addVertexWithUV(p.posX, p.posY, 0, u, v);

			p.move(180D/ReikaRenderHelper.getFPS());

			p.age++;
			if (!ReikaMathLibrary.isValueInsideBounds(posX, posX+xSize-8, p.posX) || !ReikaMathLibrary.isValueInsideBounds(posY, posY+80, p.posY)) {
				it.remove();
			}
		}

		v5.draw();

		if (rand.nextInt(50) == 0) {
			particles.add(new PylonParticle(245, 40, rand.nextInt(360)));
		}

		BlendMode.DEFAULT.apply();
	}

	private void renderCrystal(int posX, int posY) {
		float mod = 2000F;
		int tick = (int)((System.currentTimeMillis()/(double)mod)%16);
		CrystalElement e1 = CrystalElement.elements[tick];
		CrystalElement e2 = CrystalElement.elements[(tick+1)%16];
		float mix = (float)(System.currentTimeMillis()%(double)mod)/mod;
		mix = Math.min(mix*2, 1);
		int c1 = ((CrystalBlock)ChromaBlocks.CRYSTAL.getBlockInstance()).getTintColor(e1.ordinal());
		int c2 = ((CrystalBlock)ChromaBlocks.CRYSTAL.getBlockInstance()).getTintColor(e2.ordinal());
		int color = ReikaColorAPI.mixColors(c2, c1, mix);
		CrystalRenderer.staticColor = color;
		this.drawBlockRender(posX, posY, ChromaBlocks.CRYSTAL.getBlockInstance(), CrystalElement.WHITE.ordinal());
		CrystalRenderer.staticColor = -1;
	}

	private boolean isElementPage() {
		return page == ChromaResearch.ELEMENTS && subpage > 0;
	}

	private void renderElementPage(CrystalElement e, int posX, int posY, int px, int c) {
		String s = ChromaDescriptions.getElementDescription(e);
		fontRendererObj.drawSplitString(String.format("%s", s), px, posY+descY, 242, c);
		IIcon ico = e.getGlowRune();
		ReikaTextureHelper.bindTerrainTexture();
		this.drawTexturedModelRectFromIcon(posX+153, posY+12, ico, 64, 64);
	}

	private static class PylonParticle {

		private double posX;
		private double posY;
		private double angle;
		private int age;

		private PylonParticle(int x, int y, double ang) {
			posX = x;
			posY = y;
			angle = Math.toRadians(ang);
		}

		private void move(double sp) {
			posX += sp*Math.cos(angle);
			posY += sp*Math.sin(angle);
		}

	}

}
