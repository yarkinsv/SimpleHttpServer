package hh.yarkinsv;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class HTTPResponse {

    public enum ResponseType {
        OK(200, "OK"),
        FileNotFound(404, "File Not Found"),
        MethodNotAllowed(405, "Method Not Allowed"),
        BadRequest(400, "Bad Request"),
        NotModified(304, "Not Modified"),
        InternalServerError(500, "Internal Server Error");

        private int responseCode;
        private String responseReason;

        private ResponseType(int responseCode, String responseReason) {
            this.responseCode = responseCode;
            this.responseReason = responseReason;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public String getResponseReason() {
            return responseReason;
        }
    }

    private String version = "HTTP/1.1";
    private ResponseType responseType = ResponseType.OK;
    private Map<String, String> headers = new LinkedHashMap<String, String>();
    private byte[] content;

    private void addDefaultHeaders() {
        headers.put("Date", new Date().toString());
        headers.put("Server", "Java NIO Webserver");
        headers.put("Connection", "close");
    }

    public void addHeaders() {
        addDefaultHeaders();

        if (content == null) {
            content = new byte[0];
        }

        if (content != null) {
            headers.put("Content-Length", Integer.toString(content.length));
        }
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public int getResponseCode() {
        return responseType.getResponseCode();
    }

    public String getResponseReason() {
        return responseType.getResponseReason();
    }

    public String getVersion() {
        return version;
    }

    public String getHeader(String header) {
        return headers.get(header);
    }

    public byte[] getContent() {
        return content;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
