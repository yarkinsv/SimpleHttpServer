package hh.yarkinsv.files;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileInfo {
    private String fileName;
    private String etag;
    private byte[] fileBody = new byte[0];
    private byte[] fileBodyUTF8 = new byte[0];
    private byte[] fileBodyASCII = new byte[0];
    private ContentType contentType;

    public FileInfo(String fileName, byte[] fileBody) {
        this.fileName = fileName;
        this.fileBody = fileBody;
        try {
            this.etag = MessageDigest.getInstance("MD5").digest(this.fileBody).toString();
        } catch (NoSuchAlgorithmException ex) {
            this.etag = "";
        }
        this.contentType = ContentType.getContentTypeByExtension(fileName.substring(fileName.lastIndexOf(".") + 1));
        if (contentType == ContentType.Text) {
            try {
                String str = new String(fileBody, "UTF-8");
                this.fileBodyUTF8 = str.getBytes(Charset.forName("UTF-8"));
                str = new String(fileBody, "US-ASCII");
                this.fileBodyASCII = str.getBytes(Charset.forName("US-ASCII"));
            } catch (UnsupportedEncodingException ex) {

            }
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getEtag() {
        return etag;
    }

    public long getBodySize() {
        return (fileBody.length + fileBodyUTF8.length + fileBodyASCII.length) / 1024;
    }

    public byte[] getFileBody(String encoding) {
        if (encoding == null) {
            return fileBody;
        }

        if (getContentType() == ContentType.Text) {
            if (encoding.equals("UTF-8") && this.fileBodyUTF8 != null) {
                return this.fileBodyUTF8;
            } else if (encoding.equals("US-ASCII") && this.fileBodyASCII != null) {
                return this.fileBodyASCII;
            } else {
                return this.fileBodyUTF8;
            }
        }
        return fileBody;
    }

    public ContentType getContentType() {
        return contentType;
    }
}
