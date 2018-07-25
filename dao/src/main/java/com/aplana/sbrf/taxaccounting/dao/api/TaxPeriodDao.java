package com.aplana.sbrf.taxaccounting.dao.api;

import java.util.List;

import com.aplana.sbrf.taxaccounting.model.TaxPeriod;
import com.aplana.sbrf.taxaccounting.model.TaxType;

/**
 * DAO-интерфейс для работы с налоговыми периодами {@link TaxPeriod}
 *
 * @author dsultanbekov
 */
public interface TaxPeriodDao {
    /**
     * Получить объект налогового периода по идентификатору периода
     *
     * @param taxPeriodId идентификатор налогового периода
     * @return объект, задаваемый идентификатором
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если периода с заданным идентификатором не существует
     */
    TaxPeriod fetchOne(int taxPeriodId);

    /**
     * Получить список всех налоговых периодов по заданному виду налога.
     * Список будет отсортирован по убыванию даты начала периода
     *
     * @param taxType вид налога
     * @return список {@link TaxPeriod}
     * @throws com.aplana.sbrf.taxaccounting.model.exception.DaoException если налоговые периоды не найдены
     */
    List<TaxPeriod> fetchAllByTaxType(TaxType taxType);

    /**
     * Получить налоговый период по году
     *
     * @param year год
     * @return объект {@link TaxPeriod} или null
     */
    TaxPeriod fetchOneByYear(int year);

    /**
     * Добавить новый налоговый период
     *
     * @param taxPeriod налоговый период
     * @return идентификатор нового налогового периода
     */
    int create(TaxPeriod taxPeriod);

    /**
     * Получить последний налоговый период по виду налога
     *
     * @param taxType вид налога
     * @return объект {@link TaxPeriod} или null
     */
    TaxPeriod getLast(TaxType taxType);

    /**
     * Удалить налоговый период
     *
     * @param taxPeriodId идентификатор налогового периода
     */
    void delete(int taxPeriodId);
}
