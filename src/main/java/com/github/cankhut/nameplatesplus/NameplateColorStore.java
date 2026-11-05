package com.github.cankhut.nameplatesplus;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NameplateColorStore {

    private static final Map<UUID, ColorPair> COLORS = new ConcurrentHashMap<>();

    private NameplateColorStore() {
    }

    public static ColorPair get(UUID id) {
        return COLORS.get(id);
    }

    public static void put(UUID id, ColorPair colors) {
        if (colors.isDefault()) {
            COLORS.remove(id);
        } else {
            COLORS.put(id, colors);
        }
    }

    public static void clear() {
        COLORS.clear();
    }
}
