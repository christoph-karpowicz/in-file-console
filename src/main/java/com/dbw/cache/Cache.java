package com.dbw.cache;

import com.dbw.log.Level;
import com.dbw.log.Logger;
import com.dbw.log.WarningMessages;
import com.google.inject.Singleton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Optional;
import java.util.Set;

@Singleton
public class Cache {
    public static final String CACHE_FILE_PATH = "./dbw.cache";
    private Optional<PersistentCache> persistentCache;

    public Optional<PersistentCache> getPersistentCache() {
        return persistentCache;
    }

    public void load() {
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(CACHE_FILE_PATH))) {
            persistentCache = Optional.of((PersistentCache) input.readObject());
        } catch (FileNotFoundException e) {
            persistentCache = Optional.empty();
            Logger.log(Level.WARNING, WarningMessages.NO_CACHE_FILE);
        } catch (IOException | ClassNotFoundException e) {
            persistentCache = Optional.empty();
        }
        createPersistentCacheIfDoesntExist();
    }

    public void createPersistentCacheIfDoesntExist() {
        if (!exists()) {
            persistentCache = Optional.of(new PersistentCache());
        }
    }

    public boolean compareConfigFileChecksums(String path, String currentChecksum) {
        boolean areNotEqual = true;
        if (getPersistentCache().isPresent()) {
            Optional<ConfigCache> configCache = getPersistentCache().get().getConfig(path);
            if (configCache.isPresent()) {
                String previousChecksum = configCache.get().getChecksum();
                areNotEqual = !currentChecksum.equals(previousChecksum);
            }
        }
        return areNotEqual;
    }

    public ConfigCache createOrGetConfigCache(String path) {
        Optional<ConfigCache> persistedConfigCache = getPersistentCache().get().getConfig(path);
        return persistedConfigCache.orElseGet(ConfigCache::new);
    }

    public Set<String> getConfigTables(String path) {
        return getPersistentCache().get().getConfig(path).get().getTables();
    }

    public boolean haveLastUsedConfigPathsChanged(Set<String> newPaths) {
        if (!exists()) {
            return true;
        }
        Set<String> oldPaths = getLastUsedConfigPaths();
        return newPaths == null ||
                oldPaths == null ||
                newPaths.size() != oldPaths.size() ||
                !newPaths.containsAll(oldPaths);
    }

    public Set<String> getLastUsedConfigPaths() {
        return getPersistentCache().get().getLastUsedConfigPaths();
    }

    public void setLastUsedConfigPaths(Set<String> lastUsedConfigPaths) {
        getPersistentCache().get().setLastUsedConfigPaths(lastUsedConfigPaths);
    }

    public void persist() {
        if (!exists()) {
            return;
        }
        CachePersister cachePersister = new CachePersister();
        cachePersister.setPersistentCache(persistentCache.get());
        Thread cachePersisterThread = new Thread(cachePersister);
        cachePersisterThread.start();
    }

    public boolean exists() {
        return persistentCache.isPresent();
    }

    public boolean isConfigPresent(String path) {
        return getPersistentCache().get().isConfigPresent(path);
    }

    public void removeConfig(String path) {
        getPersistentCache().get().removeConfig(path);
        persist();
    }
}
