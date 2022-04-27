package com.denfop.api.space.colonies;

import com.denfop.api.space.IBody;
import com.denfop.api.space.SpaceNet;
import com.denfop.api.space.fakebody.FakePlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColonieNet implements IColonieNet {

    Map<FakePlayer, List<IColonie>> fakePlayerListMap;
    List<IColonie> colonieList;
    List<FakePlayer> fakePlayerList;

    public ColonieNet() {
        this.fakePlayerListMap = new HashMap<>();
        this.colonieList = new ArrayList<>();
        this.fakePlayerList = new ArrayList<>();
    }

    @Override
    public Map<FakePlayer, List<IColonie>> getMap() {
        return this.fakePlayerListMap;
    }

    @Override
    public boolean canAddColonie(final IBody body, final FakePlayer player) {
        if (!fakePlayerListMap.containsKey(player)) {
            return true;
        }
        List<IColonie> list = fakePlayerListMap.get(player);
        for (IColonie colonie : list) {
            if (colonie.matched(body)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addColonie(final IBody body, final FakePlayer player) {
        if (canAddColonie(body, player)) {
            List<IColonie> colonieList;
            final Colonie colonie = new Colonie(body,player);
            if (!this.fakePlayerListMap.containsKey(player)) {
                colonieList = new ArrayList<>();
                colonieList.add(colonie);
                fakePlayerListMap.put(player, colonieList);
                this.fakePlayerList.add(player);
            } else {
                colonieList = fakePlayerListMap.get(player);
                colonieList.add(colonie);
            }
            this.colonieList.add(colonie);
        }
    }

    @Override
    public void removeColonie(final IColonie colonie, final FakePlayer player) {
        List<IColonie> colonieList = fakePlayerListMap.get(player);
        colonieList.remove(colonie);
        this.colonieList.remove(colonie);
    }

    @Override
    public void working() {
       for( IColonie colonie : colonieList)
           colonie.update();
    }

    @Override
    public List<IColonie> getColonies() {
        return this.colonieList;
    }

    @Override
    public NBTTagCompound writeNBT(final NBTTagCompound tag, FakePlayer player) {
        final List<IColonie> list = fakePlayerListMap.get(player);
        NBTTagCompound nbt = new NBTTagCompound();
        for(IColonie colonie : list){
            nbt.setTag(colonie.getBody().getName(), colonie.writeNBT(new NBTTagCompound()));
        }
        nbt.setTag("player",player.writeNBT());
        tag.setTag("colonia",nbt);
        return tag;
    }

    @Override
    public void addColonie(final NBTTagCompound tag) {
        NBTTagCompound nbt = tag.getCompoundTag("colonia");
        List<IColonie> list;
        final NBTTagCompound tagplayer = nbt.getCompoundTag("player");
        FakePlayer player = new FakePlayer(tagplayer.getString("name"),tagplayer.getCompoundTag("tag"));
        for(IBody body : SpaceNet.instance.getBodyList()){
            if(nbt.hasKey(body.getName())) {
                NBTTagCompound nbt1 =nbt.getCompoundTag(body.getName());
                IColonie colonie = new Colonie(nbt1,player);
                if (!this.fakePlayerListMap.containsKey(player)) {
                    list = new ArrayList<>();
                    list.add(colonie);
                    fakePlayerListMap.put(player, colonieList);
                    this.fakePlayerList.add(player);
                } else {
                    list = fakePlayerListMap.get(player);
                    list.add(colonie);
                }
            }
        }
    }

    @Override
    public List<FakePlayer> getList() {
        return this.fakePlayerList;
    }

    @Override
    public void unload() {
        this.fakePlayerListMap.clear();
        this.colonieList.clear();
        this.fakePlayerList.clear();
    }

}
