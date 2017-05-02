package forpdateam.ru.forpda.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import forpdateam.ru.forpda.api.RequestFile;

/**
 * Created by radiationx on 02.05.17.
 */

public class ForPdaRequest {
    private String url = "";
    private Map<String, String> headers, formHeaders;
    private Set<String> encodedFormHeaders;
    private boolean isMultipartForm = false;
    private RequestFile file = null;
    //true - get, false - post
    private boolean method = true;

    public ForPdaRequest(Builder builder) {
        this.url = builder.url;
        this.headers = builder.headers;
        this.formHeaders = builder.formHeaders;
        this.encodedFormHeaders = builder.encodedFormHeaders;
        this.isMultipartForm = builder.isMultipartForm;
        this.file = builder.file;
        this.method = builder.method;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getFormHeaders() {
        return formHeaders;
    }

    public Set<String> getEncodedFormHeaders() {
        return encodedFormHeaders;
    }

    public boolean isMultipartForm() {
        return isMultipartForm;
    }

    public RequestFile getFile() {
        return file;
    }

    public boolean getMethod() {
        return method;
    }

    public static class Builder {
        private String url = "";
        private Map<String, String> headers, formHeaders;
        private Set<String> encodedFormHeaders;
        private boolean isMultipartForm = false;
        private RequestFile file = null;
        private boolean method = true;

        public ForPdaRequest.Builder url(String url) {
            this.url = url;
            return this;
        }

        public ForPdaRequest.Builder addHeaders(Map<String, String> headers) {
            if (this.headers == null)
                this.headers = new HashMap<>();
            this.headers.putAll(headers);
            return this;
        }

        public ForPdaRequest.Builder addHeader(String name, String value) {
            if (this.headers == null)
                this.headers = new HashMap<>();
            this.headers.put(name, value);
            return this;
        }

        public ForPdaRequest.Builder formHeaders(Map<String, String> formHeaders) {
            return formHeaders(formHeaders, false);
        }

        public ForPdaRequest.Builder formHeaders(Map<String, String> formHeaders, boolean encoded) {
            if (this.formHeaders == null)
                this.formHeaders = new HashMap<>();
            this.formHeaders.putAll(formHeaders);
            if (encoded) {
                if (this.encodedFormHeaders == null) {
                    encodedFormHeaders = new HashSet<>();
                }
                encodedFormHeaders.addAll(this.formHeaders.keySet());
            }
            method = false;
            return this;
        }

        public ForPdaRequest.Builder formHeader(String name, String value) {
            return formHeader(name, value, false);
        }

        public ForPdaRequest.Builder formHeader(String name, String value, boolean encoded) {
            if (this.formHeaders == null)
                this.formHeaders = new HashMap<>();
            this.formHeaders.put(name, value);
            if (encoded) {
                if (this.encodedFormHeaders == null) {
                    encodedFormHeaders = new HashSet<>();
                }
                encodedFormHeaders.add(name);
            }
            method = false;
            return this;
        }

        public ForPdaRequest.Builder multipart() {
            isMultipartForm = true;
            return this;
        }

        public Builder file(RequestFile file) {
            this.file = file;
            isMultipartForm = true;
            method = false;
            return this;
        }

        public ForPdaRequest build() {
            return new ForPdaRequest(this);
        }
    }
}
