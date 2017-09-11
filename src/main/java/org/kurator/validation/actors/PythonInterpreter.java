package org.kurator.validation.actors;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lowery on 7/23/17.
 */
public class PythonInterpreter {
    static {
        System.loadLibrary("kurator"); // Load native library at runtime
    }

    public final synchronized native Map<String, Object> run(String name, String func, HashMap<String, Object> options);

}
