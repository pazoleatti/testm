package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentType is a Querydsl query type for QDepartmentType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentType extends com.querydsl.sql.RelationalPathBase<QDepartmentType> {

    private static final long serialVersionUID = -511411656;

    public static final QDepartmentType departmentType = new QDepartmentType("DEPARTMENT_TYPE");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QDepartmentType> departmentTypePk = createPrimaryKey(id);

    public QDepartmentType(String variable) {
        super(QDepartmentType.class, forVariable(variable), "NDFL_UNSTABLE", "DEPARTMENT_TYPE");
        addMetadata();
    }

    public QDepartmentType(String variable, String schema, String table) {
        super(QDepartmentType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentType(String variable, String schema) {
        super(QDepartmentType.class, forVariable(variable), schema, "DEPARTMENT_TYPE");
        addMetadata();
    }

    public QDepartmentType(Path<? extends QDepartmentType> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DEPARTMENT_TYPE");
        addMetadata();
    }

    public QDepartmentType(PathMetadata metadata) {
        super(QDepartmentType.class, metadata, "NDFL_UNSTABLE", "DEPARTMENT_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(50));
    }

}

