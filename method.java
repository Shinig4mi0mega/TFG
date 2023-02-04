/*
 * Method can have multiple values that can be:
 * TEST:The server will answer with the data of the packet send
 * TEST_RESPONSE:Server response to TEST method client package
 * 
 * UPLOAD_SYN: Informs the server that the client wants to upload files
 * UPLOAD_ACK: Confirms the upload and that the server is ready to recive the files
 * UPLOAD_END:Informs the server there arent more files to send
 * UPLOAD_CANCEL:Server negates the upload to the client
 * 
 * UNKNOWN_METHOD:response to a unknown method send by server o client
 */

public enum method {

    TEST("TEST"),
    TEST_RESPONSE("TEST_RESPONSE"),
    UPLOAD_SYN("UPLOAD_SYN"),
    UPLOAD_ACK("UPLOAD_ACK"),
    UPLOAD_END("UPLOAD_END"),
    UPLOAD_CANCEL("UPLOAD_CANCEL"),
    UNKNOWN_METHOD("UNKNOWN_METHOD");

    private String method;

    private method(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }


}
