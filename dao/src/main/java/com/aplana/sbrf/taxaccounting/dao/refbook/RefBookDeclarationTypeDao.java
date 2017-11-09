package com.aplana.sbrf.taxaccounting.dao.refbook;

import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import org.joda.time.LocalDateTime;

import java.util.List;

/**
 * Дао для работы со справочником Виды форм
 */
public interface RefBookDeclarationTypeDao {
    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    List<RefBookDeclarationType> fetchAll();

    /**
     * Получение значений справочника на основе типа формы, подразделения и начала отчетного периода. Выполняется поиск
     * назначенных подразделению видов форм с действующей на момент начала периода версией шаблона формы указанного типа.
     * Т.е. видов форм, назначенных заданному подразделению, имеющих статус версии "действующий" и для которых есть шаблон
     * формы с заданным типом формы, "действующим" статусом версии и версией не более поздней, чем заданное начало
     * отчетного периода
     *
     * @param declarationKind Тип налоговой формы
     * @param departmentId    ID подразделения
     * @param periodStartDate Начало отчетного периода
     * @return Список значений справочника
     */
    List<RefBookDeclarationType> fetchDeclarationTypes(Long declarationKind, Integer departmentId, LocalDateTime periodStartDate);
}
