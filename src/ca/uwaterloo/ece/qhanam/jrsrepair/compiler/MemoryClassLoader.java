package ca.uwaterloo.ece.qhanam.jrsrepair.compiler;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject.Kind;
import javax.tools.ToolProvider;

import org.apache.commons.lang3.StringUtils;

/**
 * From https://weblogs.java.net/blog/malenkov/archive/2008/12/how_to_compile.html
 */
public class MemoryClassLoader extends ClassLoader {
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final MemoryFileManager manager = new MemoryFileManager(this.compiler);

    public MemoryClassLoader(String classname, String filecontent, String[] classpath, Writer output) {
        this(Collections.singletonMap(classname, filecontent), classpath, output);
    }

    public MemoryClassLoader(Map<String, String> map, String[] classpath, Writer output) {
        List<Source> list = new ArrayList<Source>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            list.add(new Source(entry.getKey(), Kind.SOURCE, entry.getValue()));
        }
        
        List<String> optionList = new ArrayList<String>();

        optionList.add("-classpath");
        optionList.add(StringUtils.join(classpath, ":"));
        optionList.add("-nowarn");

        this.compiler.getTask(output, this.manager, null, optionList, null, list).call();
    }
    
    /**
     * Introduced for JRSRepair. Returns all compiled classes.
     * @return
     */
    public List<Output> getAllClasses(){
    	List<Output> classFiles = new LinkedList<Output>();

    	for(String file : this.manager.map.keySet()){
    		classFiles.add(this.manager.map.get(file));
    	}
    	
    	return classFiles;
    }
    
    /**
     * Introduced for JRSRepair
     * @param name
     * @return
     */
    public byte[] getClassBytes(String name) {
    	Output mc = this.manager.map.get(name);
    	if(mc != null) {
    		byte[] array = mc.toByteArray();
    		return array;
    	}
    	return null;
    }

	@Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Class findClass(String name) throws ClassNotFoundException {
        synchronized (this.manager) {
            Output mc = this.manager.map.remove(name);
            if (mc != null) {
                byte[] array = mc.toByteArray();
                return defineClass(name, array, 0, array.length);
            }
        }
        return super.findClass(name);
    }
}
