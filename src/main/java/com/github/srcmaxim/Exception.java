package com.github.srcmaxim;

public interface Exception {

    class AppException extends RuntimeException {
        public AppException(String message) {
            super(message, null, true, false);
        }
    }

}
