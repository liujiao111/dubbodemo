package com.liu.thread;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.threadpool.support.fixed.FixedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class WatchingThreadPool extends FixedThreadPool implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(WatchingThreadPool.class);

    private static final  double ALARM_PERCENT = 0.90;

    private final Map<URL, ThreadPoolExecutor> THREAD_POOLS = new ConcurrentHashMap<>();

    public WatchingThreadPool() {
        //每隔3秒打印线程使用情况
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this, 1, 3, TimeUnit.SECONDS);
    }

    @Override
    public Executor getExecutor(URL url) {

        //从父类中创建线程池
        Executor executor = super.getExecutor(url);
        if(executor instanceof ThreadPoolExecutor) {
            THREAD_POOLS.put(url, (ThreadPoolExecutor) executor);
        }
        return executor;
    }

    @Override
    public void run() {
        //遍历线程池，如果超出指定的部分，进行操作， 比如发端短信等
        for (Map.Entry<URL, ThreadPoolExecutor> entry : THREAD_POOLS.entrySet()) {
            URL key = entry.getKey();
            ThreadPoolExecutor executor = entry.getValue();

            //当前执行中的线程数
            int activeCount = executor.getActiveCount();
            //总线程数
            int corePoolSize = executor.getCorePoolSize();

            double used = (double) activeCount / corePoolSize;
            int usedNum = (int) (used * 100);
            LOGGER.info("线程池执行状态：[{}/{}]:{}%", activeCount, corePoolSize, usedNum);

            if(used >= ALARM_PERCENT) {
                LOGGER.error("超出警戒值！host:{}，当前已使用量：{}%, URL:{}", key.getIp(), usedNum, key);
            }
        }


    }
}
