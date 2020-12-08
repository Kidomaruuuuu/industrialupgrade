package com.denfop.ssp;


import com.denfop.ssp.tiles.SSPBlock;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class SSPSourceTab extends CreativeTabs {
  public SSPSourceTab(String label) {
    super("SSPSourceTab");
  }
  
  public ItemStack getTabIconItem() {
   
    return  SuperSolarPanels.machines.getItemStack(SSPBlock.admin_solar_panel);
  }
}