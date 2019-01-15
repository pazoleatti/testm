package com.aplana.sbrf.taxaccounting.dao;


import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPairFilter;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Date;
import java.util.List;

/**
 * Дао для работы с настройками подразделений
 */
public interface DepartmentConfigDao extends PermissionDao {

    /**
     * Возвращяет пары КПП и ОКТМО из настроек подразделений по определенным подразделениям
     *
     * @param departmentIds список идентификаторов подразделений
     * @param relevanceDate дата актуальности настройки
     * @return список пар КПП и ОКТМО
     */
    List<Pair<String, String>> fetchKppOktmoPairs(List<Integer> departmentIds, Date relevanceDate);

    /**
     * Возвращяет страницу из значений КПП тербанка по фильтру
     *
     * @param departmentId тербанк, из настроек которого будут браться КПП
     * @param kpp          фильтр поиска
     * @param pagingParams данные пагинатора
     * @return страница из значений КПП тербанка
     */
    PagingResult<KppSelect> findAllKppByDepartmentIdAndKpp(int departmentId, String kpp, PagingParams pagingParams);

    /**
     * Возвращяет пары КПП/ОКТМО для формы создания отчетности
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации и сортировки
     * @return страница пар КПП/ОКТМО
     */
    PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter filter, PagingParams pagingParams);
}
