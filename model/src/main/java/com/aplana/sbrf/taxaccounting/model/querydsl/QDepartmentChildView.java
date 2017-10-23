package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentChildView is a Querydsl query type for QDepartmentChildView
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentChildView extends com.querydsl.sql.RelationalPathBase<QDepartmentChildView> {

    private static final long serialVersionUID = -526911069;

    public static final QDepartmentChildView departmentChildView = new QDepartmentChildView("DEPARTMENT_CHILD_VIEW");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final NumberPath<Integer> parentId = createNumber("parentId", Integer.class);

    public final StringPath viewRowid = createString("viewRowid");

    public QDepartmentChildView(String variable) {
        super(QDepartmentChildView.class, forVariable(variable), "NDFL_UNSTABLE", "DEPARTMENT_CHILD_VIEW");
        addMetadata();
    }

    public QDepartmentChildView(String variable, String schema, String table) {
        super(QDepartmentChildView.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentChildView(String variable, String schema) {
        super(QDepartmentChildView.class, forVariable(variable), schema, "DEPARTMENT_CHILD_VIEW");
        addMetadata();
    }

    public QDepartmentChildView(Path<? extends QDepartmentChildView> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DEPARTMENT_CHILD_VIEW");
        addMetadata();
    }

    public QDepartmentChildView(PathMetadata metadata) {
        super(QDepartmentChildView.class, metadata, "NDFL_UNSTABLE", "DEPARTMENT_CHILD_VIEW");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9));
        addMetadata(parentId, ColumnMetadata.named("PARENT_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9));
        addMetadata(viewRowid, ColumnMetadata.named("VIEW_ROWID").withIndex(3).ofType(Types.VARCHAR).withSize(81));
    }

}

