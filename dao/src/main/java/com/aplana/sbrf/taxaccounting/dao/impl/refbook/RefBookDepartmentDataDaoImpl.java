package com.aplana.sbrf.taxaccounting.dao.impl.refbook;

import com.aplana.sbrf.taxaccounting.dao.refbook.RefBookDepartmentDataDao;
import com.aplana.sbrf.taxaccounting.model.DepartmentType;
import com.aplana.sbrf.taxaccounting.model.refbook.RefBookDepartment;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.QBean;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.sql.SQLQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

import static com.aplana.sbrf.taxaccounting.model.QDepartment.department;
import static com.querydsl.core.types.Projections.bean;

/**
 * Created by aokunev on 14.08.2017.
 */
@Repository
public class RefBookDepartmentDataDaoImpl implements RefBookDepartmentDataDao {

    final private SQLQueryFactory sqlQueryFactory;

    public RefBookDepartmentDataDaoImpl(SQLQueryFactory sqlQueryFactory) {
        this.sqlQueryFactory = sqlQueryFactory;
    }

    /*final private Expression<DepartmentType> departmentTypeExpression = new CaseBuilder()
            .when(department.type.eq(1)).then(DepartmentType.ROOT_BANK)
            .when(department.type.eq(2)).then(DepartmentType.TERR_BANK)
            .when(department.type.eq(3)).then(DepartmentType.CSKO_PCP)
            .when(department.type.eq(4)).then(DepartmentType.MANAGEMENT)
            .otherwise(DepartmentType.INTERNAL)
            .as("type");*/

    final private Expression<DepartmentType> departmentTypeExpression = new CaseBuilder()
            .when(new BooleanBuilder()).then(DepartmentType.ROOT_BANK)
            .otherwise(DepartmentType.INTERNAL)
            .as("type");

    final private QBean<RefBookDepartment> refBookDepartmentBean = bean(RefBookDepartment.class, department.id, department.name, department.shortname, department.parentId,
            departmentTypeExpression,
            department.tbIndex, department.sbrfCode, department.regionId, department.isActive, department.code);

    @Override
    public List<RefBookDepartment> fetchDepartments() {
        BooleanBuilder where = new BooleanBuilder();
        where.and(department.isActive.eq((byte) 1));
        return sqlQueryFactory
                .select(refBookDepartmentBean)
                .from(department)
                .where(where)
                .fetch();
    }
}
