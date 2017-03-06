package com.aplana.sbrf.taxaccounting.model.identification;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrey Drunk
 */
public class AttributeCountChangeListener implements AttributeChangeListener {

    private int refreshed = 0;
    private int created = 0;
    private int ignored = 0;

    Map<String, String> msg = new HashMap<String, String>();

    @Override
    public void processAttr(AttributeChangeEvent event) {
        if (AttributeChangeEventType.CREATED.equals(event.type)) {
            created++;
            //if (event.getValue() != null) {sb.append("[").append(event.getAttrName()).append(": ").append(event.getValue()).append("]")}
        } else if (AttributeChangeEventType.REFRESHED.equals(event.type)) {
            refreshed++;
            msg.put(event.getAttrName(), new StringBuilder().append(event.getCurrentValue()).append("->").append(event.getNewValue()).toString());
        } else if (AttributeChangeEventType.IGNORED.equals(event.type)) {
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
