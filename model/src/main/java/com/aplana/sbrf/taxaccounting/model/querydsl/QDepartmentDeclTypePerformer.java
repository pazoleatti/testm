package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentDeclTypePerformer is a Querydsl query type for QDepartmentDeclTypePerformer
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentDeclTypePerformer extends com.querydsl.sql.RelationalPathBase<QDepartmentDeclTypePerformer> {

    private static final long serialVersionUID = -1046762772;

    public static final QDepartmentDeclTypePerformer departmentDeclTypePerformer = new QDepartmentDeclTypePerformer("DEPARTMENT_DECL_TYPE_PERFORMER");

    public final NumberPath<Integer> departmentDeclTypeId = createNumber("departmentDeclTypeId", Integer.class);

    public final NumberPath<Integer> performerDepId = createNumber("performerDepId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QDepartmentDeclTypePerformer> departmentDeclTypePerfPk = createPrimaryKey(departmentDeclTypeId, performerDepId);

    public final com.querydsl.sql.ForeignKey<QDepartment> deptDeclTypePerfPerfFk = createForeignKey(performerDepId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentDeclarationType> deptDeclTypePerfIdFk = createForeignKey(departmentDeclTypeId, "ID");

    public QDepartmentDeclTypePerformer(String variable) {
        super(QDepartmentDeclTypePerformer.class, forVariable(variable), "NDFL_UNSTABLE", "DEPARTMENT_DECL_TYPE_PERFORMER");
        addMetadata();
    }

    public QDepartmentDeclTypePerformer(String variable, String schema, String table) {
        super(QDepartmentDeclTypePerformer.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentDeclTypePerformer(String variable, String schema) {
        super(QDepartmentDeclTypePerformer.class, forVariable(variable), schema, "DEPARTMENT_DECL_TYPE_PERFORMER");
        addMetadata();
    }

    public QDepartmentDeclTypePerformer(Path<? extends QDepartmentDeclTypePerformer> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DEPARTMENT_DECL_TYPE_PERFORMER");
        addMetadata();
    }

    public QDepartmentDeclTypePerformer(PathMetadata metadata) {
        super(QDepartmentDeclTypePerformer.class, metadata, "NDFL_UNSTABLE", "DEPARTMENT_DECL_TYPE_PERFORMER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(departmentDeclTypeId, ColumnMetadata.named("DEPARTMENT_DECL_TYPE_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(performerDepId, ColumnMetadata.named("PERFORMER_DEP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

