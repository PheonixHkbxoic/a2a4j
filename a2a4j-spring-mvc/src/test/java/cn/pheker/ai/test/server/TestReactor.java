package cn.pheker.ai.test.server;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author PheonixHkbxoic
 * @date 2025/4/15 15:17
 * @desc
 */
public class TestReactor {

    @Test
    public void test() throws InterruptedException {
        System.out.println("before");

        dataPublishAndSubscribe().block();

        // 有publishOn或subscribeOn时 不会阻塞
        System.out.println("after");
        try {
            System.in.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Mono<Object> dataPublishAndSubscribe() {
        return Mono.fromRunnable(() -> {
            System.out.println("runnable");
            Flux.range(1, 100)
                    .publishOn(Schedulers.boundedElastic())
                    .handle((i, sink) -> {
                        String s = String.format("第%04d个元素", i);
                        sink.next(s);
                        System.out.println("publish " + s);
                        try {
                            TimeUnit.MILLISECONDS.sleep(5);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (i >= 1000) {
                            sink.complete();
                        }
                    })
//                .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(s -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(8);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("consume " + s);
                    });
        });
    }
    
}
