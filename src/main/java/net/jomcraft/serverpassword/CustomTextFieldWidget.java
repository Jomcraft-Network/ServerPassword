/* 
 *		ServerPassword - 1.18.x <> Codedesign by PT400C and Compaszer
 *		© Jomcraft-Network 2022
 */
package net.jomcraft.serverpassword;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CustomTextFieldWidget extends EditBox implements Widget, GuiEventListener {

	public CustomTextFieldWidget(Font p_i232260_1_, int p_i232260_2_, int p_i232260_3_, int p_i232260_4_, int p_i232260_5_, Component p_i232260_6_) {
		super(p_i232260_1_, p_i232260_2_, p_i232260_3_, p_i232260_4_, p_i232260_5_, p_i232260_6_);
	}

	public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
		if (this.isVisible()) {
			if (this.isBordered()) {
				int i = this.isFocused() ? -1 : -6250336;
				fill(p_230431_1_, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, i);
				fill(p_230431_1_, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
			}

			int i2 = this.isEditable ? this.textColor : this.textColorUneditable;
			int j = this.cursorPos - this.displayPos;
			int k = this.highlightPos - this.displayPos;

			String str = this.getValue().replaceAll(".", "*");

			String s = this.font.plainSubstrByWidth(str.substring(this.displayPos), this.getInnerWidth());
			boolean flag = j >= 0 && j <= s.length();
			boolean flag1 = this.isFocused() && this.frame / 6 % 2 == 0 && flag;
			int l = this.isBordered() ? this.x + 4 : this.x;
			int i1 = this.isBordered() ? this.y + (this.height - 8) / 2 : this.y;
			int j1 = l;
			if (k > s.length()) {
				k = s.length();
			}

			if (!s.isEmpty()) {
				String s1 = flag ? s.substring(0, j) : s;
				j1 = this.font.drawShadow(p_230431_1_, this.formatter.apply(s1, this.displayPos), (float) l, (float) i1, i2);
			}

			boolean flag2 = this.cursorPos < str.length() || str.length() >= this.getMaxLength();
			int k1 = j1;
			if (!flag) {
				k1 = j > 0 ? l + this.width : l;
			} else if (flag2) {
				k1 = j1 - 1;
				--j1;
			}

			if (!s.isEmpty() && flag && j < s.length()) {
				this.font.drawShadow(p_230431_1_, this.formatter.apply(s.substring(j), this.cursorPos), (float) j1, (float) i1, i2);
			}

			if (!flag2 && this.suggestion != null) {
				this.font.drawShadow(p_230431_1_, this.suggestion, (float) (k1 - 1), (float) i1, -8355712);
			}

			if (flag1) {
				if (flag2) {
					GuiComponent.fill(p_230431_1_, k1, i1 - 1, k1 + 1, i1 + 1 + 9, -3092272);
				} else {
					this.font.drawShadow(p_230431_1_, "_", (float) k1, (float) i1, i2);
				}
			}

			if (k != j) {
				int l1 = l + this.font.width(s.substring(0, k));
				this.renderHighlight(k1, i1 - 1, l1 - 1, i1 + 1 + 9);
			}

		}
	}
}