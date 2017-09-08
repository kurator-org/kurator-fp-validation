package org.kurator.validation.actors;

import java.util.Map;

/**
 * Created by lowery on 7/23/17.
 */
public class PythonInterpreter {
    static {
        System.loadLibrary("kurator"); // Load native library at runtime
    }

    public final native Map<String, String> run(String name, String func, Map<String, String> options);

}
