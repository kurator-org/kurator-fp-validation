package org.kurator.validation.actors;

import org.kurator.akka.KuratorActor;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lowery on 7/23/17.
 */
public class NativePythonActor extends KuratorActor {
    private PythonInterpreter interpreter;

    @Override
    protected void onInitialize() throws Exception {
        interpreter = new PythonInterpreter();
    }

    @Override
    protected void onStart() throws Exception {

    }

    @Override
    public void onData(Object value) throws Exception {
        String module = (String)configuration.get("module");
        String onData = (String)configuration.get("onData");

        Object input = null;
        if (this.inputs.isEmpty()) {
            input = value;
        } else {
            input = mapInputs(value);
            ((Map)input).putAll(settings);
        }

        Map<String, String> response = interpreter.run(module, onData, (HashMap) input);
        broadcast(response);
    }

    private synchronized Map<String,Object> mapInputs(Object receivedValue) {

        Map<String,Object> mappedInputs = new HashMap<String,Object>();
        if (receivedValue instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String,Object> receivedValues = (Map<String,Object>)receivedValue;
            for (Map.Entry<String, String> mapEntry : this.inputs.entrySet()) {
                String incomingName = mapEntry.getKey();
                String localName = mapEntry.getValue();
                mappedInputs.put(localName, receivedValues.get(incomingName));
            }
        }

        return mappedInputs;
    }

}
