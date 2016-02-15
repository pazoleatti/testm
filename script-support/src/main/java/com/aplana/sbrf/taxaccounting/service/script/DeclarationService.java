package com.aplana.sbrf.taxaccounting.service.script;

import com.aplana.sbrf.taxaccounting.model.*;
import com.aplana.sbrf.taxaccounting.model.log.Logger;
import com.aplana.sbrf.taxaccounting.util.ScriptExposed;

import javax.xml.stream.XMLStreamReader;
import java.util.List;
import java.util.zip.ZipInputStream;

@ScriptExposed
public interface DeclarationService {
    /**
     * Поиск декларации в отчетном периоде подразделения
     */
    List<DeclarationData> find(int declarationTypeId, int departmentReportPeriodId);

    /**
     * Поиск декларации в отчетном периоде подразделения + «КПП» и «Налоговый орган»
     */
    DeclarationData find(int declarationTypeId, int departmentReportPeriodId, String kpp, String taxOrganCode);

    /**
     * Декларация в последнем отчетном периоде подразделения
     */
    @SuppressWarnings("unused")
    DeclarationData getLast(int declarationTypeId, int departmentId, int reportPeriodId);

	/**
	 * Возвращает список налоговых форм, являющихся источником для указанной декларации и находящихся в статусе
	 * "Принята"
	 *
	 * @param declarationData декларация
	 * @return список НФ-источников в статусе "Принята"
	 */
	FormDataCollection getAcceptedFormDataSources(DeclarationData declarationData, TAUserInfo userInfo, Logger logger);

    /**
     * Получить данные декларации в формате законодателя (XML)
     * @param declarationDataId идентификатор декларации
     */
    @SuppressWarnings("unused")
    String getXmlData(long declarationDataId);

    /**
     * Получить данные декларации в формате законодателя (XML) в виде потока
     * @param declarationDataId идентификатор декларации
     */
    ZipInputStream getXmlStream(long declarationDataId);

    /**
     * Получить данные декларации в формате законодателя (XML) в виде потока для чтения StAX
     * @param declarationDataId идентификатор декларации
     */
    XMLStreamReader getXmlStreamReader(long declarationDataId);

    /**
     * Проверить существование декларации в отчетном периоде (без учета подразделения).
     *
     * @param declarationTypeId идентификатор типа декларации
     * @param reportPeriodId идентификатор отчетного периода
     */
    @SuppressWarnings("unused")
    boolean checkExistDeclarationsInPeriod(int declarationTypeId, int reportPeriodId);

    /**
     * Проверка декларации на уникальность с аналогичными параметрам
     */
    @SuppressWarnings("unused")
    boolean checkUnique(DeclarationData declarationData, Logger logger);
    /**
     * Получить имя файла в формате законодателя
     * @param declarationDataId идентификатор декларации
     * @return имя файла взятого из xml данных
     * @throws com.aplana.sbrf.taxaccounting.model.exception.AccessDeniedException - если у пользователя нет прав на просмотр данной декларации
     */
    String getXmlDataFileName(long declarationDataId);

    /**
     * Возвращает список нф-источников для указанной декларации (включая несозданные)
     * @param sourceFormData нф-источник
     * @param light true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные приемники
     * @param stateRestriction ограничение по состоянию для созданных экземпляров
     * @return список нф-источников
     */
    List<Relation> getDeclarationDestinationsInfo(FormData sourceFormData, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction,
                                                  TAUserInfo userInfo, Logger logger);

    /**
     * Возвращает список нф-источников для указанной декларации (включая несозданные)
     * @param declaration декларациz-приемник
     * @param light true - заполнятся только текстовые данные для GUI и сообщений
     * @param excludeIfNotExist true - исключить несозданные источники
     * @param stateRestriction ограничение по состоянию для созданных экземпляров
     * @return список нф-источников
     */
    List<Relation> getDeclarationSourcesInfo(DeclarationData declaration, boolean light, boolean excludeIfNotExist, WorkflowState stateRestriction,
                                             TAUserInfo userInfo, Logger logger);

    /**
     * Получить список id типов декларации для указанного налога.
     *
     * @param taxType тип налога
     */
    List<Integer> getDeclarationTypeIds(TaxType taxType);
}
