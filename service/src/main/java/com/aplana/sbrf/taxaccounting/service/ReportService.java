package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationReportType;

import java.util.Collection;
import java.util.List;

/**
 * Интерфейс для работы с таблицей отчетов
 */
@ScriptExposed
public interface ReportService {
    /**
     * Создание записи об отчете декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param blobDataId        идентификатор блоба
     * @param type              тип отчета
     */
    void attachReportToDeclaration(long declarationDataId, String blobDataId, DeclarationReportType type);

    /**
     * Получение записи об отчете декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     * @return uuid идентификатор блоба
     */
    String getReportFileUuid(long declarationDataId, DeclarationReportType type);

    /**
     * То же что {@link #getReportFileUuid(long, DeclarationReportType)}, но с проверкой прав
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     * @return uuid идентификатор блоба
     */
    String getReportFileUuidSafe(long declarationDataId, DeclarationReportType type);

    /**
     * Удаление всех отчетов по id декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    void deleteAllByDeclarationId(long declarationDataId);

    /**
     * Удаляет отчет декларации по типу
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     */
    void deleteByDeclarationAndType(long declarationDataId, DeclarationReportType type);

    /**
     * Удаляет отчет декларации по типу
     *
     * @param declarationDataIds идентификаторы декларации
     * @param reportTypes        типы отчета
     */
    void deleteDec(Collection<Long> declarationDataIds, List<DeclarationReportType> reportTypes);

    /**
     * Удаление отчета по uuid
     *
     * @param uuid идентификатор блоба
     */
    void deleteDec(String uuid);

    /**
     * Удаление отчетов декларации по id, кроме {@link DeclarationReportType#XML_DEC}
     *
     * @param declarationDataId идентификатор декларации
     */
    void deleteNotXmlDec(long declarationDataId);

    /**
     * Удаляет спецотчет налоговой формы
     *
     * @param declarationDataId идентификатор декларации
     * @param subreportAlias    альяс спецотчета
     */
    void deleteSubreport(long declarationDataId, String subreportAlias);
}