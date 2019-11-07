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
     * Получение количества сиквенсов по году отчетного периода
     * @param year год отчетного периода
     * @return количество найденных сиквенсов (в случае отсутствия нужно создать новый)
     */
    Integer countSequenceByYear(Integer year);

    /**
     * Получение номера справки из сиквенса по отчетному году.
     * @param year год отчетного периода, по которому будет выбран сиквенс
     * @return уникальный номер справки
     */
    Integer getNextSprNum(Integer year);

    /**
     * Создать сиквенс по отчетному году. Выполнять в отдельной транзакции.
     * @param year год отчетного периода, по которому будет создан сиквенс
     */
    void createSequence(Integer year);

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

    /**
     * Проверка наличие ранее созданной аннулирующей формы справки
     * @param declarationDataId Идентификатор налоговой формы к которой относятся данные
     * @param num Номер справки
     * @param lastName Фамилия
     * @param firstName Имя
     * @param middleName Отчество
     * @param innNp ИНН РФ
     * @param idDocNumber Номер ДУЛ
     * @return true если есть
     */
    Boolean checkExistingAnnulReport(Long declarationDataId, Integer num, String lastName, String firstName, String middleName, String innNp, String idDocNumber);

}
