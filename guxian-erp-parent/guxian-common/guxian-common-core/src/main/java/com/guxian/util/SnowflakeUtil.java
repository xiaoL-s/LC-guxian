package com.guxian.util;

/**
 * 雪花算法工具，分布式全局唯一ID生成
 */
public class SnowflakeUtil {
    private static final long START_TIME = 1672531200000L;
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATA_CENTER_BITS = 5L;
    private static final long MAX_WORKER_ID = (1 << WORKER_ID_BITS) - 1;
    private static final long MAX_DATA_CENTER_ID = (1 << DATA_CENTER_BITS) - 1;
    private static final long SEQUENCE_BITS = 12L;

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATA_CENTER_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATA_CENTER_BITS;
    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;

    private final long workerId;
    private final long dataCenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeUtil(long workerId, long dataCenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException("workerId超出范围");
        }
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("dataCenterId超出范围");
        }
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID");
        }
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - START_TIME) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    private long waitNextMillis(long lastTs) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTs) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    // 全局单例
    private static final SnowflakeUtil INSTANCE = new SnowflakeUtil(1, 1);
    public static long getNextId() {
        return INSTANCE.nextId();
    }
}