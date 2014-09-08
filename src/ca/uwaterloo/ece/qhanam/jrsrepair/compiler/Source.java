package ca.uwaterloo.ece.qhanam.jrsrepair.compiler;

import java.net.URI;

import javax.tools.SimpleJavaFileObject;

/**
 * From https://weblogs.java.net/blog/malenkov/archive/2008/12/how_to_compile.html
 */
public class Source extends SimpleJavaFileObject {
    private final String content;

    public Source(String name, Kind kind, String content) {
        super(URI.create("memo:///" + name.replace('.', '/') + kind.extension), kind);
        this.content = content;
    }

    @Override
    public CharSequence getCharContent(boolean ignore) {
        return this.content;
    }
}
