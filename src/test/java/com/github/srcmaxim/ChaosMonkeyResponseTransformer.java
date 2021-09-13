package com.github.srcmaxim;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ChaosMonkeyResponseTransformer extends ResponseTransformer {

    private final Random random = new Random();
    private final AtomicInteger count = new AtomicInteger();

    @Override
    public String getName() {
        return "chaos-monkey";
    }

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        int status = count.incrementAndGet() > 2 ? 200 : 400;
        return Response.Builder.like(response)
                .but()
                .status(status)
                .incrementInitialDelay(random.nextInt(50))
                .build();
    }

}
