package com.github.srcmaxim;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientTest {

    private static WireMockServer wireMockServer;

    @BeforeAll
    static void setup() {
        var options = options()
                .dynamicPort()
                .extensions(ChaosMonkeyResponseTransformer.class);
        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
    }

    @AfterAll
    static void teardown() {
        wireMockServer.stop();
    }

    @Test
    void testCall() {
        wireMockServer.stubFor(get("/posts/1")
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(ok()
                        .withTransformers("chaos-monkey")
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": 1,
                                  "userId": 2,
                                  "title": "Title",
                                  "body": "Body"
                                }
                                """)));

        var url = URI.create(wireMockServer.baseUrl() + "/posts/1");
        var client = new Client();
        var postGetRequest = new Client.GetRequest<>(url, Dto.Post.class);
        var postUni = client.getResponse(postGetRequest)
                .subscribe().withSubscriber(UniAssertSubscriber.create());
        var post = postUni.awaitItem(Duration.ofSeconds(2)).getItem();
        assertEquals(1L, post.id());
        assertEquals(2L, post.userId());
        assertEquals("Title", post.title());
        assertEquals("Body", post.body());

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/posts/1"))
                .withHeader("Accept", equalTo("application/json")));
    }

}