package com.aplana.sbrf.taxaccounting.web.module.departmentconfig.client;

import com.aplana.sbrf.taxaccounting.model.TaxType;
import com.aplana.sbrf.taxaccounting.web.module.departmentconfig.shared.DepartmentCombined;
import com.gwtplatform.mvp.client.UiHandlers;

import java.util.Date;

/**
 * Хендлеры формы настройки подразделений
 *
 * @author Dmitriy Levykin
 */
public interface DepartmentConfigUiHandlers extends UiHandlers {
    /**
     * Сохранение
     *
     * @param combinedDepartmentParam
     * @param period
     * @param department
    */

    void save(DepartmentCombined combinedDepartmentParam, Integer period, Integer department);

    /**
     * Удаление
     *
     * @param combinedDepartmentParam
     * @param period
     * @param department
     */
    void delete(DepartmentCombined combinedDepartmentParam, Integer period, Integer department);

    /**
     * Проверка перед редакритованием
     *
     * @param period
     * @param department
     */
    void edit(Integer period, Integer department);

    /**
     * Очистка формы
     */
    void clear();

    /**
     * Перезагрузка параметров подразделений на форме
     *
     * @param departmentId
     * @param taxType
     * @param reportPeriodId
     */
    void reloadDepartmentParams(Integer departmentId, TaxType taxType, Integer reportPeriodId, String uuid);

    /**
     * Перезагрузка дерева подразделений
     *
     * @param taxType
     * @param currentDepartmentId
     */
    void reloadDepartments(TaxType taxType, Integer currentDepartmentId);

    /**
     * Перезагрузка дерева периодов в зависимости от выбранного подразделения
     * @param taxType
     */
    void reloadPeriods(TaxType taxType);

    /**
     * Проверка конролера УНП
     * @return
     */
    boolean isControlUnp();

    void getRefBookPeriod(Integer currentReportPeriodId, Integer currentDepartmentId);
}
