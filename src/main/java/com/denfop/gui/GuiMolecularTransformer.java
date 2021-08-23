package com.denfop.gui;

import com.denfop.Constants;
import com.denfop.api.Recipes;
import com.denfop.container.ContainerBaseMolecular;
import com.denfop.tiles.base.TileEntityMolecularTransformer;
import com.denfop.utils.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.recipe.RecipeOutput;
import ic2.core.IC2;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

@SideOnly(Side.CLIENT)
public class GuiMolecularTransformer extends GuiBaseMolecularTranformer {
	public final ContainerBaseMolecular<? extends TileEntityMolecularTransformer> container;

	public GuiMolecularTransformer(ContainerBaseMolecular<? extends TileEntityMolecularTransformer> container1) {
		super(container1);
		this.container = container1;
	}

	public void initGui() {
		super.initGui();
		this.buttonList.add(new GuiButton(0, (this.width - this.xSize) / 2 + 180, (this.height - this.ySize) / 2 + 3,
				17, 14, I18n.format("button.rg")));
		this.buttonList.add(new GuiButton(1, (this.width - this.xSize) / 2 + 7, (this.height - this.ySize) / 2 + 3,
				53, 14, I18n.format("button.changemode")));
	}

	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);
		String input = I18n.format("gui.MolecularTransformer.input") + ": ";
		String output = I18n.format("gui.MolecularTransformer.output") + ": ";
		String energyPerOperation = I18n.format("gui.MolecularTransformer.energyPerOperation") + ": ";
		String progress = I18n.format("gui.MolecularTransformer.progress") + ": ";
		double chargeLevel = (15.0D * this.container.base.getProgress());

		RecipeOutput output1 = Recipes.molecular.getOutputFor(this.container.base.inputSlot.get(0),false);
		if (chargeLevel > 0 && !this.container.base.inputSlot.isEmpty()&& Recipes.molecular.getOutputFor(this.container.base.inputSlot.get(0),false) != null) {
			if (!this.container.base.queue) {
				drawTexturedModalRect(this.xoffset + 23, this.yoffset + 48, 221, 7, 10, (int) chargeLevel);
				this.fontRendererObj.drawString(input + this.container.base.inputSlot.get().getDisplayName(),
						this.xoffset + 60, this.yoffset + 25, 4210752);

				this.fontRendererObj.drawString(output + output1.items.get(0).getDisplayName(), this.xoffset + 60,
						this.yoffset + 25 + 11, 4210752);
				this.fontRendererObj.drawString(energyPerOperation + ModUtils.getString(output1.metadata.getDouble("energy")) + " EU",
						this.xoffset + 60, this.yoffset + 25 + 22, 4210752);
				if (this.container.base.getProgress() * 100 <= 100)
					this.fontRendererObj.drawString(
							progress + MathHelper.floor_double(this.container.base.getProgress() * 100) + "%",
							this.xoffset + 60, this.yoffset + 25 + 33, 4210752);
				if (this.container.base.getProgress() * 100 > 100)
					this.fontRendererObj.drawString(
							progress + MathHelper.floor_double(100) + "%",
							this.xoffset + 60, this.yoffset + 25 + 33, 4210752);
				this.fontRendererObj.drawString(
						"EU/t: " +ModUtils.getString(this.container.base.differenceenergy),
						this.xoffset + 60, this.yoffset + 25 + 44, 4210752);
				double hours = this.container.base.time.get(0);
				double minutes = this.container.base.time.get(1);
				double seconds = this.container.base.time.get(2);
				String time1 = hours > 0 ? ModUtils.getString(hours)+StatCollector.translateToLocal("iu.hour")+"" : "";
				String time2 = minutes > 0 ? ModUtils.getString(minutes)+StatCollector.translateToLocal("iu.minutes")+"": "";
				String time3 = seconds > 0 ? ModUtils.getString(seconds)+StatCollector.translateToLocal("iu.seconds")+"" : "";

				this.fontRendererObj.drawString(
						StatCollector.translateToLocal("iu.timetoend") +time1+time2+time3 ,
						this.xoffset + 60, this.yoffset + 25 + 55, 4210752);

			}
		 else{

				int size;
				for(int i =0; ;i++){
					ItemStack stack = new ItemStack(this.container.base.inputSlot.get().getItem(),i,this.container.base.inputSlot.get().getItemDamage());
					if(Recipes.molecular.getOutputFor(stack,false) != null) {
						size =i;
						break;
					}
				}
				size = (int) Math.floor((float)this.container.base.inputSlot.get().stackSize/size);
				int  size1 = this.container.base.outputSlot.get() != null ? 64-this.container.base.outputSlot.get().stackSize : 64;
				size = Math.min(size1,size);

				if(this.container.base.outputSlot.get() == null ||this.container.base.outputSlot.get().stackSize < 64){
			drawTexturedModalRect(this.xoffset + 23, this.yoffset + 48, 221, 7, 10, (int) chargeLevel);
			this.fontRendererObj.drawString(input+ this.container.base.inputSlot.get().stackSize+"x" + this.container.base.inputSlot.get().getDisplayName(),
					this.xoffset + 60, this.yoffset + 25, 4210752);

			this.fontRendererObj.drawString(output + size+"x" +output1.items.get(0).getDisplayName(), this.xoffset + 60,
					this.yoffset + 25 + 11, 4210752);
			this.fontRendererObj.drawString(energyPerOperation + ModUtils.getString(output1.metadata.getDouble("energy")*size) + " EU",
					this.xoffset + 60, this.yoffset + 25 + 22, 4210752);
			if (this.container.base.getProgress() * 100 <= 100)
				this.fontRendererObj.drawString(
						progress + MathHelper.floor_double(this.container.base.getProgress() * 100) + "%",
						this.xoffset + 60, this.yoffset + 25 + 33, 4210752);
			if (this.container.base.getProgress() * 100 > 100)
				this.fontRendererObj.drawString(
						progress + MathHelper.floor_double(100) + "%",
						this.xoffset + 60, this.yoffset + 25 + 33, 4210752);

					this.fontRendererObj.drawString(
							"EU/t: " +ModUtils.getString(this.container.base.differenceenergy),
							this.xoffset + 60, this.yoffset + 25 + 44, 4210752);
					double hours = this.container.base.time.get(0);
					double minutes = this.container.base.time.get(1);
					double seconds = this.container.base.time.get(2);
					String time1 = hours > 0 ? ModUtils.getString(hours)+StatCollector.translateToLocal("iu.hour") : "";
					String time2 = minutes > 0 ? ModUtils.getString(minutes)+StatCollector.translateToLocal("iu.minutes"): "";
					String time3 = seconds > 0 ? ModUtils.getString(seconds)+StatCollector.translateToLocal("iu.seconds") : "";

					this.fontRendererObj.drawString(
							StatCollector.translateToLocal("iu.timetoend") +time1+time2+time3 ,
							this.xoffset + 60, this.yoffset + 25 + 55, 4210752);


				}}

		}
	}

	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 0) {
			IC2.network.get().initiateClientTileEntityEvent(this.container.base, 0);

		}
		if (guibutton.id == 1) {
			IC2.network.get().initiateClientTileEntityEvent(this.container.base, 1);

		}
	}

	public String getName() {
		return StatCollector.translateToLocal("blockMolecularTransformer.name");
	}

	public ResourceLocation getResourceLocation() {


		if (this.container.base.redstoneMode == 1) {

			return new ResourceLocation(Constants.TEXTURES,
					"textures/gui/guiMolecularTransformerNew_chemical_green.png");
		} else if (this.container.base.redstoneMode == 2) {

			return new ResourceLocation(Constants.TEXTURES, "textures/gui/guiMolecularTransformerNew_gold.png");
		} else if (this.container.base.redstoneMode == 3) {

			return new ResourceLocation(Constants.TEXTURES, "textures/gui/guiMolecularTransformerNew_red.png");
		} else if (this.container.base.redstoneMode == 4) {

			return new ResourceLocation(Constants.TEXTURES, "textures/gui/guiMolecularTransformerNew_silver.png");
		} else if (this.container.base.redstoneMode == 5) {

			return new ResourceLocation(Constants.TEXTURES, "textures/gui/guiMolecularTransformerNew_violet.png");
		} else if (this.container.base.redstoneMode == 6) {

			return new ResourceLocation(Constants.TEXTURES, "textures/gui/guiMolecularTransformerNew_blue.png");
		} else if (this.container.base.redstoneMode == 7) {

			return new ResourceLocation(Constants.TEXTURES, "textures/gui/guiMolecularTransformerNew_green.png");
		}

		else {

			return new ResourceLocation(Constants.TEXTURES, "textures/gui/guiMolecularTransformerNew.png");
		}
	}
}