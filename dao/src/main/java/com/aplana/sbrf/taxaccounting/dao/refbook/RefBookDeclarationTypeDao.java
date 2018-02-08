package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;

import java.util.Date;
import java.util.List;

/**
 * Дао для работы со справочником Виды форм
 */
public interface RefBookDeclarationTypeDao {
    /**
     * Получение всех значений справочника
     *
     * @return список значений справочника
     */
    List<RefBookDeclarationType> fetchAll();

    /**
     * Получение значений справочника на основе типа формы, подразделения и начала отчетного периода. Выполняется поиск
     * назначенных подразделению видов форм с действующей на момент начала периода версией шаблона формы указанного типа.
     * Т.е. видов форм, назначенных заданному подразделению, имеющих статус версии "действующий" и для которых есть шаблон
     * формы с заданным типом формы, "действующим" статусом версии и версией не более поздней, чем заданное начало
     * отчетного периода
     *
     * @param declarationKind тип налоговой формы
     * @param departmentId    id подразделения
     * @param periodStartDate начало отчетного периода
     * @return список значений справочника
     */
    List<RefBookDeclarationType> fetchDeclarationTypes(Long declarationKind, Integer departmentId, Date periodStartDate);
}
