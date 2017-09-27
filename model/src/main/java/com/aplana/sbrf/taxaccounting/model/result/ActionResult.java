package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат операции. Содержит uuid, по которому можно получить сообщения в журнале
 */
public class ActionResult {
    /**
     * UUID группы сообщений в журнале
     */
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
