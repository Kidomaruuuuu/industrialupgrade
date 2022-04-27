package com.denfop.api.vein;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;

public interface IVein {

  int getMeta();

  Type getType();

  ChunkPos getChunk();

  int getCol();

  void setCol(int col);

  int getMaxCol();

  void removeCol(int col);

  void setMaxCol(int maxcol);

  boolean canMining();

  NBTTagCompound writeTag();

  void setType(Type type);

  void setMeta(int meta);

  boolean equals(Object o);

  boolean get();

  void setFind(boolean find);

}
