package hh.yarkinsv.files;

import java.util.Arrays;

public enum ContentType {
    Text("text/html", new String[] {"html", "htm"}),
    Application("application/javascript", new String[] {"js"}),
    Image("image/jpeg", new String[] { "jpg", "jpeg" });

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
