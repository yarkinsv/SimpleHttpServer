package hh.yarkinsv.files;

import java.util.Arrays;

public enum ContentType {
    Text("text/html", new String[] {"txt", "html", "rtf"}),
    Application("application/javascript", new String[] {"exe", "js"}),
    Image("image/jpeg", new String[] {"img", "jpg", "jpeg", "png"});

    private String name;
    private String[] extensions;

    private ContentType(String name, String[] extensions) {
        this.name = name;
        this.extensions = extensions;
    }

    public String getName() {
        return name;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public static ContentType getContentTypeByExtension(String extension) {
        if (Arrays.asList(ContentType.Text.getExtensions()).contains(extension)) {
            return ContentType.Text;
        } else if (Arrays.asList(ContentType.Application.getExtensions()).contains(extension)) {
            return ContentType.Application;
        } else if (Arrays.asList(ContentType.Image.getExtensions()).contains(extension)) {
            return ContentType.Image;
        } else {
            return null;
        }
    }
}
