package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDeclarationTypeDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentDeclarationType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDeclarationType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.QBean;
import com.querydsl.sql.SQLExpressions;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.QDeclarationTemplate.declarationTemplate;
import static com.aplana.sbrf.taxaccounting.model.QDeclarationType.declarationType;
import static com.aplana.sbrf.taxaccounting.model.QDepartmentDeclarationType.departmentDeclarationType;
import static com.querydsl.core.types.Projections.bean;

/**
 * Реализация дао для работы со справочником Виды форм
 */
@Repository
public class RefBookDeclarationTypeDaoImpl implements RefBookDeclarationTypeDao {
    final private SQLQueryFactory sqlQueryFactory;

    public RefBookDeclarationTypeDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    final private QBean<RefBookDeclarationType> refBookDeclarationTypeBean = bean(RefBookDeclarationType.class, declarationType.id,
            declarationType.ifrsName, declarationType.isIfrs, declarationType.name, declarationType.status.as("versionStatusId"));

    /**
     * Получение всех значений справочника
     *
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDeclarationType> fetchAll() {
        BooleanBuilder where = new BooleanBuilder();
        where.and(declarationType.status.eq((byte) 0));
        return sqlQueryFactory
                .select(refBookDeclarationTypeBean)
                .from(declarationType)
                .where(where)
                .fetch();
    }

    /**
     * Получение значений справочника для создания налоговой формы
     *
     * @param declarationKind Вид налоговой формы
     * @param departmentId    ID подразделения
     * @param periodStartDate Начало отчетного периода
     * @return Список значений справочника
     */
    @Override
    public List<RefBookDeclarationType> fetchDeclarationTypesForCreate(Long declarationKind, Integer departmentId, Date periodStartDate) {
        BooleanBuilder subqueryWhere = new BooleanBuilder();
        subqueryWhere.and(declarationTemplate.status.eq((byte) 0));
        subqueryWhere.and(declarationTemplate.formKind.eq(declarationKind));
        subqueryWhere.andNot(SQLExpressions.date(declarationTemplate.version).after(LocalDateTime.fromCalendarFields(DateUtils.toCalendar(periodStartDate))));

        SQLQuery<Integer> declarationTypesWithTemplates = sqlQueryFactory
                .select(declarationTemplate.declarationTypeId)
                .distinct()
                .from(declarationTemplate)
                .where(subqueryWhere);

        BooleanBuilder where = new BooleanBuilder();
        where.and(declarationType.status.eq((byte) 0));
        where.and(declarationType.id.in(declarationTypesWithTemplates));
        where.and(departmentDeclarationType.departmentId.eq(departmentId));

        List<RefBookDeclarationType> declarationTypes = sqlQueryFactory
                .select(refBookDeclarationTypeBean)
                .from(declarationType)
                .join(departmentDeclarationType).on(declarationType.id.eq(departmentDeclarationType.declarationTypeId))
                .where(where)
                .fetch();

        return declarationTypes;
    }
}
