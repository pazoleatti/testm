package com.aplana.sbrf.taxaccounting.model.filter;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * Модель для параметров Фильтра вкладки "Сведения о доходах в виде авансовых платежей" страницу РНУ НДФЛ
 */
@Getter
@Setter
@ToString
public class NdflPersonPrepaymentFilter {
    /**
     * id формы
     */
    private long declarationDataId;
    /**
     * ИНП
     */
    private String inp;
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
}
