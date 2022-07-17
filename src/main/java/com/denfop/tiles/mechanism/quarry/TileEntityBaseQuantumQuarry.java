package com.denfop.tiles.mechanism.quarry;

import com.denfop.Config;
import com.denfop.IUCore;
import com.denfop.IUItem;
import com.denfop.Ic2Items;
import com.denfop.api.audio.EnumTypeAudio;
import com.denfop.api.audio.IAudioFixer;
import com.denfop.api.recipe.InvSlotOutput;
import com.denfop.api.vein.Type;
import com.denfop.api.vein.Vein;
import com.denfop.api.vein.VeinSystem;
import com.denfop.audio.AudioSource;
import com.denfop.audio.PositionSpec;
import com.denfop.componets.QEComponent;
import com.denfop.container.ContainerQuantumQuarry;
import com.denfop.gui.GuiQuantumQuarry;
import com.denfop.invslot.InvSlotQuantumQuarry;
import com.denfop.items.modules.EnumQuarryModules;
import com.denfop.items.modules.EnumQuarryType;
import com.denfop.utils.ModUtils;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.init.Localization;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TileEntityBaseQuantumQuarry extends TileEntityInventory implements IHasGui, IAudioFixer,
        IUpgradableBlock {

    public final Random rand = new Random();
    public final int energyconsume;
    public final InvSlotQuantumQuarry inputslotB;
    public final InvSlotOutput outputSlot;
    public final InvSlotQuantumQuarry inputslot;
    public final InvSlotQuantumQuarry inputslotA;
    public double consume;
    public boolean mac_enabled = false;
    public boolean comb_mac_enabled = false;
    public boolean furnace;
    public int chance;
    public int col;
    public List<ItemStack> list;
    public AudioSource audioSource;
    public double getblock;
    public QEComponent energy;
    public boolean analyzer;
    public int progress;
    public EnumQuarryModules list_modules;
    public Vein vein;
    public List<ItemStack> main_list = new ArrayList<>(IUCore.list);
    public boolean original = true;
    public boolean can_dig_vein = true;
    public EnumTypeAudio typeAudio = EnumTypeAudio.OFF;
    public EnumTypeAudio[] valuesAudio = EnumTypeAudio.values();

    public TileEntityBaseQuantumQuarry(int coef) {

        this.progress = 0;
        this.getblock = 0;
        this.energyconsume = Config.enerycost * coef;
        this.energy = this.addComponent(QEComponent.asBasicSink(this, 5E7D, 14));
        this.inputslot = new InvSlotQuantumQuarry(this, 25, "input", 0);
        this.inputslotA = new InvSlotQuantumQuarry(this, 26, "input1", 1);
        this.inputslotB = new InvSlotQuantumQuarry(this, 27, "input2", 2);
        this.outputSlot = new InvSlotOutput(this, "output", 24);
        this.list = new ArrayList<>();
        this.analyzer = false;
        this.chance = 0;
        this.col = 1;
        this.furnace = false;
        this.list_modules = null;
        this.consume = this.energyconsume;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(final ItemStack stack, final List<String> tooltip, final ITooltipFlag advanced) {
        if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add(Localization.translate("press.lshift"));
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            tooltip.add(Localization.translate("iu.quarry_energy.info"));
        }
        super.addInformation(stack, tooltip, advanced);

    }

    public EnumTypeAudio getType() {
        return typeAudio;
    }

    public void setType(EnumTypeAudio type) {
        typeAudio = type;
    }

    public void initiate(int soundEvent) {
        if (this.getType() == valuesAudio[soundEvent % valuesAudio.length]) {
            return;
        }
        setType(valuesAudio[soundEvent % valuesAudio.length]);
        IC2.network.get(true).initiateTileEntityEvent(this, soundEvent, true);
    }

    public boolean list(TileEntityBaseQuantumQuarry tile, ItemStack stack1) {
        if (tile.list_modules == null) {
            return false;
        }
        return list(tile.list_modules, stack1);
    }

    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
        this.progress = nbttagcompound.getInteger("progress");
        this.getblock = nbttagcompound.getDouble("getblock");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
        nbttagcompound.setDouble("getblock", this.getblock);
        nbttagcompound.setInteger("progress", this.progress);
        return nbttagcompound;
    }


    protected void onLoaded() {
        super.onLoaded();
        this.inputslot.update();
        this.inputslotA.update();
        this.inputslotB.update();
        this.vein = VeinSystem.system.getVein(this.getWorld().getChunkFromBlockCoords(this.pos).getPos());
        if (this.vein != null) {
            if (this.vein.getType() != Type.VEIN) {
                return;
            }
            final ItemStack stack = new ItemStack(IUItem.heavyore, 1, vein.getMeta());
            if (list(this.list_modules, stack)) {
                this.can_dig_vein = false;
            }
        }
    }

    protected void onUnloaded() {
        super.onUnloaded();
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IUCore.audioManager.removeSources(this);
            this.audioSource = null;
        }

    }


    @Override
    public void onPlaced(final ItemStack stack, final EntityLivingBase placer, final EnumFacing facing) {
        super.onPlaced(stack, placer, facing);
        this.vein = VeinSystem.system.getVein(this.getWorld().getChunkFromBlockCoords(this.pos).getPos());
        if (this.vein != null) {
            if (this.vein.getType() != Type.VEIN) {
                return;
            }
            final ItemStack stack1 = new ItemStack(IUItem.heavyore, 1, vein.getMeta());
            if (list(this.list_modules, stack1)) {
                this.can_dig_vein = false;
            }

        }
    }

    protected void updateEntityServer() {
        super.updateEntityServer();
        double proccent = this.consume;

        if (this.vein != null) {
            if (this.analyzer && this.vein.get()) {
                if (this.vein.getType() == Type.VEIN && this.vein.getCol() > 0 && this.energy.getEnergy() > consume) {
                    final ItemStack stack = new ItemStack(IUItem.heavyore, 1, vein.getMeta());
                    if (this.can_dig_vein) {
                        if (!this.getActive()) {
                            this.setActive(true);
                        }
                        if (this.outputSlot.add(stack)) {
                            this.energy.useEnergy(proccent);
                            this.getblock++;
                            this.vein.removeCol(1);
                        }
                    }
                }
            }
        }
        if (this.analyzer && !Config.enableonlyvein) {
            double col = this.col;
            int chance2 = this.chance;
            int coble = rand.nextInt((int) col + 1);
            this.getblock += coble;
            col -= coble;

            for (double i = 0; i < col; i++) {
                if (this.energy.getEnergy() >= proccent) {
                    if (!this.getActive()) {
                        this.setActive(true);
                        initiate(0);
                    }
                    this.energy.useEnergy(proccent);
                    this.getblock++;
                    int num = main_list.size();
                    if (num <= 0) {
                        return;
                    }
                    int chance1 = rand.nextInt(num);
                    if (main_list.size() != IUCore.list.size()) {
                        int chance3 = rand.nextInt(IUCore.list.size());
                        if (!(chance3 <= chance1)) {
                            continue;
                        }
                    }
                    ItemStack stack = main_list.get(chance1);
                    if (this.original) {
                        if (OreDictionary.getOreIDs(stack).length > 0) {
                            String name = OreDictionary.getOreName(OreDictionary.getOreIDs(stack)[0]);
                            if ((!name
                                    .startsWith("gem") && !name
                                    .startsWith("shard")
                                    && stack.getItem() != Items.REDSTONE && stack
                                    .getItem() != Items.DYE && stack.getItem() != Items.COAL && stack
                                    .getItem() != Items.GLOWSTONE_DUST) && chance2 >= 1) {

                                this.outputSlot.add(stack);

                            } else {
                                int k = this.world.rand.nextInt(chance2 + 1);
                                for (int j = 0; j < k + 1; j++) {
                                    this.outputSlot.add(stack);
                                }
                            }
                        }
                    } else {

                        this.outputSlot.add(main_list.get(chance1));

                    }


                } else {
                    if (this.getActive()) {
                        initiate(2);
                        this.setActive(false);
                    }
                }
            }
        } else {
            if (this.getActive()) {
                initiate(2);
                this.setActive(false);
            }
        }

        if (getActive()) {
            if (this.world.getWorldTime() % 20 == 0 && !this.outputSlot.isEmpty()) {
                ItemStack stack3 = Ic2Items.ejectorUpgrade;
                ModUtils.tick(stack3, this.outputSlot, this);
            }
        }
    }

    public ContainerBase<? extends TileEntityBaseQuantumQuarry> getGuiContainer(EntityPlayer player) {
        return new ContainerQuantumQuarry(player, this);

    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {

        return new GuiQuantumQuarry(new ContainerQuantumQuarry(player, this));
    }

    public String getStartSoundFile() {
        return "Machines/quarry.ogg";
    }

    public String getInterruptSoundFile() {
        return "Machines/InterruptOne.ogg";
    }

    public void onNetworkEvent(int event) {
        if (this.audioSource == null && this.getStartSoundFile() != null) {
            this.audioSource = IUCore.audioManager.createSource(this, this.getStartSoundFile());
        }

        switch (event) {
            case 0:
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
                break;
            case 1:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    if (this.getInterruptSoundFile() != null) {
                        IUCore.audioManager.playOnce(
                                this,
                                PositionSpec.Center,
                                this.getInterruptSoundFile(),
                                false,
                                IUCore.audioManager.getDefaultVolume()
                        );
                    }
                }
                break;
            case 2:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                }
            case 3:
        }

    }

    public void onGuiClosed(EntityPlayer player) {
    }

    public boolean list(EnumQuarryModules type, ItemStack stack1) {
        if (type == null) {
            return false;
        }
        if (type.type == EnumQuarryType.BLACKLIST) {

            return this.list.contains(stack1);


        } else if (type.type == EnumQuarryType.WHITELIST) {

            return !this.list.contains(stack1);

        }
        return false;
    }


    @Override
    public double getEnergy() {
        return 0;
    }

    @Override
    public boolean useEnergy(final double v) {
        return false;
    }

    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(
                UpgradableProperty.ItemProducing
        );
    }

}
