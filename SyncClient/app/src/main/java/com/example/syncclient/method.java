package com.example.syncclient;

public enum method {

    TEST("TEST"),
    TEST_RESPONSE("TEST_RESPONSE"),
    UPLOAD_SYN("UPLOAD_SYN"),
    UPLOAD_ACK("UPLOAD_ACK"),
    UPLOAD_FILE("UPLOAD_FILE"),
    UPLOAD_FILE_ACK("UPLOAD_ACK"),
    UPLOAD_END("UPLOAD_END"),
    UPLOAD_END_ACK("UPLOAD_END_ACK"),
    UPLOAD_CANCEL("UPLOAD_CANCEL"),
    FILE_SAVED("FILE_SAVE"),
    FILE_SAVE_FAILED("FILE_SAVE"),
    LAST_UPLOADS_SYN("LAST_UPLOADS_SYN"),
    LAST_UPLOADS_ACK("LAST_UPLOADS_ACK"),
    FIND_SERVER("FIND_SERVER"),
    FIND_SERVER_ACK("FIND_SERVER_ACK"),
    UNKNOWN_METHOD("UNKNOWN_METHOD");

    private String method;

    private method(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

}

