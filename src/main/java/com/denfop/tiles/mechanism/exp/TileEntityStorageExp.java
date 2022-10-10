package com.denfop.tiles.mechanism.exp;


import com.denfop.IUCore;
import com.denfop.api.audio.EnumTypeAudio;
import com.denfop.api.audio.IAudioFixer;
import com.denfop.audio.AudioSource;
import com.denfop.componets.EXPComponent;
import com.denfop.container.ContainerStorageExp;
import com.denfop.gui.GuiStorageExp;
import com.denfop.invslot.InvSlotExpStorage;
import com.denfop.tiles.base.TileEntityInventory;
import com.denfop.utils.ExperienceUtils;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.network.INetworkDataProvider;
import ic2.api.network.INetworkUpdateListener;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityStorageExp extends TileEntityInventory implements IHasGui, INetworkUpdateListener, INetworkDataProvider,
        INetworkClientTileEntityEventListener, IAudioFixer {

    public final InvSlotExpStorage inputSlot;
    public final EXPComponent energy;
    public int expirencelevel;
    public int expirencelevel1;
    public AudioSource audioSource;
    public EnumTypeAudio typeAudio = EnumTypeAudio.OFF;
    public EnumTypeAudio[] valuesAudio = EnumTypeAudio.values();
    private double energy1;

    public TileEntityStorageExp() {
        this.inputSlot = new InvSlotExpStorage(this);
        this.energy = this.addComponent(EXPComponent.asBasicSink(this, 2000000000, 14));

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

    public String getStartSoundFile() {
        return "Machines/zab.ogg";
    }

    public String getInterruptSoundFile() {
        return "Machines/zap.ogg";
    }


    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(EntityPlayer entityPlayer, boolean isAdmin) {
        return new GuiStorageExp(new ContainerStorageExp(entityPlayer, this));
    }

    public ContainerBase<? extends TileEntityStorageExp> getGuiContainer(EntityPlayer entityPlayer) {
        return new ContainerStorageExp(entityPlayer, this);
    }

    @Override
    public void onGuiClosed(EntityPlayer entityPlayer) {

    }

    @Override
    protected void updateEntityServer() {
        super.updateEntityServer();
        if (energy1 != this.energy.getEnergy()) {
            this.expirencelevel = ExperienceUtils.getLevelForExperience((int) Math.min(this.energy.getEnergy(), 2000000000));
            this.expirencelevel1 =
                    ExperienceUtils.getLevelForExperience((int) Math.min(
                            Math.min(this.energy.getEnergy() - 2000000000, 0),
                            2000000000
                    ));
            this.energy1 = this.energy.getEnergy();
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
    protected void onLoaded() {
        super.onLoaded();
        this.expirencelevel = ExperienceUtils.getLevelForExperience((int) Math.min(this.energy.getEnergy(), 2000000000));
        this.expirencelevel1 =
                ExperienceUtils.getLevelForExperience((int) Math.min(
                        Math.min(this.energy.getEnergy() - 2000000000, 0),
                        2000000000
                ));
        this.energy1 = this.energy.getEnergy();
    }


    @Override
    public void onNetworkEvent(EntityPlayer player, int event) {
        // 0 убрать с меха опыт
        // 1 добавить в мех опыт
        if (event == 1) {
            if (this.energy.getEnergy() < this.energy.getCapacity()) {
                this.energy.storage = ExperienceUtils.removePlayerXP(player, this.energy.getCapacity(), this.energy.getEnergy());
                initiate(1);
            }

        }
        if (event == 0) {
            if (this.energy.getEnergy() > 0) {
                int temp = 0;
                if (this.energy.getEnergy() > 2000000000) {
                    temp = (int) (this.energy.getEnergy() - 2000000000);
                }
                this.energy.storage = ExperienceUtils.addPlayerXP1(player, (int) Math.min(this.energy.getEnergy(), 2000000000));
                this.energy.addEnergy(temp);
                initiate(0);
            }
        }
        this.expirencelevel = ExperienceUtils.getLevelForExperience((int) Math.min(this.energy.getEnergy(), 2000000000));
        this.expirencelevel1 =
                ExperienceUtils.getLevelForExperience((int) Math.min(
                        Math.min(this.energy.getEnergy() - 2000000000, 0),
                        2000000000
                ));

        this.energy1 = this.energy.getEnergy();
    }

    @Override
    public void onNetworkEvent(int event) {
        if (this.audioSource == null && getStartSoundFile() != null) {
            this.audioSource = IUCore.audioManager.createSource(this, getStartSoundFile());
        }
        switch (event) {
            case 0:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    IUCore.audioManager.playOnce(this, getStartSoundFile());
                }
                break;
            case 1:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    IUCore.audioManager.playOnce(this, getInterruptSoundFile());

                }
                break;
            case 2:
                if (this.audioSource != null) {
                    this.audioSource.stop();
                }
                break;
        }
    }

}
