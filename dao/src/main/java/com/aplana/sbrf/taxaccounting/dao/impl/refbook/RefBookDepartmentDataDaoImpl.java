package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.PagingParams;
import com.aplana.sbrf.taxaccounting.model.PagingResult;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.aplana.sbrf.taxaccounting.model.util.QueryDSLOrderingUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.querydsl.QDepartment.department;
import static com.aplana.sbrf.taxaccounting.model.querydsl.QDepartmentFullpath.departmentFullpath;
import static com.querydsl.core.types.Projections.bean;

/**
 * Реализация дао для работы со справочником Подразделения
 */
@Repository
public class RefBookDepartmentDataDaoImpl implements RefBookDepartmentDataDao {

    final private SQLQueryFactory sqlQueryFactory;

    public RefBookDepartmentDataDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<RefBookDepartment> refBookDepartmentBean = bean(RefBookDepartment.class, department.id, department.name, department.shortname.as("shortName"), department.parentId,
            department.type, department.tbIndex, department.sbrfCode, department.regionId, department.isActive, department.code, departmentFullpath.shortname.as("fullPath"));

    /**
     * Получение значения справочника по идентификатору
     *
     * @param id Идентификатор подразделения
     * @return Значение справочника
     */
    @Override
    public RefBookDepartment fetchDepartmentById(Integer id) {
        return sqlQueryFactory
                .select(refBookDepartmentBean)
                .from(department)
                .innerJoin(departmentFullpath).on(department.id.eq(departmentFullpath.id))
                .where(department.isActive.eq((byte) 1).and(department.id.eq(id)))
                .fetchFirst();
    }

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids Список идентификаторов
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDepartment> fetchDepartments(Collection<Integer> ids) {
        return fetchDepartments(ids, false);
    }

    /**
     * Получение действующих значений справочника по идентификаторам
     *
     * @param ids Список идентификаторов
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDepartment> fetchActiveDepartments(Collection<Integer> ids) {
        return fetchDepartments(ids, true);
    }

    /**
     * Получение значений справочника по идентификаторам
     *
     * @param ids        Список идентификаторов
     * @param activeOnly Искать только действующие подразделения
     * @return Список значений справочника
     */
    private List<RefBookDepartment> fetchDepartments(Collection<Integer> ids, boolean activeOnly) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(department.isActive.eq((byte) 1).and(department.id.in(ids)));
        return sqlQueryFactory
                .select(refBookDepartmentBean)
                .from(department)
                .innerJoin(departmentFullpath).on(department.id.eq(departmentFullpath.id))
                .where(where)
                .fetch();
    }

    /**
     * Получение значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param ids          Список идентификаторов
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @Override
    public PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, PagingParams pagingParams) {
        return fetchDepartments(ids, name, false, pagingParams);
    }

    /**
     * Получение действующих значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     *
     * @param ids          Список идентификаторов
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    @Override
    public PagingResult<RefBookDepartment> fetchActiveDepartments(Collection<Integer> ids, String name, PagingParams pagingParams) {
        return fetchDepartments(ids, name, true, pagingParams);
    }

    /**
     * Получение значений справочника по идентификаторам с фильтрацией по наименованию подразделения и пейджингом
     * Также можно выбрать все подразделения или только действующие
     *
     * @param ids          Список идентификаторов
     * @param name         Параметр фильтрации по наименованию подразделения, может содержаться в любой части полного
     *                     наименования или в любой части полного пути до подразделения, состоящего из кратких наименований
     * @param activeOnly   Искать только действующие подразделения
     * @param pagingParams Параметры пейджинга
     * @return Страница списка значений справочника
     */
    private PagingResult<RefBookDepartment> fetchDepartments(Collection<Integer> ids, String name, boolean activeOnly, PagingParams pagingParams) {
        BooleanBuilder where = new BooleanBuilder();
        where.and(department.id.in(ids));

        if (!StringUtils.isBlank(name)) {
            where = where.and(department.name.containsIgnoreCase(name).or(departmentFullpath.shortname.containsIgnoreCase(name)));
        }

        if (activeOnly) {
            where = where.and(department.isActive.eq((byte) 1));
        }

        SQLQuery<RefBookDepartment> queryBase = sqlQueryFactory
                .select(refBookDepartmentBean)
                .from(department)
                .innerJoin(departmentFullpath).on(department.id.eq(departmentFullpath.id))
                .where(where);

        if (StringUtils.isNotBlank(pagingParams.getProperty()) && StringUtils.isNotBlank(pagingParams.getDirection())) {
            String orderingProperty = pagingParams.getProperty();
            Order ascDescOrder = Order.valueOf(pagingParams.getDirection().toUpperCase());
            OrderSpecifier ordering = QueryDSLOrderingUtils.getOrderSpecifierByPropertyAndOrder(refBookDepartmentBean, orderingProperty, ascDescOrder);
            if (ordering != null) {
                queryBase = queryBase.orderBy(ordering);
            }
        }

        long totalCount = queryBase.fetchCount();

        List<RefBookDepartment> departments = queryBase
                .limit(pagingParams.getCount())
                .offset(pagingParams.getStartIndex())
                .fetch();

        return new PagingResult<>(departments, (int) totalCount);
    }
}
