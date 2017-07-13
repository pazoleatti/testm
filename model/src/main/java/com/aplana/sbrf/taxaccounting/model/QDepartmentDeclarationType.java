package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentDeclarationType is a Querydsl query type for QDepartmentDeclarationType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentDeclarationType extends com.querydsl.sql.RelationalPathBase<QDepartmentDeclarationType> {

    private static final long serialVersionUID = 198176677;

    public static final QDepartmentDeclarationType departmentDeclarationType = new QDepartmentDeclarationType("DEPARTMENT_DECLARATION_TYPE");

    public final NumberPath<Integer> declarationTypeId = createNumber("declarationTypeId", Integer.class);

    public final NumberPath<Integer> departmentId = createNumber("departmentId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QDepartmentDeclarationType> deptDeclTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDepartment> deptDeclTypeFkDept = createForeignKey(departmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationType> deptDeclTypeFkDeclType = createForeignKey(declarationTypeId, "ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentDeclTypePerformer> _deptDeclTypePerfIdFk = createInvForeignKey(id, "DEPARTMENT_DECL_TYPE_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationSource> _declSourceFkDeptDecltype = createInvForeignKey(id, "DEPARTMENT_DECLARATION_TYPE_ID");

    public QDepartmentDeclarationType(String variable) {
        super(QDepartmentDeclarationType.class, forVariable(variable), "NDFL_UNSTABLE", "DEPARTMENT_DECLARATION_TYPE");
        addMetadata();
    }

    public QDepartmentDeclarationType(String variable, String schema, String table) {
        super(QDepartmentDeclarationType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentDeclarationType(Path<? extends QDepartmentDeclarationType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DEPARTMENT_DECLARATION_TYPE");
        addMetadata();
    }

    public QDepartmentDeclarationType(PathMetadata metadata) {
        super(QDepartmentDeclarationType.class, metadata, "NDFL_UNSTABLE", "DEPARTMENT_DECLARATION_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(declarationTypeId, ColumnMetadata.named("DECLARATION_TYPE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(departmentId, ColumnMetadata.named("DEPARTMENT_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

