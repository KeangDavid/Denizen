package com.denizenscript.denizen.utilities.flags;

import com.denizenscript.denizen.utilities.DataPersistenceHelper;
import com.denizenscript.denizencore.flags.MapTagBasedFlagTracker;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataHolder;

import java.util.Collection;
import java.util.stream.Collectors;

public class DataPersistenceFlagTracker extends MapTagBasedFlagTracker {

    public DataPersistenceFlagTracker(PersistentDataHolder holder) {
        this.holder = holder;
    }

    public DataPersistenceFlagTracker(PersistentDataHolder holder, String keyPrefix) {
        this.holder = holder;
        this.keyPrefix = keyPrefix;
    }

    public PersistentDataHolder holder;

    public String keyPrefix = "flag_";

    @Override
    public MapTag getRootMap(String key) {
        return (MapTag) DataPersistenceHelper.getDenizenKey(holder, keyPrefix + CoreUtilities.toLowerCase(key));
    }

    @Override
    public void setRootMap(String key, MapTag map) {
        if (map == null) {
            DataPersistenceHelper.removeDenizenKey(holder, keyPrefix + CoreUtilities.toLowerCase(key));
            return;
        }
        DataPersistenceHelper.setDenizenKey(holder, keyPrefix + CoreUtilities.toLowerCase(key), map);
    }

    @Override
    public Collection<String> listAllFlags() {
        return holder.getPersistentDataContainer().getKeys().stream()
                .filter(k -> k.getNamespace().equals("denizen") && k.getKey().startsWith(keyPrefix))
                .map(k -> k.getKey().substring(keyPrefix.length())).collect(Collectors.toList());
    }

    public void doTotalClean() {
        if (MapTagBasedFlagTracker.skipAllCleanings) {
            return;
        }
        for (NamespacedKey key : holder.getPersistentDataContainer().getKeys()) {
            if (!key.getNamespace().equals("denizen") || !key.getKey().startsWith("flag_")) {
                continue;
            }
            ObjectTag map = holder.getPersistentDataContainer().get(key, DataPersistenceHelper.PERSISTER_TYPE);
            if (!(map instanceof MapTag)) {
                continue;
            }
            if (isExpired(((MapTag) map).map.get(expirationString))) {
                holder.getPersistentDataContainer().remove(key);
                continue;
            }
            ObjectTag subValue = ((MapTag) map).map.get(valueString);
            if (subValue instanceof MapTag) {
                if (doClean((MapTag) subValue)) {
                    holder.getPersistentDataContainer().set(key, DataPersistenceHelper.PERSISTER_TYPE, map);
                }
            }
        }
    }
}
