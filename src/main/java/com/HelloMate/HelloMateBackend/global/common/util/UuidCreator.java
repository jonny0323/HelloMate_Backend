package com.HelloMate.HelloMateBackend.global.common.util;

import java.util.UUID;

public final class UuidCreator {

    private UuidCreator() {
    }

    public static String create() {
        return UUID.randomUUID().toString();
    }
}
