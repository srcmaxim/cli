package com.github.srcmaxim;

import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;

@Command(name = "http", mixinStandardHelpOptions = true)
public class HttpCommand implements Runnable {

    private static Logger LOGGER = Logger.getLogger("http");

    @Parameters(paramLabel = "<url>", defaultValue = "https://jsonplaceholder.typicode.com/posts/1", description = "HTTP URL")
    String url;

    @Inject
    Client client;

    @Override
    public void run() {
        var time = System.currentTimeMillis();
        var responseA = client.getResponse(new Client.GetRequest<>(URI.create("https://jsonplaceholder.typicode.com/posts/1"), Dto.Post.class));
        var responseB = client.getResponse(new Client.GetRequest<>(URI.create("https://jsonplaceholder.typicode.com/posts/2"), Dto.Post.class));
        var responseC = client.getResponse(new Client.GetRequest<>(URI.create("https://jsonplaceholder.typicode.com/posts/3"), Dto.Post.class));
        var response = Uni.combine().all().unis(responseA, responseB, responseC)
                .combinedWith((item1, item2, item3) -> List.of(item1, item2, item3))
                .onItem().invoke(i -> LOGGER.infof("""
                        Response Bodies: %s at %d
                        """, i, System.currentTimeMillis() - time))
                .await().indefinitely();
    }

}
