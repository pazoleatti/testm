package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Модель блокировок для отображения в таблице
 */
@Getter
@Setter
@ToString
public class LockDataDTO implements Serializable {
    private static final long serialVersionUID = 2298941928955273347L;

    /* Идентификатор блокировки */
    private long id;
    /* Ключ блокировки */
    private String key;
    /* Описание блокировки */
    private String description;
    /* Полное имя пользователя, установившего блокировку*/
    private String user;
    /* Дата установки блокировки */
    private Date dateLock;
    /* Позволено ли текущему пользователю удалять блокировку */
    private boolean allowedToUnlock = false;
}
