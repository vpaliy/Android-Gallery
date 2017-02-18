package com.vpaliy.studioq.common.eventBus;

public final class Registrator {

    public static void register(Object object) {
        EventBusProvider.defaultBus().register(object);
    }

    public static void unregister(Object object) {
        EventBusProvider.defaultBus().unregister(object);
    }
}
