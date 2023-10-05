package org.lrpc.common;

import org.lrpc.common.util.DataUtil;

import java.util.concurrent.atomic.LongAdder;

/**
 * 请求id生成器
 */
public class IdGenerator {

//    起始时间戳
    public static final long START_STAMP = DataUtil.get("2022-1-1").getTime();

    public static final long DATA_CENTER_BIT = 5L;
    public static final long MACHINE_BIT = 5L;
    public static final long SEQUENCE_BIT = 12L;

//    最大值
    public static final long DATA_CENTER_MAX = ~(-1L << DATA_CENTER_BIT);
    public static final long MACHINE_MAX = ~(-1L << MACHINE_BIT);
    public static final long SEQUENCE_MAX = ~(1L << SEQUENCE_BIT);

    public static final long TIMESTAMP_LEFT = DATA_CENTER_BIT +MACHINE_BIT + SEQUENCE_BIT;

    public static final long DATA_CENTER_LEFT = MACHINE_BIT + SEQUENCE_BIT;

    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    private long dataCenterId;
    private long machineId;

    private LongAdder sequenceId = new LongAdder();
//    TODO 时钟回拨问题，需要去处理
    private long lastTimeStamp = -1;

    public IdGenerator(long dataCenterId, long machineId) {
//        判断传入参数是否合法
        if (dataCenterId > DATA_CENTER_MAX || machineId > MACHINE_MAX) {
            throw new IllegalArgumentException("您传入的数据中心编号或机器号不合法");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }

    public long getId() {
//        处理时间戳
        long currentTime = System.currentTimeMillis();
        long timeStamp = currentTime - START_STAMP;

//        判断时间回拨
        if (timeStamp < lastTimeStamp) {
            throw new RuntimeException("您的服务器进行了时间回调");
        }

//        sequenceId需要做一些处理，如果同一个时间节点、必须自增
        if (timeStamp == lastTimeStamp) {
            sequenceId.increment();
            if (sequenceId.sum() >= SEQUENCE_MAX) {
                timeStamp = getNextTimeStamp();
                sequenceId.reset();
            }
        }else {
            sequenceId.reset();
        }
//        执行结束，将时间戳赋值给lastTimeStamp
        lastTimeStamp = timeStamp;
        long sequence = sequenceId.sum();
        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATA_CENTER_LEFT
                | machineId << MACHINE_LEFT | sequence;
    }

    private long getNextTimeStamp() {
        long current = System.currentTimeMillis() - START_STAMP;
//        如果一样就一直循环，一直到下一个时间戳
        while (current == lastTimeStamp) {
            current = System.currentTimeMillis() - START_STAMP;
        }
        return current;
    }

    public static void main(String[] args) {
        IdGenerator idGenerator = new IdGenerator(1,2);
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                System.out.println(idGenerator.getId());
            }).start();
        }
    }
}
