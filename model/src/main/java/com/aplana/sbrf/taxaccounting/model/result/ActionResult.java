package com.aplana.sbrf.taxaccounting.model.result;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Результат операции. Содержит uuid, по которому можно получить сообщения в журнале
 */
@Setter
@Getter
@NoArgsConstructor
public class ActionResult {
    //UUID группы сообщений в журнале
    private String uuid;
    //Признак того, что операция завершена успешно
    private boolean success;
    //Сообщение об ошибке
    private String error;

    public ActionResult(String uuid) {
        this.uuid = uuid;
    }

    public ActionResult error(String error) {
        this.error = error;
        return this;
    }

    public ActionResult uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
}
