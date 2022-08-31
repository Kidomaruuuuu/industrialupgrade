package com.denfop.events;

import ic2.core.IWorldTickCallback;

import java.util.List;
import java.util.Queue;
import java.util.Set;

public class WorldData {

    private final Queue<IWorldTickCallback> singleUpdates;
    private final List<IWorldTickCallback> continuousUpdatesToAdd;
    private final Set<IWorldTickCallback> continuousUpdates;
    private final List<IWorldTickCallback> continuousUpdatesToRemove;

    public WorldData(
            Queue<IWorldTickCallback> singleUpdates, Set<IWorldTickCallback> continuousUpdates,
            List<IWorldTickCallback> continuousUpdatesToAdd, List<IWorldTickCallback> continuousUpdatesToRemove
    ) {
        this.singleUpdates = singleUpdates;
        this.continuousUpdatesToAdd = continuousUpdatesToAdd;
        this.continuousUpdates = continuousUpdates;
        this.continuousUpdatesToRemove = continuousUpdatesToRemove;
    }

    public List<IWorldTickCallback> getContinuousUpdatesToAdd() {
        return continuousUpdatesToAdd;
    }

    public List<IWorldTickCallback> getContinuousUpdatesToRemove() {
        return continuousUpdatesToRemove;
    }

    public Queue<IWorldTickCallback> getSingleUpdates() {
        return singleUpdates;
    }

    public Set<IWorldTickCallback> getContinuousUpdates() {
        return continuousUpdates;
    }

}
