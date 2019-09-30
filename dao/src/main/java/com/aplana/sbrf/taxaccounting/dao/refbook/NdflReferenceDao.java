package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.NumFor2Ndfl;

import java.util.List;

public interface NdflReferenceDao {

    /**
     * Обновить значение поля у записей
     * @param uniqueRecordIds
     * @param alias
     * @param value
     */
    int updateField(List<Long> uniqueRecordIds, String alias, String value);

    /**
     * Получение номера справки из сиквенса по отчетному году. В случае отсутствия сиквенса, его создание.
     * @param year год отчетного периода, по которому будет выбран/создан сиквенс
     * @return уникальный номер справки
     */
    Integer getNextSprNum(Integer year);

    /**
     * Получение номера справки для формирования 2-НДФЛ
     * @param personId физлицо
     * @param year ОНФ."Период"."Год"
     * @param kpp ОНФ.КПП
     * @param oktmo ОНФ.ОКТМО
     * @param declarationTypeId Форма."Макет формы"."Вид формы"
     * @return список номеров справок и номеров корректировок, подходящих под параметры
     *
     */
    List<NumFor2Ndfl> getCorrSprNum(Long personId, int year, String kpp, String oktmo, int declarationTypeId);
}
