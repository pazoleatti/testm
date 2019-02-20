package com.aplana.sbrf.taxaccounting.script.service;

import com.aplana.sbrf.taxaccounting.model.DepartmentReportPeriod;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.ReportPeriod;
import com.aplana.sbrf.taxaccounting.model.util.DepartmentReportPeriodFilter;
import com.aplana.sbrf.taxaccounting.service.ScriptExposed;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 */
@ScriptExposed
public interface DepartmentReportPeriodService {

    /**
     * Получение объекта {@link DepartmentReportPeriod} по идентификатору
     *
     * @param id идентификатор
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod get(int id);

    /**
     * Получение первого некорректирующего отчетного периода подразделения по идентификатору подразделения
     * и идентификатору отчетного периода
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod getFirst(int departmentId, int reportPeriodId);

    /**
     * Получение последнего отчетного периода подразделения
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod getLast(int departmentId, int reportPeriodId);

    /**
     * Получение предпоследнего отчетного периода подразделения по идентификатору подразделения
     * и идентификатору отчетного периода
     * Если предпоследний отчетный период не является корректировочным возвращается null
     *
     * @param departmentId   идентификатор подразделения
     * @param reportPeriodId идентификатор отчетного периода
     * @return объект {@link DepartmentReportPeriod} или null
     */
    DepartmentReportPeriod getPrevLast(int departmentId, int reportPeriodId);

    /**
     * Получение идентификаторов некорректирующих отчетных периодов подразделений по типу подразделения и
     * активному отчетному периоду
     *
     * @param departmentTypeCode       {@link DepartmentType} тип подразделения
     * @param departmentReportPeriodId идентификатор отчетного периода подразделения, по {@link ReportPeriod} которого
     *                                 производится поиск
     * @return список идентификаторов или пустой список
     */
    List<Integer> getIdsByDepartmentTypeAndReportPeriod(int departmentTypeCode, int departmentReportPeriodId);

    /**
     * Формирует название периода с учетом того, является ли период корректировочным
     * @param departmentReportPeriod    период для которого формируется название
     * @param formatExp	                шаблон форматирования даты
     * @return строка с сообщением о корректировочномм приоде, если период не корректировочный возвращается пустая
     * строка
     */
    String formatPeriodName(DepartmentReportPeriod departmentReportPeriod, String formatExp);
}
