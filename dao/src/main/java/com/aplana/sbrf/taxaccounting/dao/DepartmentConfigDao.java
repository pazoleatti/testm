package com.aplana.sbrf.taxaccounting.dao;


import com.aplana.sbrf.taxaccounting.model.DeclarationData;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.KppOktmoPairFilter;
import com.aplana.sbrf.taxaccounting.model.KppSelect;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.ReportFormCreationKppOktmoPair;
import com.aplana.sbrf.taxaccounting.model.action.DepartmentConfigsFilter;
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
     * Возвращяет список настроек подразделений по ид подразделения
     *
     * @param departmentId ид подразделения
     * @return список настроек подразделений
     */
    List<DepartmentConfig> findAllByDepartmentId(int departmentId);

    /**
     * Возвращяет пары КПП/ОКТМО формы, для которых существуют настройки подразделений по ТБ.
     * Настройки берутся только актуальные на текущую дату или которые пересекаются с периодом формы, но не переходят в другой ТБ в старших версиях
     *
     * @param declarationId  ид формы
     * @param departmentId   ид подразделений
     * @param reportPeriodId ид периода формы
     * @return пары КПП/ОКТМО
     */
    List<KppOktmoPair> findAllKppOKtmoPairs(long declarationId, Integer departmentId, int reportPeriodId, Date relevanceDate);

    /**
     * Возвращяет страницу настроек подразделений для отображения в GUI
     *
     * @param filter       объект содержащих данные используемые для фильтрации
     * @param pagingParams параметры пагиинации
     * @return список объектов содержащих данные о настройках подразделений
     */
    PagingResult<DepartmentConfig> findAllByFilter(DepartmentConfigsFilter filter, PagingParams pagingParams);

    /**
     * Возвращяет кол-во записей настроек подразделений по фильтру
     *
     * @param filter фильтр
     * @return кол-во записей
     */
    int countByFilter(DepartmentConfigsFilter filter);

    /**
     * Возвращяет предыдущую версию настройки подразделений
     *
     * @param id ид настройки подразделений
     * @return настройка подразделений
     */
    DepartmentConfig findPrevById(long id);

    /**
     * Возвращяет следующую версию настройки подразделений
     *
     * @param id ид настройки подразделений
     * @return настройка подразделений
     */
    DepartmentConfig findNextById(long id);

    /**
     * Возвращяет список настроек подразделений по КПП/ОКТМО
     *
     * @param kpp   КПП
     * @param oktmo код ОКТМО
     * @return список настроек подразделений
     */
    List<DepartmentConfig> findAllByKppAndOktmo(String kpp, String oktmo);

    /**
     * Возвращяет пары КПП и ОКТМО из настроек подразделений по определенным подразделениям
     *
     * @param departmentIds список идентификаторов подразделений
     * @param relevanceDate дата актуальности настройки
     * @return список пар КПП и ОКТМО
     */
    List<Pair<String, String>> findAllKppOktmoPairsByDepartmentIdIn(List<Integer> departmentIds, Date relevanceDate);

    /**
     * Возвращяет страницу из значений КПП тербанка по фильтру
     *
     * @param departmentId тербанк, из настроек которого будут браться КПП
     * @param kpp          фильтр поиска
     * @param pagingParams данные пагинатора
     * @return страница из значений КПП тербанка
     */
    PagingResult<KppSelect> findAllKppByDepartmentIdAndKppContaining(int departmentId, String kpp, PagingParams pagingParams);

    /**
     * Возвращяет все пары КПП/ОКТМО из формы и настройки подразделений и связывает их по КПП/ОКТМО.
     * Настройки берутся только актуальные на текущую дату или которые пересекаются с периодом формы, но не переходят в другой ТБ в старших версиях
     *
     * @param declaration   НФ
     * @param relevanceDate дата актуальности
     * @return список пар КПП/ОКТМО из формы и связанные с ними настройки подразделений
     */
    List<Pair<KppOktmoPair, DepartmentConfig>> findAllByDeclaration(DeclarationData declaration, Date relevanceDate);

    /**
     * Если форма задана, то возвращяет все пары КПП/ОКТМО из раздела 2 формы с дополнительной информацией о связанных настройках подразделений.
     * Иначе возвращяет только пары КПП/ОКТМО из настроек подразделений.
     * Настройки берутся только актуальные на текущую дату или которые пересекаются с периодом формы, но не переходят в другой ТБ в старших версиях
     *
     * @param filter       фильтр
     * @param pagingParams параметры пагинации и сортировки
     * @return страница пар КПП/ОКТМО
     */
    PagingResult<ReportFormCreationKppOktmoPair> findAllKppOktmoPairsByFilter(KppOktmoPairFilter filter, PagingParams pagingParams);

    /**
     * Создаёт настройку подразделений
     *
     * @param departmentConfig настройка подразделений
     */
    void create(DepartmentConfig departmentConfig);

    /**
     * Изменяет настройку подразделений
     *
     * @param departmentConfig настройка подразделений
     */
    void update(DepartmentConfig departmentConfig);

    /**
     * Удаляет настройку подразделений по идентификатору
     *
     * @param id идентификатор настройки подразделений
     */
    void deleteById(long id);

    /**
     * Удаляет настройки указанного подразделения
     *
     * @param departmentId ид подразделения
     */
    void deleteByDepartmentId(int departmentId);

    /**
     * Изменяет дату начала действия настройки подразделения
     *
     * @param id   ид настройки подразделения
     * @param date дата начала действия
     */
    void updateStartDate(long id, Date date);

    /**
     * Изменяет дату окончания действия настройки подразделения
     *
     * @param id   ид настройки подразделения
     * @param date дата окончания действия
     */
    void updateEndDate(long id, Date date);
}
