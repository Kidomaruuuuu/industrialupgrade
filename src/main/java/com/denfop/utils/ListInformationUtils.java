package com.denfop.utils;

import com.denfop.IUItem;
import com.denfop.blocks.mechanism.BlockMoreMachine;
import com.denfop.blocks.mechanism.BlockMoreMachine1;
import com.denfop.blocks.mechanism.BlockMoreMachine2;
import com.denfop.blocks.mechanism.BlockMoreMachine3;
import ic2.core.init.Localization;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ListInformationUtils {

    public static final List<String> panelinform = new ArrayList<>();
    public static final List<String> storageinform = new ArrayList<>();
    public static final List<String> fisherinform = new ArrayList<>();
    public static final List<String> analyzeinform = new ArrayList<>();
    public static final List<String> quarryinform = new ArrayList<>();
    public static final List<String> mechanism_info = new ArrayList<>();
    public static final List<String> mechanism_info1 = new ArrayList<>();
    public static final List<String> mechanism_info2 = new ArrayList<>();

    public static int tick = 0;
    public static int index = 0;
    public static int index1 = 0;
    public static int index2 = 0;

    public static void init() {
        for (int i = 0; i < BlockMoreMachine.values().length; i++) {
            mechanism_info.add(Localization.translate(new ItemStack(IUItem.machines_base, 1, i).getUnlocalizedName()));
        }
        for (int i = 0; i < BlockMoreMachine1.values().length; i++) {
            mechanism_info.add(Localization.translate(new ItemStack(IUItem.machines_base1, 1, i).getUnlocalizedName()));
        }
        for (int i = 0; i < BlockMoreMachine2.values().length; i++) {
            mechanism_info.add(Localization.translate(new ItemStack(IUItem.machines_base2, 1, i).getUnlocalizedName()));
        }
        for (int i = 0; i < BlockMoreMachine3.values().length; i++) {
            mechanism_info.add(Localization.translate(new ItemStack(IUItem.machines_base3, 1, i).getUnlocalizedName()));
        }
        mechanism_info1.add(Localization.translate(new ItemStack(IUItem.oilrefiner).getUnlocalizedName()));
        mechanism_info1.add(Localization.translate(new ItemStack(IUItem.oiladvrefiner).getUnlocalizedName()));
        mechanism_info1.add(Localization.translate(new ItemStack(IUItem.oilgetter).getUnlocalizedName()));
        mechanism_info1.add(Localization.translate(new ItemStack(IUItem.basemachine1, 1, 15).getUnlocalizedName()));
        mechanism_info1.add(Localization.translate(new ItemStack(IUItem.basemachine1, 1, 1).getUnlocalizedName()));
        mechanism_info1.add(Localization.translate(new ItemStack(IUItem.basemachine2, 1, 11).getUnlocalizedName()));

        mechanism_info2.add(Localization.translate(new ItemStack(IUItem.machines, 1, 4).getUnlocalizedName()));
        mechanism_info2.add(Localization.translate(new ItemStack(IUItem.basemachine, 1, 3).getUnlocalizedName()));
        mechanism_info2.add(Localization.translate(new ItemStack(IUItem.basemachine, 1, 12).getUnlocalizedName()));
        mechanism_info2.add(Localization.translate(new ItemStack(IUItem.machines, 1, 6).getUnlocalizedName()));

        quarryinform.add(Localization.translate("iu.quarryinformation1"));
        quarryinform.add(Localization.translate("iu.quarryinformation2"));
        quarryinform.add(Localization.translate("iu.quarryinformation3"));
        quarryinform.add(Localization.translate("iu.quarryinformation4"));
        quarryinform.add(Localization.translate("iu.quarryinformation5"));
        quarryinform.add(Localization.translate("iu.quarryinformation6"));
        quarryinform.add(Localization.translate("iu.quarryinformation7"));
        quarryinform.add(Localization.translate("iu.quarryinformation8"));
        quarryinform.add(Localization.translate("iu.quarryinformation9"));
        fisherinform.add(Localization.translate("iu.fisherinformation1"));
        fisherinform.add(Localization.translate("iu.fisherinformation2"));
        fisherinform.add(Localization.translate("iu.fisherinformation3"));

        panelinform.add(Localization.translate("iu.panelinformation1"));
        panelinform.add(Localization.translate("iu.panelinformation2"));
        panelinform.add(Localization.translate("iu.panelinformation3"));
        panelinform.add(Localization.translate("iu.panelinformation4"));
        panelinform.add(Localization.translate("iu.panelinformation5"));
        panelinform.add(Localization.translate("iu.panelinformation6"));
        panelinform.add(Localization.translate("iu.panelinformation7"));
        panelinform.add(Localization.translate("iu.panelinformation8"));
        panelinform.add(Localization.translate("iu.panelinformation9"));
        storageinform.add(Localization.translate("iu.electricstorageinformation1"));
        storageinform.add(Localization.translate("iu.electricstorageinformation2"));
        storageinform.add(Localization.translate("iu.electricstorageinformation3"));
        storageinform.add(Localization.translate("iu.electricstorageinformation4"));
        storageinform.add(Localization.translate("iu.electricstorageinformation5"));
        storageinform.add(Localization.translate("iu.electricstorageinformation6"));
        storageinform.add(Localization.translate("iu.electricstorageinformation7"));
        analyzeinform.add(Localization.translate("iu.analyzerinformation1"));
        analyzeinform.add(Localization.translate("iu.analyzerinformation2"));
        analyzeinform.add(Localization.translate("iu.analyzerinformation3"));
        analyzeinform.add(Localization.translate("iu.analyzerinformation4"));
        analyzeinform.add(Localization.translate("iu.analyzerinformation5"));
        analyzeinform.add(Localization.translate("iu.analyzerinformation6"));
        analyzeinform.add(Localization.translate("iu.analyzerinformation7"));
    }

}
