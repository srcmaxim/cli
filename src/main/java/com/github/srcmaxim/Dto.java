package com.github.srcmaxim;

import javax.json.bind.annotation.JsonbCreator;

public interface Dto {

    record Post(long userId, long id, String title, String body) {
        @JsonbCreator
        public Post {
        }
    }

}
