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

import net.minecraft.entity.player.EntityPlayer;
import Reika.ChromatiCraft.Base.ChromaBookGui;

public class GuiNotes extends ChromaBookGui {

	public GuiNotes(EntityPlayer ep) {
		super(ep, 256, 220);
	}

	@Override
	public String getBackgroundTexture() {
		return "";
	}

}
