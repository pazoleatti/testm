package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.FormDataCollection;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

@ScriptExposed
public interface DeclarationService {
    /**
     * Поиск декларации в отчетном периоде подразделения
     */
    DeclarationData find(int declarationTypeId, int departmentReportPeriodId);

    /**
     * Декларация в последнем отчетном периоде подразделения
     */
    @SuppressWarnings("unused")
    DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId);

	/**
	 * Создает идентификатор xml файла для декларации.
	 * @param declarationTypeId идентификатор типа декларации
	 * @param departmentId идентификатор {@link com.aplana.sbrf.taxaccounting.model.Department подразделения}
	 * @return идентификатор xml файла
	 */
	String generateXmlFileId(int declarationTypeId, int departmentId, int reportPeriodId);
	
	/**
	 * Возвращает список налоговых форм, являющихся источником для указанной декларации и находящихся в статусе
	 * "Принята"
	 *
	 * @param declarationData декларация
	 * @return список НФ-источников в статусе "Принята"
	 */
	FormDataCollection getAcceptedFormDataSources(DeclarationData declarationData);

    /**
     * Получить данные декларации в формате законодателя (XML)
     * @param declarationDataId идентификатор декларации
     */
    @SuppressWarnings("unused")
    String getXmlData(long declarationDataId);

    /**
     * Проверить существование декларации в отчетном периоде (без учета подразделения).
     *
     * @param declarationTypeId идентификатор типа декларации
     * @param reportPeriodId идентификатор отчетного периода
     */
    boolean checkExistDeclarationsInPeriod(int declarationTypeId, int reportPeriodId);
}
