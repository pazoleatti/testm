package com.aplana.sbrf.taxaccounting.model.ndfl;

import com.aplana.sbrf.taxaccounting.model.util.NdflComparator;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Andrey Drunk
 * Cведения о доходах в виде авансовых платежей
 */
public class NdflPersonPrepayment extends NdflPersonOperation {

    // Сумма фиксированного авансового платежа (Графа 4)
    private BigDecimal summ;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Номер уведомления
    private String notifNum;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Дата выдачи уведомления
    private Date notifDate;

    // Уведомление, подтверждающее право на уменьшение налога на фиксированные авансовые платежи.Код налогового органа, выдавшего уведомление (Графа 7)
    private String notifSource;

    // Дата и время редактирования. Заполняется при редактировании данных НФ через загрузку Excel файла
    private Date modifiedDate;

    // Значение имени пользователя из Справочника пользователей системы. Заполняется при редактировании данных НФ через загрузку Excel файла
    private String modifiedBy;

    public static final String TABLE_NAME = "ndfl_person_prepayment";
    public static final String SEQ = "seq_ndfl_person_prepayment";
    public static final String[] COLUMNS = {"id", "ndfl_person_id", "row_num", "operation_id", "summ", "notif_num",
            "notif_date", "notif_source", "source_id", "modified_date", "modified_by", "asnu_id"};
    public static final String[] FIELDS = {"id", "ndflPersonId", "rowNum", "operationId", "summ", "notifNum",
            "notifDate", "notifSource", "sourceId", "modifiedDate", "modifiedBy", "asnuId"};

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getSeq() {
        return SEQ;
    }

    @Override
    public String[] getColumns() {
        return COLUMNS;
    }

    @Override
    public String[] getFields() {
        return FIELDS;
    }

    public BigDecimal getSumm() {
        return summ;
    }

    public void setSumm(BigDecimal summ) {
        this.summ = summ;
    }

    public String getNotifNum() {
        return notifNum;
    }

    public void setNotifNum(String notifNum) {
        this.notifNum = notifNum;
    }

    public Date getNotifDate() {
        return notifDate;
    }

    public void setNotifDate(Date notifDate) {
        this.notifDate = notifDate;
    }

    public String getNotifSource() {
        return notifSource;
    }

    public void setNotifSource(String notifSource) {
        this.notifSource = notifSource;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @Override
    public String toString() {
        return "NdflPersonPrepayment{" +
                "summ=" + summ +
                ", notifNum='" + notifNum + '\'' +
                ", notifDate=" + notifDate +
                ", notifSource='" + notifSource + '\'' +
                '}';
    }

    /**
     * Получение компаратора для сортировки сведений о доходах в виде авансовых платежей {@link NdflPersonPrepayment}
     *
     * @param ndflPerson физическое лицо
     * @param <T>        тип объекта {@link NdflPerson} или его наследник
     * @return компаратор {@link NdflComparator} для сортировки {@link NdflPersonPrepayment}
     */
    public static <T extends NdflPerson> Comparator<NdflPersonPrepayment> getComparator(final T ndflPerson) {
        return new NdflComparator<NdflPersonPrepayment>() {
            @Override
            public int compare(NdflPersonPrepayment o1, NdflPersonPrepayment o2) {
                final List<String> operationIdOrderList = getOperationIdOrderList(ndflPerson);
                return compareValues(o1.operationId, o2.operationId, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return operationIdOrderList.indexOf(s1) - operationIdOrderList.indexOf(s2);
                    }
                });
            }

            /**
             * Получение списка идентификаторов операций для сведений о доходах физического лица (Раздел 2) {@link NdflPersonIncome#operationId}
             *
             * @param ndflPerson физическое лицо
             * @return список идентификаторов операций свединий о доходах ФЛ (Раздел 2) в порядке их следования в ndflPerson.incomes
             */
            private List<String> getOperationIdOrderList(T ndflPerson) {
                Set<String> operationIdOrderList = new LinkedHashSet<>();
                for (NdflPersonIncome income : ndflPerson.getIncomes()) {
                    operationIdOrderList.add(income.getOperationId());
                }
                return new ArrayList<>(operationIdOrderList);
            }
        };
    }
}
