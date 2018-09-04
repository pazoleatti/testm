package com.aplana.sbrf.taxaccounting.model.identification;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public class AttributeCountChangeListener implements AttributeChangeListener {

    private int refreshed = 0;
    private int created = 0;
    private int ignored = 0;

    Map<String, String> msg = new LinkedHashMap<>();

    @Override
    public void processAttr(AttributeChangeEvent event) {
        processAttr(null, event);
    }


    public void processAttr(String info, AttributeChangeEvent event) {
        if (AttributeChangeEventType.CREATED.equals(event.getType())) {
            created++;
            //if (event.getValue() != null) {sb.append("[").append(event.getAttrName()).append(": ").append(event.getValue()).append("]")}
        } else if (AttributeChangeEventType.REFRESHED.equals(event.getType())) {
            refreshed++;
            msg.put(event.getAttrName(), new StringBuilder().append(info != null ? info : "").append(event.getCurrentValue() != null ? event.getCurrentValue() : "__").append(" -> ").append(event.getNewValue() != null ? event.getNewValue() : "__").toString());
        } else if (AttributeChangeEventType.IGNORED.equals(event.getType())) {
            ignored++;
        }
    }


    public Map<String, String> getMessages() {
        return msg;
    }

    public boolean isUpdate() {
        return (created != 0 || refreshed != 0);
    }

}
