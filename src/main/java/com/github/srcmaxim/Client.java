package com.github.srcmaxim;

import io.smallrye.mutiny.Uni;
import org.eclipse.yasson.FieldAccessStrategy;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class Client {

    private static Logger LOGGER = Logger.getLogger("client");

    public <T> Uni<T> getResponse(GetRequest<T> getRequest) {
        var uni = Uni.createFrom().item(getRequest)
                .onItem().transformToUni(i -> Uni.createFrom().completionStage(asyncRequest(i)));
        var uniRetry = uni.onFailure().retry()
                .withJitter(0.2).withBackOff(Duration.ofMillis(50), Duration.ofMillis(500)).atMost(10);
        return uniRetry
                .onItem().invoke(i -> LOGGER.infof("Received item: %s", i));
    }

    private <T> CompletableFuture<T> asyncRequest(GetRequest<T> getRequest) {
        var request = HttpRequest.newBuilder(getRequest.requestUrl())
                .version(HttpClient.Version.HTTP_2)
                .timeout(Duration.ofMillis(300))
                .header("Accept", "application/json")
                .build();
        return HttpClient.newHttpClient()
                .sendAsync(request, responseInfo -> {
                    if (responseInfo.statusCode() != 200)
                        throw new Exception.AppException("no_data");
                    return asJson(getRequest.tClass());
                }).thenApply(HttpResponse::body);
    }

    private static <T> HttpResponse.BodySubscriber<T> asJson(Class<T> targetType) {
        var jsonb = jsonb();
        var upstream = HttpResponse.BodySubscribers.ofInputStream();
        return HttpResponse.BodySubscribers.mapping(upstream, body -> jsonb.fromJson(body, targetType));
    }

    private static Jsonb jsonb() {
        var visibilityStrategy = new FieldAccessStrategy();
        var jsonbConfig = new JsonbConfig()
                .withPropertyVisibilityStrategy(visibilityStrategy);
        return JsonbBuilder.create(jsonbConfig);
    }

    record GetRequest<T>(URI requestUrl, Class<T> tClass) {
    }

}
