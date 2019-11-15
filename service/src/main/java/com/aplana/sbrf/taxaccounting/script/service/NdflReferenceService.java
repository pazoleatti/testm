package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.ReportFormsCreationParams;
import com.aplana.sbrf.taxaccounting.model.refbook.NumFor2Ndfl;
import com.aplana.sbrf.taxaccounting.model.refbook.ReferenceAnnulResult;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.List;

@ScriptExposed
public interface NdflReferenceService {

    /**
     * Получение номера справки для формирования 2-НДФЛ
     * @param personId физлицо
     * @param year ОНФ."Период"."Год"
     * @param kpp ОНФ.КПП
     * @param oktmo ОНФ.ОКТМО
     * @param declarationTypeId Форма."Макет формы"."Вид формы"
     * @return список номеров справок и номеров корректировок, подходящих под параметры
     */
    List<NumFor2Ndfl> getCorrSprNum(Long personId, int year, String kpp, String oktmo, int declarationTypeId);

    /**
     * Получение номера справки из сиквенса по отчетному году. В случае отсутствия сиквенса, его создание.
     * @param year год отчетного периода, по которому будет выбран/создан сиквенс
     * @return уникальный номер справки
     */
    Integer getNextSprNum(Integer year);

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

    /**
     * Получение всех записей из реестра справок с номером корректировки 99
     * @param lastName Фамилия
     * @param firstName Имя
     * @param middleName Отчество
     * @return список найденных записей из реестра справок
     */
    List<ReferenceAnnulResult> findAllReferencesRegistryAnnulByFio(String lastName, String firstName, String middleName);


}
