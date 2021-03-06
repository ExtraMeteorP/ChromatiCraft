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

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import Reika.ChromatiCraft.Base.GuiDescription;
import Reika.ChromatiCraft.Registry.ChromaResearch;
import Reika.ChromatiCraft.Registry.ChromaTiles;
import Reika.ChromatiCraft.Render.ISBRH.CrystalRenderer;
import Reika.DragonAPI.Libraries.IO.ReikaGuiAPI;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;

public class GuiMachineDescription extends GuiDescription {

	private float renderq = 22.5F;

	public GuiMachineDescription(EntityPlayer ep, ChromaResearch r) {
		super(ep, r, 256, 220);
	}

	@Override
	protected PageType getGuiLayout() {
		return PageType.PLAIN;
	}

	@Override
	public final void drawScreen(int x, int y, float f) {
		super.drawScreen(x, y, f);

		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2 - 8;

		if (subpage == 0)
			this.drawMachineRender(posX, posY);
	}

	private void drawMachineRender(int posX, int posY) {
		GL11.glTranslated(0, 0, 32);
		GL11.glColor4f(1, 1, 1, 1);
		double x = posX+167;
		double y = posY+44;
		//float q = 12.5F + fscale*(float)Math.sin(System.nanoTime()/1000000000D); //wobble
		//ReikaJavaLibrary.pConsole(y-ReikaGuiAPI.instance.getMouseScreenY(height));
		int range = 64;
		boolean rotate = ReikaGuiAPI.instance.isMouseInBox((int)x-range/2, (int)x+range/2, (int)y-range, (int)y+range);

		if (Mouse.isButtonDown(0) && rotate) {
			int mvy = Mouse.getDY();
			if (mvy < 0 && renderq < 45) {
				renderq++;
			}
			if (mvy > 0 && renderq > -45) {
				renderq--;
			}
		}
		y -= 8*Math.sin(Math.abs(Math.toRadians(renderq)));

		ChromaTiles m = page.getMachine();

		if (m.isPlant())
			renderq = 22.5F;

		GL11.glEnable(GL11.GL_BLEND);

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		double sc = 48;
		GL11.glPushMatrix();
		int r = (int)(System.nanoTime()/20000000)%360;
		if (m.isPlant())
			r = -45;
		if (m.hasRender()) {
			double dx = x;
			double dy = y+m.getRenderOffset();
			double dz = 0;
			GL11.glPushMatrix();
			GL11.glTranslated(dx, dy, dz);
			GL11.glScaled(sc, -sc, sc);
			GL11.glRotatef(renderq, 1, 0, 0);
			GL11.glRotatef(r, 0, 1, 0);
			TileEntityRendererDispatcher.instance.renderTileEntityAt(m.createTEInstanceForRender(), -0.5, 0, -0.5, 0);
			GL11.glPopMatrix();
		}
		if (m.hasBlockRender()) {
			double dx = x;
			double dy = y;
			double dz = 0;
			GL11.glPushMatrix();
			GL11.glTranslated(dx, dy, dz);
			GL11.glScaled(sc, -sc, sc);
			GL11.glRotatef(renderq, 1, 0, 0);
			GL11.glRotatef(r, 0, 1, 0);
			if (m == ChromaTiles.CRYSTAL) {
				GL11.glTranslated(-0.5, -0.33, -0.5);
				CrystalRenderer.renderAllArmsInInventory = true;
			}
			ReikaTextureHelper.bindTerrainTexture();
			rb.renderBlockAsItem(m.getBlock(), m.getBlockMetadata(), 1);
			CrystalRenderer.renderAllArmsInInventory = false;
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();

		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glTranslated(0, 0, -32);
	}

}
