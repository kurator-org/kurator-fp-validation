package org.kurator.validation.data;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.filteredpush.kuration.util.SpecimenRecord ;

public class OrderedSpecimenRecord extends SpecimenRecord {

    private Set<String> header = new LinkedHashSet<String>();

    @Override
    public String put(String key, String value) {
        if (!super.containsKey(key)) {
            header.add(key);
        }
        return super.put(key, value.trim());
    }

    @Override
    public Set<String> keySet() {
        return new LinkedHashSet<String>(header);
    }
 
    @Override
    public Collection<String> values() {
        List<String> values = new LinkedList<String>();
        for (String fieldName : header) {
            values.add(get(fieldName));
        }
        return values;
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