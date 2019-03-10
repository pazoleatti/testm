package com.aplana.sbrf.taxaccounting.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class NotificationsFilterData implements Serializable {
    private static final long serialVersionUID = -2957561387544546546L;

    // если надо можно еще добавлять
    public enum SortColumn {
        DATE,
        TEXT
    }

    /**
     * Фильтр по подразделению-отправителю
     */
    private Integer senderDepartmentId;
    /**
     * Фильтр по подразделению-получателю
     */
    private List<Integer> receiverDepartmentIds;
    /**
     * Фильтр по конкретному пользователю, ожидающему уведомлений
     */
    private Integer userId;
    /**
     * Фильтр по ролям пользователя
     */
    private List<Integer> userRoleIds;
    /**
     * Фильтр по тексту в сообщениях уведомлений.
     */
    private String text;
    /**
     * Фильтр по дате уведомлений - нижний порог
     */
    private Date timeFrom;
    /**
     * Фильтр по дате уведомлений - верхний порог
     */
    private Date timeTo;
    /**
     * Признак прочтения. Если null - то не учитывается
     */
    private Boolean read;

    @Deprecated
    private Integer countOfRecords;
    @Deprecated
    private Integer startIndex;

    @Deprecated
    private SortColumn sortColumn = SortColumn.DATE;
    @Deprecated
    private boolean isAsc = false;

    public Boolean isRead() {
        return read;
    }
}
