package io.github.pheonixhkbxoic.a2a4j.mvc;

import io.github.pheonixhkbxoic.a2a4j.core.core.PushNotificationAuth;
import io.github.pheonixhkbxoic.a2a4j.core.core.PushNotificationReceiverAuth;
import io.github.pheonixhkbxoic.a2a4j.core.spec.entity.Task;
import io.github.pheonixhkbxoic.a2a4j.core.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author PheonixHkbxoic
 */
@Getter
@Slf4j
public abstract class WebMvcNotificationAdapter {
    private String endpoint = "/notify";
    protected List<String> jwksUrls;
    protected PushNotificationReceiverAuth auth;
    protected RouterFunction<ServerResponse> routerFunction;
    protected AtomicLong verifyFailCount = new AtomicLong(0L);

    public WebMvcNotificationAdapter() {
    }

    public WebMvcNotificationAdapter(String endpoint, List<String> jwksUrls) {
        if (!Util.isEmpty(endpoint)) {
            this.endpoint = endpoint;
        }
        if (jwksUrls.isEmpty()) {
            String error = "a2a4j.notification.jwksUrls can not be empty";
            throw new RuntimeException(error);
        }
        this.jwksUrls = jwksUrls;
        this.auth = new PushNotificationReceiverAuth();
        this.reloadJwks();
        this.routerFunction = RouterFunctions.route()
                .GET(this.endpoint, this::handleValidationCheck)
                .POST(this.endpoint, this::handleNotification)
                .build();
    }

    public void reloadJwks() {
        this.auth = new PushNotificationReceiverAuth();
        this.auth.loadJwks(jwksUrls);
        this.verifyFailCount.set(0L);
    }

    private ServerResponse handleValidationCheck(ServerRequest request) {
        Optional<String> validationToken = request.param("validationToken");
        log.info("push notification validationToken received: {}", validationToken.orElse(""));
        return validationToken.map(s -> ServerResponse.ok().body(s)).orElseGet(() -> ServerResponse.badRequest().build());
    }

    public ServerResponse handleNotification(ServerRequest request) {
        List<String> header = request.headers().header(PushNotificationAuth.AUTH_HEADER);
        String authorization = header.isEmpty() ? "" : header.get(0);
        try {
            Task data = request.body(Task.class);
            boolean verified = this.auth.verifyPushNotification(authorization, data);
            if (!verified) {
                verifyFailCount.incrementAndGet();
                log.info("push notification verification failed, authorization: {}, data: {}", authorization, Util.toJson(data));
                return ServerResponse.badRequest().body("push notification verification failed, authorization: " + authorization);
            }
            log.info("push notification received, authorization: {}, data: {}", authorization, Util.toJson(data));
        } catch (Exception e) {
            log.error("error verifying push notification, authorization: {}, error: {}", authorization, e.getMessage(), e);
            verifyFailCount.incrementAndGet();
            return ServerResponse.badRequest().body(e.getMessage());
        }
        return ServerResponse.ok().build();
    }

}
