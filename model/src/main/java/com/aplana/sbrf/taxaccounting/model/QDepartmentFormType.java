package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentFormType is a Querydsl query type for QDepartmentFormType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentFormType extends com.querydsl.sql.RelationalPathBase<QDepartmentFormType> {

    private static final long serialVersionUID = 1782547309;

    public static final QDepartmentFormType departmentFormType = new QDepartmentFormType("DEPARTMENT_FORM_TYPE");

    public final NumberPath<Integer> departmentId = createNumber("departmentId", Integer.class);

    public final NumberPath<Integer> formTypeId = createNumber("formTypeId", Integer.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> kind = createNumber("kind", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QDepartmentFormType> deptFormTypePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDepartment> deptFormTypeFkDepId = createForeignKey(departmentId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormType> deptFormTypeFkTypeId = createForeignKey(formTypeId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormKind> deptFormTypeFkKind = createForeignKey(kind, "ID");

    public final com.querydsl.sql.ForeignKey<QFormDataSource> _formDataSourceFkSrcDepId = createInvForeignKey(id, "SRC_DEPARTMENT_FORM_TYPE_ID");

    public final com.querydsl.sql.ForeignKey<QFormDataSource> _formDataSourceFkDepId = createInvForeignKey(id, "DEPARTMENT_FORM_TYPE_ID");

    public final com.querydsl.sql.ForeignKey<QDepartmentFormTypePerformer> _deptFormTypePerfFkId = createInvForeignKey(id, "DEPARTMENT_FORM_TYPE_ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationSource> _declSourceFkDeptFormtype = createInvForeignKey(id, "SRC_DEPARTMENT_FORM_TYPE_ID");

    public QDepartmentFormType(String variable) {
        super(QDepartmentFormType.class, forVariable(variable), "NDFL_1_0", "DEPARTMENT_FORM_TYPE");
        addMetadata();
    }

    public QDepartmentFormType(String variable, String schema, String table) {
        super(QDepartmentFormType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentFormType(Path<? extends QDepartmentFormType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "DEPARTMENT_FORM_TYPE");
        addMetadata();
    }

    public QDepartmentFormType(PathMetadata metadata) {
        super(QDepartmentFormType.class, metadata, "NDFL_1_0", "DEPARTMENT_FORM_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(departmentId, ColumnMetadata.named("DEPARTMENT_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(formTypeId, ColumnMetadata.named("FORM_TYPE_ID").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(kind, ColumnMetadata.named("KIND").withIndex(4).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

