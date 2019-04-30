package com.aplana.sbrf.taxaccounting.dao;

import com.aplana.sbrf.taxaccounting.model.DeclarationReportType;

import java.util.Collection;
import java.util.List;

/**
 * DAO-Интерфейс для работы с таблицей отчетов
 */
public interface ReportDao {
    /**
     * Создание записи об отчете декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param blobDataId        идентификатор блоба
     * @param type              тип отчета
     */
    void createDec(long declarationDataId, String blobDataId, DeclarationReportType type);

    /**
     * Получение записи об отчете декларации
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     * @return uuid идентификатор блоба
     */
    String getDec(long declarationDataId, DeclarationReportType type);

    /**
     * Удаление всех отчетов по id декларации
     *
     * @param declarationDataId идентификатор декларации
     */
    void deleteAllByDeclarationId(long declarationDataId);

    /**
     * Удаляет отчет декларации по типу
     *
     * @param declarationDataIds идентификаторы деклараций
     */
    void deleteDec(Collection<Long> declarationDataIds);

    /**
     * Удаляет отчет декларации по типу
     *
     * @param declarationDataId идентификатор декларации
     * @param type              тип отчета
     */
    void deleteDec(long declarationDataId, DeclarationReportType type);

    /**
     * Удаляет спецотчет налоговой формы
     *
     * @param declarationDataId идентификатор декларации
     * @param subreportAlias    альяс спецотчета
     */
    void deleteSubreport(long declarationDataId, String subreportAlias);

    /**
     * Удаляет отчеты деклараций по типам
     *
     * @param declarationDataIds идентификаторы декларации
     * @param ddReportTypes      типы отчета
     */
    void deleteDec(Collection<Long> declarationDataIds, List<DeclarationReportType> ddReportTypes);

    /**
     * Удаление отчета по uuid
     *
     * @param uuid идентификатор блоба
     */
    void deleteDec(String uuid);

    /**
     * Удаление ненужных/устаревших отчетов
     *
     * @return Количество удаленных запсией
     */
    int clean();

    /**
     * Удаление отчетов деклараций по id, кроме {@link DeclarationReportType#XML_DEC}
     *
     * @param declarationDataId идентификатор декларации
     */
    void deleteNotXmlDec(long declarationDataId);
}
