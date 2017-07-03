package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentFormTypePerformer is a Querydsl query type for QDepartmentFormTypePerformer
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentFormTypePerformer extends com.querydsl.sql.RelationalPathBase<QDepartmentFormTypePerformer> {

    private static final long serialVersionUID = -655833375;

    public static final QDepartmentFormTypePerformer departmentFormTypePerformer = new QDepartmentFormTypePerformer("DEPARTMENT_FORM_TYPE_PERFORMER");

    public final NumberPath<Integer> departmentFormTypeId = createNumber("departmentFormTypeId", Integer.class);

    public final NumberPath<Integer> performerDepId = createNumber("performerDepId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QDepartmentFormTypePerformer> departmentFormTypePerfPk = createPrimaryKey(departmentFormTypeId, performerDepId);

    public final com.querydsl.sql.ForeignKey<QDepartmentFormType> deptFormTypePerfFkId = createForeignKey(departmentFormTypeId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartment> deptFormTypePerfFkPerf = createForeignKey(performerDepId, "ID");

    public QDepartmentFormTypePerformer(String variable) {
        super(QDepartmentFormTypePerformer.class, forVariable(variable), "NDFL_1_0", "DEPARTMENT_FORM_TYPE_PERFORMER");
        addMetadata();
    }

    public QDepartmentFormTypePerformer(String variable, String schema, String table) {
        super(QDepartmentFormTypePerformer.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentFormTypePerformer(Path<? extends QDepartmentFormTypePerformer> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "DEPARTMENT_FORM_TYPE_PERFORMER");
        addMetadata();
    }

    public QDepartmentFormTypePerformer(PathMetadata metadata) {
        super(QDepartmentFormTypePerformer.class, metadata, "NDFL_1_0", "DEPARTMENT_FORM_TYPE_PERFORMER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(departmentFormTypeId, ColumnMetadata.named("DEPARTMENT_FORM_TYPE_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(performerDepId, ColumnMetadata.named("PERFORMER_DEP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

