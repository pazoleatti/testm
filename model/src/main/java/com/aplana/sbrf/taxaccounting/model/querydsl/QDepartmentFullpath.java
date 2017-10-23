package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentFullpath is a Querydsl query type for QDepartmentFullpath
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentFullpath extends com.querydsl.sql.RelationalPathBase<QDepartmentFullpath> {

    private static final long serialVersionUID = -1314462926;

    public static final QDepartmentFullpath departmentFullpath = new QDepartmentFullpath("DEPARTMENT_FULLPATH");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath shortname = createString("shortname");

    public QDepartmentFullpath(String variable) {
        super(QDepartmentFullpath.class, forVariable(variable), "NDFL_UNSTABLE", "DEPARTMENT_FULLPATH");
        addMetadata();
    }

    public QDepartmentFullpath(String variable, String schema, String table) {
        super(QDepartmentFullpath.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentFullpath(String variable, String schema) {
        super(QDepartmentFullpath.class, forVariable(variable), schema, "DEPARTMENT_FULLPATH");
        addMetadata();
    }

    public QDepartmentFullpath(Path<? extends QDepartmentFullpath> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DEPARTMENT_FULLPATH");
        addMetadata();
    }

    public QDepartmentFullpath(PathMetadata metadata) {
        super(QDepartmentFullpath.class, metadata, "NDFL_UNSTABLE", "DEPARTMENT_FULLPATH");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9));
        addMetadata(shortname, ColumnMetadata.named("SHORTNAME").withIndex(2).ofType(Types.VARCHAR).withSize(4000));
    }

}

