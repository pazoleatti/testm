package com.aplana.sbrf.taxaccounting.model.filter;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookAsnu;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Модель для параметров Фильтра вкладки "Сведения о доходах в виде авансовых платежей" страницу РНУ НДФЛ
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class NdflPersonPrepaymentFilter implements Serializable {
    /**
     * Общий фильтр
     */
    @ToString.Exclude
    private NdflFilter ndflFilter;
    /**
     * id операции
     */
    private String operationId;
    /**
     * Номер уведомления
     */
    private String notifNum;
    /**
     * Код НО, выдавшего уведомления
     */
    private String notifSource;
    /**
     * Дата выдачи уведомления с
     */
    private Date notifDateFrom;
    /**
     * Дата выдачи уведомления по
     */
    private Date notifDateTo;

    /**
     * Номер строки
     */
    private String rowNum;
    /**
     * Идентификатор строки
     */
    private String id;
    /**
     * Дата редактирования с
     */
    private Date modifiedDateFrom;
    /**
     * Дата редактирования по
     */
    private Date modifiedDateTo;
    /**
     * Обновил
     */
    private String modifiedBy;

    public NdflPersonPrepaymentFilter(NdflFilter ndflFilter) {
        this.ndflFilter = ndflFilter;
    }

    public List<RefBookAsnu> getAsnu() {
        return ndflFilter.getIncome().getAsnu();
    }
}
