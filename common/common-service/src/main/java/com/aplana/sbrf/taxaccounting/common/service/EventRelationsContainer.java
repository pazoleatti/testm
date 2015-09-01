package com.aplana.sbrf.taxaccounting.common.service;


import com.aplana.sbrf.taxaccounting.common.model.EventType;
import com.aplana.sbrf.taxaccounting.model.FormDataEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс хранящий соотношения события АПИ к событию в системе УН
 *
 * @author aivanov
 */
public class EventRelationsContainer {

    private static EventRelationsContainer INST;

    private Map<EventType, FormDataEvent> map;

    private EventRelationsContainer() {
        map = new HashMap<EventType, FormDataEvent>();

        map.put(EventType.UPLOAD_CONTRACTS, FormDataEvent.GAR_UPLOAD_CONTRACTS);
        map.put(EventType.UPLOAD_CUSTOMER_DATA, FormDataEvent.GAR_UPLOAD_CUSTOMER_DATA);
        map.put(EventType.UPLOAD_PAYMENTS_DATA, FormDataEvent.GAR_UPLOAD_PAYMENTS_DATA);
        map.put(EventType.UPLOAD_REFBOOK, FormDataEvent.GAR_UPLOAD_REFBOOK);

        map.put(EventType.CREATE_CLIENT_PROFILES, FormDataEvent.GAR_CREATE_CLIENT_PROFILES);
        map.put(EventType.EDIT_CLIENT_PROFILES, FormDataEvent.GAR_EDIT_CLIENT_PROFILES);

        map.put(EventType.CREATE_GUARANTEE, FormDataEvent.GAR_CREATE_GUARANTEE);
        map.put(EventType.EDIT_GUARANTEE, FormDataEvent.GAR_EDIT_GUARANTEE);
        map.put(EventType.CLOSE_GUARANTEE, FormDataEvent.GAR_CLOSE_GUARANTEE);

        map.put(EventType.CREATE_OBSP, FormDataEvent.GAR_CREATE_OBSP);
        map.put(EventType.EDIT_OBSP, FormDataEvent.GAR_EDIT_OBSP);
        map.put(EventType.CLOSE_OBSP, FormDataEvent.GAR_CLOSE_OBSP);

        map.put(EventType.CREATE_TASK_FORMATION_RNU23, FormDataEvent.GAR_CREATE_TASK_FORMATION_RNU23);
        map.put(EventType.CREATE_TASK_FORMATION_REPORT, FormDataEvent.GAR_CREATE_TASK_FORMATION_REPORT);

    }

    public static EventRelationsContainer get() {
        if (INST == null) INST = new EventRelationsContainer();
        return INST;
    }

    public static FormDataEvent get(EventType eventType) {
        return get().getMap().get(eventType);
    }

    public static EventType get(FormDataEvent formDataEvent) {
        for (Map.Entry<EventType, FormDataEvent> set : get().getMap().entrySet()) {
            if (set.getValue() == formDataEvent) {
                return set.getKey();
            }
        }
        return null;
    }

    public Map<EventType, FormDataEvent> getMap() {
        return map;
    }
}