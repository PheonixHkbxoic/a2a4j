package io.github.PheonixHkbxoic.a2a4j.mvc.test;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author PheonixHkbxoic
 */
public class EchoAgent {

    Mono<String> chat(String prompt) {
        return Mono.just("I'm echo agent! echo: " + prompt);
    }

    Flux<String> chatStream(String prompt) {
        return chat(prompt).flux().flatMap(s -> {
            Stream<String> stream = Arrays.stream(s.split(" +"));
            return Flux.fromStream(stream);
        });
    }

    @Test
    public void test() {
        String prompt = "abc 123 好是呢";
        Mono<String> chat = chat(prompt);
        chat.subscribe(System.out::println);

        Flux<String> flux = chatStream(prompt);
        flux.subscribe(System.out::println);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
