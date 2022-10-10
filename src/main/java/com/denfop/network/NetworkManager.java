package com.denfop.network;

import com.denfop.IUCore;
import com.denfop.api.radiationsystem.Radiation;
import com.denfop.api.research.main.BaseLevelSystem;
import com.denfop.api.research.main.EnumLeveling;
import com.denfop.items.armour.ItemArmorImprovemedQuantum;
import ic2.api.network.ClientModifiable;
import ic2.api.network.INetworkManager;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.item.tool.HandHeldInventory;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.IPlayerItemDataListener;
import ic2.core.util.LogCategory;
import ic2.core.util.ReflectionUtil;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ServerCustomPacketEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

public class NetworkManager implements INetworkManager {

    private static FMLEventChannel channel;
    private static final Field playerInstancePlayers = ReflectionUtil.getField(PlayerChunkMapEntry.class, List.class);

    public NetworkManager() {
        if (channel == null) {
            channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("IU");
        }

        channel.register(this);
    }

    private static FMLProxyPacket makePacket(GrowingBuffer buffer, boolean advancePos) {
        return new FMLProxyPacket(new PacketBuffer(buffer.toByteBuf(advancePos)), "IU");
    }

    private static FMLProxyPacket makePacket(GrowingBuffer buffer) {
        return new FMLProxyPacket(new PacketBuffer(buffer.toByteBuf(true)), "IU");
    }

    static <T extends Collection<EntityPlayerMP>> T getPlayersInRange(World world, BlockPos pos, T result) {
        if (!(world instanceof WorldServer)) {
            return result;
        } else {
            PlayerChunkMap playerManager = ((WorldServer) world).getPlayerChunkMap();
            PlayerChunkMapEntry instance = playerManager.getEntry(pos.getX() >> 4, pos.getZ() >> 4);
            if (instance != null) {
                result.addAll(ReflectionUtil.getFieldValue(playerInstancePlayers, instance));
            }
            return result;
        }
    }

    private static TeUpdateDataServer getTeUpdateData(TileEntity te) {
        assert IC2.platform.isSimulating();

        if (te == null) {
            throw new NullPointerException();
        } else {
            WorldData worldData = WorldData.get(te.getWorld());
            TeUpdateDataServer ret = worldData.tesToUpdate.get(te);
            if (ret == null) {
                ret = new TeUpdateDataServer();
                worldData.tesToUpdate.put(te, ret);
            }

            return ret;
        }
    }

    private static TeUpdateDataServer getTeUpdateData(World te) {
        assert IC2.platform.isSimulating();

        if (te == null) {
            throw new NullPointerException();
        } else {
            WorldData worldData = WorldData.get(te);
            TeUpdateDataServer ret;
            ret = new TeUpdateDataServer();
            worldData.tesToUpdate.put(null, ret);

            return ret;
        }
    }

    static void writeFieldData(Object object, String fieldName, GrowingBuffer out) throws IOException {
        int pos = fieldName.indexOf(61);
        if (pos != -1) {
            out.writeString(fieldName.substring(0, pos));
            DataEncoder.encode(out, fieldName.substring(pos + 1));
        } else {
            out.writeString(fieldName);

            try {
                DataEncoder.encode(out, ReflectionUtil.getValueRecursive(object, fieldName));
            } catch (NoSuchFieldException var5) {
                throw new RuntimeException("Can't find field " + fieldName + " in " + object.getClass().getName(), var5);
            }
        }

    }

    public void onTickEnd(WorldData worldData) {
        try {
            TeUpdate.send(worldData, this);
        } catch (IOException var3) {
            throw new RuntimeException(var3);
        }
    }

    protected boolean isClient() {
        return false;
    }

    private Field getClientModifiableField(Class<?> cls, String fieldName) {
        Field field = ReflectionUtil.getFieldRecursive(cls, fieldName);
        if (field == null) {
            IC2.log.warn(LogCategory.Network, "Can't find field %s in %s.", fieldName, cls.getName());
            return null;
        } else if (field.getAnnotation(ClientModifiable.class) == null) {
            IC2.log.warn(LogCategory.Network, "The field %s in %s is not modifiable.", fieldName, cls.getName());
            return null;
        } else {
            return field;
        }
    }

    protected void onCommonPacketData(
            SubPacketType packetType,
            boolean simulating,
            GrowingBuffer is,
            final EntityPlayer player
    ) throws IOException {
        final String fieldName;
        final Object value;
        final int windowId;
        switch (packetType) {
            case PlayerItemData:
                final int slot = is.readByte();
                final Item item = DataEncoder.decode(is, Item.class);
                int dataCount = is.readVarInt();
                final Object[] subData = new Object[dataCount];

                for (int i = 0; i < dataCount; ++i) {
                    subData[i] = DataEncoder.decode(is);
                }

                if (slot >= 0 && slot < 9) {
                    IC2.platform.requestTick(simulating, () -> {
                        for (int i = 0; i < subData.length; ++i) {
                            subData[i] = DataEncoder.getValue(subData[i]);
                        }

                        ItemStack stack = player.inventory.mainInventory.get(slot);
                        if (!StackUtil.isEmpty(stack) && stack.getItem() == item && item instanceof IPlayerItemDataListener) {
                            ((IPlayerItemDataListener) item).onPlayerItemNetworkData(player, slot, subData);
                        }

                    });
                }
                break;
            case ContainerData:
                windowId = is.readInt();
                fieldName = is.readString();
                value = DataEncoder.decode(is);
                IC2.platform.requestTick(simulating, () -> {
                    if (player.openContainer instanceof ContainerBase && player.openContainer.windowId == windowId && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(
                            player.openContainer.getClass(),
                            fieldName
                    ) != null)) {
                        ReflectionUtil.setValueRecursive(player.openContainer, fieldName, DataEncoder.getValue(value));
                    }

                });
                break;
            case ContainerEvent:
                windowId = is.readInt();
                fieldName = is.readString();
                IC2.platform.requestTick(simulating, () -> {
                    if (player.openContainer instanceof ContainerBase && player.openContainer.windowId == windowId) {
                        ((ContainerBase) player.openContainer).onContainerEvent(fieldName);
                    }

                });
                break;
            case HandHeldInvData:
                windowId = is.readInt();
                fieldName = is.readString();
                value = DataEncoder.decode(is);
                IC2.platform.requestTick(simulating, () -> {
                    if (player.openContainer instanceof ContainerBase && player.openContainer.windowId == windowId) {
                        ContainerBase<?> container = (ContainerBase) player.openContainer;
                        if (container.base instanceof HandHeldInventory && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(
                                container.base.getClass(),
                                fieldName
                        ) != null)) {
                            ReflectionUtil.setValueRecursive(container.base, fieldName, DataEncoder.getValue(value));
                        }
                    }

                });
                break;
            case TileEntityData:
                final Object teDeferred = DataEncoder.decodeDeferred(is, TileEntity.class);
                fieldName = is.readString();
                value = DataEncoder.decode(is);
                IC2.platform.requestTick(simulating, () -> {
                    TileEntity te = DataEncoder.getValue(teDeferred);
                    if (te != null && (NetworkManager.this.isClient() || NetworkManager.this.getClientModifiableField(
                            te.getClass(),
                            fieldName
                    ) != null)) {
                        ReflectionUtil.setValueRecursive(te, fieldName, DataEncoder.getValue(value));
                    }

                });
                break;
            default:
                IC2.log.warn(LogCategory.Network, "Unhandled packet type: %s", packetType.name());
        }

    }

    public final void updateTileEntityField(TileEntity te, String field) {
        if (!this.isClient()) {
            getTeUpdateData(te).addGlobalField(field);
        } else if (this.getClientModifiableField(te.getClass(), field) == null) {
            IC2.log.warn(LogCategory.Network, "Field update for %s failed.", te);
        } else {
            GrowingBuffer buffer = new GrowingBuffer(64);

            try {
                SubPacketType.TileEntityData.writeTo(buffer);
                DataEncoder.encode(buffer, te, false);
                writeFieldData(te, field, buffer);
            } catch (IOException var5) {
                throw new RuntimeException(var5);
            }

            buffer.flip();
            this.sendPacket(buffer);
        }

    }

    public final void initiateTileEntityEvent(TileEntity te, int event, boolean limitRange) {
        assert !this.isClient();


    }

    public final void initiateItemEvent(EntityPlayer player, ItemStack stack, int event, boolean limitRange) {

    }

    public void initiateClientItemEvent(ItemStack stack, int event) {
        assert false;

    }

    public void initiateClientTileEntityEvent(TileEntity te, int event) {
        assert false;

    }

    public final void sendInitialData(TileEntity te) {
        assert !this.isClient();


    }

    @SubscribeEvent
    public void onPacket(ServerCustomPacketEvent event) {
        if (this.getClass() == NetworkManager.class) {
            try {
                this.onPacketData(
                        GrowingBuffer.wrap(event.getPacket().payload()),
                        ((NetHandlerPlayServer) event.getHandler()).player
                );
            } catch (Throwable var3) {
                IC2.log.warn(LogCategory.Network, var3, "Network read failed");
                throw new RuntimeException(var3);
            }

            event.getPacket().payload().release();
        }

    }

    private void onPacketData(GrowingBuffer is, final EntityPlayer player) throws IOException {
        if (is.hasAvailable()) {
            SubPacketType packetType = SubPacketType.read(is, true);
            if (packetType != null) {
                if (packetType == SubPacketType.KeyUpdate) {
                    final int keyState = is.readInt();
                    IC2.platform.requestTick(true, () -> IUCore.keyboard.processKeyUpdate(player, keyState));
                } else if (packetType == SubPacketType.ColorPicker) {
                    final int color = is.readInt();
                    final ItemStack armor = player.inventory.armorInventory.get(2);
                    if (armor.getItem() instanceof ItemArmorImprovemedQuantum) {
                        if (color != -1) {
                            Color color1 = new Color(color);
                            final NBTTagCompound nbt = player.getEntityData();
                            nbt.setDouble("Red", color1.getRed());
                            nbt.setDouble("Blue", color1.getBlue());
                            nbt.setDouble("Green", color1.getGreen());
                        } else {
                            final NBTTagCompound nbt = player.getEntityData();
                            nbt.setBoolean("RGB", true);
                        }
                    }
                }
                onCommonPacketData(packetType, true, is, player);
            }

        }

    }

    public void initiateKeyUpdate(int keyState) {
    }

    protected final void sendPacket(GrowingBuffer buffer, boolean advancePos, EntityPlayerMP player) {
        assert !this.isClient();

        channel.sendTo(makePacket(buffer, advancePos), player);
    }

    protected final void sendPacket(GrowingBuffer buffer) {
        if (!this.isClient()) {
            channel.sendToAll(makePacket(buffer));
        } else {
            channel.sendToServer(makePacket(buffer));
        }

    }

    final void sendLargePacket(EntityPlayerMP player, int id, GrowingBuffer data) {
        GrowingBuffer buffer = new GrowingBuffer(16384);
        buffer.writeShort(0);

        try {
            DeflaterOutputStream deflate = new DeflaterOutputStream(buffer);
            data.writeTo(deflate);
            deflate.close();
        } catch (IOException var8) {
            throw new RuntimeException(var8);
        }

        buffer.flip();
        boolean firstPacket = true;

        boolean lastPacket;
        do {
            lastPacket = buffer.available() <= 32766;
            if (!firstPacket) {
                buffer.skipBytes(-2);
            }

            SubPacketType.LargePacket.writeTo(buffer);
            int state = 0;
            if (firstPacket) {
                state |= 1;
            }

            if (lastPacket) {
                state |= 2;
            }

            state |= id << 2;
            buffer.write(state);
            buffer.skipBytes(-2);
            if (lastPacket) {
                this.sendPacket(buffer, true, player);

                assert !buffer.hasAvailable();
            } else {
                this.sendPacket(buffer.copy(32766), true, player);
            }

            firstPacket = false;
        } while (!lastPacket);

    }

    public final void updateTileEntityFieldTo(TileEntity te, String field, EntityPlayerMP player) {
        assert !this.isClient();

        getTeUpdateData(te).addPlayerField(field, player);
    }

    public final void updateTileEntityFieldTo(World te, String field, EntityPlayerMP player) {
        assert !this.isClient();

        getTeUpdateData(te).addPlayerField(field, player);
    }

    public void initiateResearchSystem(BaseLevelSystem levelSystem) {
        GrowingBuffer buffer = new GrowingBuffer(64);
        try {
            SubPacketType.LevelSystem.writeTo(buffer);
            DataEncoder.encode(buffer, levelSystem, false);
        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }

        buffer.flip();
        this.sendPacket(buffer);
    }

    public void initiateResearchSystemAdd(EnumLeveling level, int add, String name) {
        GrowingBuffer buffer = new GrowingBuffer(64);
        try {
            SubPacketType.LevelSystemAdd.writeTo(buffer);
            DataEncoder.encode(buffer, name, false);
            DataEncoder.encode(buffer, level.ordinal(), false);
            DataEncoder.encode(buffer, add, false);

        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }

        buffer.flip();
        this.sendPacket(buffer);
    }

    public void initiateResearchSystemDelete(String name) {
        GrowingBuffer buffer = new GrowingBuffer(64);
        try {
            SubPacketType.LevelSystemRemove.writeTo(buffer);
            DataEncoder.encode(buffer, name, false);


        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }

        buffer.flip();
        this.sendPacket(buffer);
    }

    public void initiateRadiation(List<Radiation> radiation, EntityPlayer player) {
        GrowingBuffer buffer = new GrowingBuffer();

        SubPacketType.Radiation.writeTo(buffer);
        buffer.writeString(player.getName());
        buffer.writeInt(radiation.size());
        radiation.forEach(radiation1 -> {
            try {
                DataEncoder.encode(buffer, radiation, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        buffer.flip();
        this.sendPacket(buffer);
    }

    public void initiateRadiation(Radiation radiation) {
        GrowingBuffer buffer = new GrowingBuffer(64);
        try {
            SubPacketType.RadiationUpdate.writeTo(buffer);
            DataEncoder.encode(buffer, radiation, false);


        } catch (IOException var5) {
            throw new RuntimeException(var5);
        }

        buffer.flip();
        this.sendPacket(buffer);
    }

}
