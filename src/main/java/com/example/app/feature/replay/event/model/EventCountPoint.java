package com.example.app.feature.replay.event.model;

import java.time.LocalDateTime;

public class EventCountPoint {

    private final int systemNo;
    private final LocalDateTime bucketStart;
    private final int count;

    public EventCountPoint(int systemNo, LocalDateTime bucketStart, int count) {
        this.systemNo = systemNo;
        this.bucketStart = bucketStart;
        this.count = count;
    }

    public int getSystemNo() {
        return systemNo;
    }

    public LocalDateTime getBucketStart() {
        return bucketStart;
    }

    public int getCount() {
        return count;
    }
}