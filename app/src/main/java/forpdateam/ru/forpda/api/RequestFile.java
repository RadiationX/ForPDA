package forpdateam.ru.forpda.api;

import java.io.InputStream;

/**
 * Created by radiationx on 12.01.17.
 */

public class RequestFile {
    private InputStream fileStream;
    private String fileName, mimeType, requestName;

    public RequestFile(String fileName, String mimeType, InputStream fileStream) {
        this.fileStream = fileStream;
        this.fileName = fileName;
        this.mimeType = mimeType;
    }

    public RequestFile(String requestName, String fileName, String mimeType, InputStream fileStream) {
        this.requestName = requestName;
        this.fileStream = fileStream;
        this.fileName = fileName;
        this.mimeType = mimeType;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public InputStream getFileStream() {
        return fileStream;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setFileStream(InputStream fileStream) {
        this.fileStream = fileStream;
    }
}
