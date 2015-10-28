package com.aplana.sbrf.taxaccounting.model;

/**
 * Объект-хранилище статуса выполнения скрипта
 * @author Dmitriy Levykin
 */
public class ScriptStatusHolder {

    private ScriptStatus scriptStatus = ScriptStatus.DEFAULT;

    private String statusMessage;

    private int successCount = 0;
    private int totalCount = 0;

    /**
     * Статус скрипта
     */
    public ScriptStatus getScriptStatus() {
        return scriptStatus;
    }

    /**
     * Установка статуса скрипта, вызывается из скрипта
     */
    public void setScriptStatus(ScriptStatus scriptStatus) {
        this.scriptStatus = scriptStatus;
    }

    /**
     * Расшифровка статуса (может отсутствовать)
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Расшифровка статуса (может отсутствовать)
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * Количество записей
     */
    public int getSuccessCount() {
        return successCount;
    }

    /**
     * Количество записей
     */
    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
}
