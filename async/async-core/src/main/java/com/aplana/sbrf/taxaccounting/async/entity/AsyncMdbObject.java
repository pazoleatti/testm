package com.aplana.sbrf.taxaccounting.async.entity;

import java.io.Serializable;
import java.util.Map;

/**
 * Объект, используемый для передачи параметров в сообщения очередей
 * Необходим т.к. MapMessage поддерживает только передачу примитивов
 * @author dloshkarev
 */
public class AsyncMdbObject implements Serializable {
    private static final long serialVersionUID = -7524392747144658654L;

    /** Идентификатор типа задачи */
    private long taskTypeId;

    /** Параметры. Все значения обязательно должны быть сериализуемы */
    private Map<String, Object> params;

    public long getTaskTypeId() {
        return taskTypeId;
    }

    public void setTaskTypeId(long taskTypeId) {
        this.taskTypeId = taskTypeId;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
