package io.github.PheonixHkbxoic.a2a4j.webflux;

import io.github.PheonixHkbxoic.a2a4j.core.core.PushNotificationAuth;
import io.github.PheonixHkbxoic.a2a4j.core.core.PushNotificationReceiverAuth;
import io.github.PheonixHkbxoic.a2a4j.core.spec.entity.Task;
import io.github.PheonixHkbxoic.a2a4j.core.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

/**
 * @author PheonixHkbxoic
 */
@Getter
@Slf4j
public abstract class WebfluxNotificationAdapter {
    private String endpoint = "/notify";
    protected PushNotificationReceiverAuth auth;
    protected RouterFunction<ServerResponse> routerFunction;

    public WebfluxNotificationAdapter() {
    }

    public WebfluxNotificationAdapter(String endpoint, List<String> jwksUrls) {
        if (!Util.isEmpty(endpoint)) {
            this.endpoint = endpoint;
        }
        if (jwksUrls.isEmpty()) {
            String error = "a2a4j.notification.jwksUrls can not be empty";
            throw new RuntimeException(error);
        }
        this.auth = new PushNotificationReceiverAuth().loadJwks(jwksUrls);
        this.routerFunction = RouterFunctions.route()
                .GET(this.endpoint, this::handleValidationCheck)
                .POST(this.endpoint, this::handleNotification)
                .build();
    }

    private Mono<ServerResponse> handleValidationCheck(ServerRequest request) {
        Optional<String> validationToken = request.queryParam("validationToken");
        log.info("push notification validationToken received: {}", validationToken.orElse(""));
        return validationToken.map(s -> ServerResponse.ok().bodyValue(s)).orElseGet(() -> ServerResponse.badRequest().build());
    }

    public Mono<ServerResponse> handleNotification(ServerRequest request) {
        List<String> header = request.headers().header(PushNotificationAuth.AUTH_HEADER);
        String authorization = header.isEmpty() ? "" : header.get(0);
        return request.bodyToMono(Task.class).flatMap(data -> {
            try {

                boolean verified = this.auth.verifyPushNotification(authorization, data);
                if (!verified) {
                    log.info("push notification verification failed, authorization: {}, data: {}", authorization, Util.toJson(data));
                    return ServerResponse.badRequest().bodyValue("push notification verification failed, authorization: " + authorization);
                }
                log.info("push notification received, authorization: {}, data: {}", authorization, Util.toJson(data));
            } catch (Exception e) {
                log.error("error verifying push notification, authorization: {}, error: {}", authorization, e.getMessage(), e);
                return ServerResponse.badRequest().bodyValue(e.getMessage());
            }
            return ServerResponse.ok().build();
        });
    }

}
