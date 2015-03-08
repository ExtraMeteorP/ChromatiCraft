package Reika.ChromatiCraft.GUI;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import Reika.ChromatiCraft.ChromatiCraft;
import Reika.ChromatiCraft.Auxiliary.ChromaFontRenderer;
import Reika.ChromatiCraft.Items.Tools.Wands.ItemTransitionWand;
import Reika.ChromatiCraft.Items.Tools.Wands.ItemTransitionWand.TransitionMode;
import Reika.ChromatiCraft.Registry.ChromaPackets;
import Reika.DragonAPI.Instantiable.GUI.ImagedGuiButton;
import Reika.DragonAPI.Instantiable.IO.PacketTarget;
import Reika.DragonAPI.Libraries.IO.ReikaGuiAPI;
import Reika.DragonAPI.Libraries.IO.ReikaPacketHelper;
import Reika.DragonAPI.Libraries.IO.ReikaTextureHelper;

public class GuiTransitionWand extends GuiScreen {

	private final EntityPlayer player;

	private int xSize = 176;
	private int ySize = 52;

	public GuiTransitionWand(EntityPlayer ep) {
		player = ep;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		String tex = "Textures/GUIs/buttons.png";

		int l = TransitionMode.list.length;
		TransitionMode md = this.getCurrentMode();
		for (int i = 0; i < l; i++) {
			int u = md.ordinal() == i ? 18 : 0;
			buttonList.add(new ImagedGuiButton(i, j+8+i*(xSize-10)/l, k+14, 18, 18, u, 0, tex, ChromatiCraft.class));
		}
	}

	@Override
	protected void actionPerformed(GuiButton b) {
		this.getItem().setMode(player.getCurrentEquippedItem(), TransitionMode.list[b.id]);
		ReikaPacketHelper.sendDataPacket(ChromatiCraft.packetChannel, ChromaPackets.TRANSITIONWAND.ordinal(), new PacketTarget.ServerTarget(), b.id);
		this.initGui();
	}

	@Override
	public void drawScreen(int x, int y, float ptick) {
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		ReikaTextureHelper.bindTexture(ChromatiCraft.class, "Textures/GUIs/transition.png");
		ReikaGuiAPI.instance.drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		int tx = j+xSize/2;
		int ty = k+4;
		ReikaGuiAPI.instance.drawCenteredStringNoShadow(ChromaFontRenderer.FontType.GUI.renderer, "Transition Wand Mode Selection", tx, ty, 0xffffff);

		int l = TransitionMode.list.length;
		TransitionMode md = this.getCurrentMode();
		for (int i = 0; i < l; i++) {
			int color = md.ordinal() == i ? 0x00ff00 : 0xffffff;
			int w = (xSize-10)/l;
			tx = j+8+i*w;
			ty = k+33;
			ChromaFontRenderer.FontType.GUI.renderer.drawSplitString(TransitionMode.list[i].desc, tx, ty, w, color);
		}

		super.drawScreen(x, y, ptick);
	}

	private TransitionMode getCurrentMode() {
		return this.getItem().getMode(player.getCurrentEquippedItem());
	}

	private ItemTransitionWand getItem() {
		return (ItemTransitionWand)player.getCurrentEquippedItem().getItem();
	}

}
