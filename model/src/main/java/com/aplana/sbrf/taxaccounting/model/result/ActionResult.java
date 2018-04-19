package com.aplana.sbrf.taxaccounting.model.result;

/**
 * Результат операции. Содержит uuid, по которому можно получить сообщения в журнале
 */
public class ActionResult {
    /**
     * UUID группы сообщений в журнале
     */
    private String uuid;
    /**
     * Признак того, что операция завершена успешно
     */
    private boolean success;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ActionResult() {
    }

    public ActionResult(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
