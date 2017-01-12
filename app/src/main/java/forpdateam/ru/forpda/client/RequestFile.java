package forpdateam.ru.forpda.client;

import java.io.InputStream;

/**
 * Created by radiationx on 12.01.17.
 */

public class RequestFile {
    private InputStream fileStream;
    private String fileName, fileScheme, requestName;

    public RequestFile(String fileName, String fileScheme, InputStream fileStream) {
        this.fileStream = fileStream;
        this.fileName = fileName;
        this.fileScheme = fileScheme;
    }

    public RequestFile(String requestName, String fileName, String fileScheme, InputStream fileStream) {
        this.requestName = requestName;
        this.fileStream = fileStream;
        this.fileName = fileName;
        this.fileScheme = fileScheme;
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

    public String getFileScheme() {
        return fileScheme;
    }

    public String getRequestName() {
        return requestName;
    }
}
