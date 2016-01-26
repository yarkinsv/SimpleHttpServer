package hh.yarkinsv.files;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class FileInfo {
    private String fileName;
    private String etag;
    private byte[] fileBody;
    private ContentType contentType;

    public FileInfo(String fileName, byte[] fileBody) {
        this.fileName = fileName;
        //fileName.lastIndexOf('.')
        this.fileBody = fileBody;
    }

    public String getFileName() {
        return fileName;
    }

    public String getEtag() {
        return etag;
    }

    public byte[] getFileBody(String encoding)  {
        try {
            if (encoding == "UTF-8" || encoding == "US-ASCII") {
                String str = new String(fileBody, encoding);
                fileBody = str.getBytes(Charset.forName(encoding));
            }
        } catch (UnsupportedEncodingException ex) {
        }

        return fileBody;
    }

    public ContentType getContentType() {
        return contentType;
    }
}
