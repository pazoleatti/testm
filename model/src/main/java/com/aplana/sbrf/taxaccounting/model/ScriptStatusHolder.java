package com.aplana.sbrf.taxaccounting.model;

/**
 * Объект-хранилище статуса выполнения скрипта
 * @author Dmitriy Levykin
 */
public class ScriptStatusHolder {

    private ScriptStatus scriptStatus = ScriptStatus.DEFAULT;

    private String statusMessage;

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
}
