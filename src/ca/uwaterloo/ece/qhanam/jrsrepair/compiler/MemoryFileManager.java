package ca.uwaterloo.ece.qhanam.jrsrepair.compiler;

import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject.Kind;

public class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
   public final Map<String, Output> map = new HashMap<String, Output>();

    MemoryFileManager(JavaCompiler compiler) {
        super(compiler.getStandardFileManager(null, null, null));
    }

    @Override
    public Output getJavaFileForOutput
            (Location location, String name, Kind kind, FileObject source) {
        Output mc = new Output(name, kind);
        this.map.put(name, mc);
        return mc;
    }
}
