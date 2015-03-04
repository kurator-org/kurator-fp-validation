package org.kurator.validation.data;

import java.util.LinkedHashSet;
import java.util.Set;

import fp.util.SpecimenRecord;

public class OrderedSpecimenRecord extends SpecimenRecord {

    private Set<String> header;

    public OrderedSpecimenRecord() {
        super();
        header = new LinkedHashSet<String>();
    }

    @Override
    public String put(String key, String value) {
        header.add(key);
        return super.put(key, value);
    }

    @Override
    public Set<String> keySet() {
        return new LinkedHashSet<String>(header);
    }

    @Override
    public String toString() {

        boolean isFirst = true;

        StringBuffer buffer = new StringBuffer("{");

        for (String item : header){
            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(item)
                  .append("=")
                  .append(get(item));
        }

        buffer.append("}");

        return buffer.toString();
    }
    private static final long serialVersionUID = 1L;

}
