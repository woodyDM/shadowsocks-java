package org.simplesocks.netty.app.http.vo;

import com.sun.org.glassfish.external.statistics.CountStatistic;
import lombok.Data;
import org.simplesocks.netty.app.utils.ProxyCounter;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class CounterSnapshot {

    private LocalDateTime startTime ;
    private Long directNumber  ;
    private Long proxyNumber ;
    private Long failedNumber ;

    public static CounterSnapshot valueOf(ProxyCounter counter){
        CounterSnapshot s = new CounterSnapshot();
        s.startTime = counter.getStartTime();
        s.directNumber = counter.getDirectCounter().get();
        s.proxyNumber = counter.getProxyCounter().get();
        s.failedNumber = counter.getFailedCounter().get();
        return s;
    }
}
