package com.aplana.sbrf.taxaccounting.service;

import com.aplana.sbrf.taxaccounting.model.DeclarationDataReportType;

import java.util.Collection;
import java.util.List;

/**
 * Интерфейс для работы с таблицей отчетов
 */
public interface ReportService {
    /**
     * Создание записи об отчете декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param blobDataId        идентификатор блоба
     * @param type              тип отчета
     */
    void attachReportToDeclaration(long declarationDataId, String blobDataId, DeclarationDataReportType type);

    /**
     * Получение записи об отчете декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     * @return uuid идентификатор блоба
     */
    String getReportFileUuid(long declarationDataId, DeclarationDataReportType type);

    /**
     * То же что {@link #getReportFileUuid(long, DeclarationDataReportType)}, но с проверкой прав
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     * @return uuid идентификатор блоба
     */
    String getReportFileUuidSafe(long declarationDataId, DeclarationDataReportType type);

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
    void deleteByDeclarationAndType(long declarationDataId, DeclarationDataReportType type);

    /**
     * Удаляет отчет декларации по типу
     *
     * @param declarationDataIds идентификаторы декларации
     * @param reportTypes        типы отчета
     */
    void deleteDec(Collection<Long> declarationDataIds, List<DeclarationDataReportType> reportTypes);

    /**
     * Удаление отчета по uuid
     *
     * @param uuid идентификатор блоба
     */
    void deleteDec(String uuid);

    /**
     * Удаление отчетов декларации по id, кроме {@link DeclarationDataReportType#XML_DEC}
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