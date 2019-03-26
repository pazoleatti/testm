package com.aplana.sbrf.taxaccounting.dao;


import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPairFilter;
import com.aplana.sbrf.taxaccounting.model.refbook.DepartmentConfig;
import com.aplana.sbrf.taxaccounting.model.util.Pair;

import java.util.Date;
import java.util.List;

/**
 * Дао для работы с настройками подразделений
 */
public interface DepartmentConfigDao extends PermissionDao {

    /**
     * Возвращяет настройку подразделений по идентификатору, null если не найдена
     *
     * @param id идентификатор версии настройки подразделений
     * @return настройка подразделений
     */
    DepartmentConfig findById(long id);

    /**
     * Возвращяет предыдущую версию настройки подразделений
     *
     * @param departmentConfig настройка подразделений
     * @return настройка подразделений
     */
    DepartmentConfig findPrev(DepartmentConfig departmentConfig);

    /**
     * Возвращяет пары КПП и ОКТМО из настроек подразделений по определенным подразделениям
     *
     * @param departmentIds список идентификаторов подразделений
     * @param relevanceDate дата актуальности настройки
     * @return список пар КПП и ОКТМО
     */
    List<Pair<String, String>> findKppOktmoPairs(List<Integer> departmentIds, Date relevanceDate);

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
     * Возвращяет настройку подразделений по КПП/ОКТМО и актуальную на дату
     *
     * @param kpp           КПП
     * @param oktmoCode     ОКТМО
     * @param relevanceDate дата актуальности
     * @return найстройка подразделений
     */
    DepartmentConfig findByKppAndOktmoAndDate(String kpp, String oktmoCode, Date relevanceDate);

    /**
     * Возвращяет все пары КПП/ОКТМО из формы и все настройки подразделений, которые актуальны на определенную дату или пересекаются с периодом формы,
     * соединеннные по КПП и ОКТМО через full join
     *
     * @param declaration   НФ
     * @param relevanceDate дата актуальности
     * @return список пар КПП/ОКТМО из формы и связанные с ними настройки подразделений
     */
    List<Pair<KppOktmoPair, DepartmentConfig>> findAllByDeclaration(DeclarationData declaration, Date relevanceDate);

    /**
     * Если форма задана, то возвращяет все пары КПП/ОКТМО из раздела 2 формы с дополнительной информацией о связанных настройках подразделений,
     * которые актуальны на определенную дату или пересекаются с заданным периодом.
     * Иначе возвращяет только пары КПП/ОКТМО из настроек подразделений
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации и сортировки
     * @return страница пар КПП/ОКТМО
     */
    PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(ReportFormCreationKppOktmoPairFilter filter, PagingParams pagingParams);

    /**
     * Возвращяет признак наличия настройки с заданными КПП и ОКТМО, актуальной на текущий момент или пересекающейся с периодов формы
     *
     * @param kpp            КПП
     * @param oktmo          ОКТМО
     * @param reportPeriodId идентификатор периода формы
     * @return признак наличия настройки подразделения
     */
    boolean existsByKppAndOkmtoAndPeriodId(String kpp, String oktmo, int reportPeriodId);
}
