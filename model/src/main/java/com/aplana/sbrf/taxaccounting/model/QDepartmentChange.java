package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDepartmentChange is a Querydsl query type for QDepartmentChange
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDepartmentChange extends com.querydsl.sql.RelationalPathBase<QDepartmentChange> {

    private static final long serialVersionUID = 780505279;

    public static final QDepartmentChange departmentChange = new QDepartmentChange("DEPARTMENT_CHANGE");

    public final NumberPath<Long> code = createNumber("code", Long.class);

    public final NumberPath<Integer> departmentId = createNumber("departmentId", Integer.class);

    public final NumberPath<Byte> garantUse = createNumber("garantUse", Byte.class);

    public final NumberPath<Integer> hierLevel = createNumber("hierLevel", Integer.class);

    public final NumberPath<Byte> isActive = createNumber("isActive", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> logDate = createDateTime("logDate", org.joda.time.LocalDateTime.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> operationtype = createNumber("operationtype", Integer.class);

    public final NumberPath<Integer> parentId = createNumber("parentId", Integer.class);

    public final StringPath region = createString("region");

    public final StringPath sbrfCode = createString("sbrfCode");

    public final StringPath shortname = createString("shortname");

    public final NumberPath<Byte> sunrUse = createNumber("sunrUse", Byte.class);

    public final StringPath tbIndex = createString("tbIndex");

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QDepartmentChange> depChangePk = createPrimaryKey(departmentId, logDate);

    public QDepartmentChange(String variable) {
        super(QDepartmentChange.class, forVariable(variable), "NDFL_UNSTABLE", "DEPARTMENT_CHANGE");
        addMetadata();
    }

    public QDepartmentChange(String variable, String schema, String table) {
        super(QDepartmentChange.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDepartmentChange(Path<? extends QDepartmentChange> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "DEPARTMENT_CHANGE");
        addMetadata();
    }

    public QDepartmentChange(PathMetadata metadata) {
        super(QDepartmentChange.class, metadata, "NDFL_UNSTABLE", "DEPARTMENT_CHANGE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(13).ofType(Types.DECIMAL).withSize(15));
        addMetadata(departmentId, ColumnMetadata.named("DEPARTMENT_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(garantUse, ColumnMetadata.named("GARANT_USE").withIndex(14).ofType(Types.DECIMAL).withSize(1));
        addMetadata(hierLevel, ColumnMetadata.named("HIER_LEVEL").withIndex(4).ofType(Types.DECIMAL).withSize(9));
        addMetadata(isActive, ColumnMetadata.named("IS_ACTIVE").withIndex(12).ofType(Types.DECIMAL).withSize(1));
        addMetadata(logDate, ColumnMetadata.named("LOG_DATE").withIndex(2).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(5).ofType(Types.VARCHAR).withSize(510));
        addMetadata(operationtype, ColumnMetadata.named("OPERATIONTYPE").withIndex(3).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(parentId, ColumnMetadata.named("PARENT_ID").withIndex(6).ofType(Types.DECIMAL).withSize(9));
        addMetadata(region, ColumnMetadata.named("REGION").withIndex(11).ofType(Types.VARCHAR).withSize(510));
        addMetadata(sbrfCode, ColumnMetadata.named("SBRF_CODE").withIndex(10).ofType(Types.VARCHAR).withSize(255));
        addMetadata(shortname, ColumnMetadata.named("SHORTNAME").withIndex(8).ofType(Types.VARCHAR).withSize(510));
        addMetadata(sunrUse, ColumnMetadata.named("SUNR_USE").withIndex(15).ofType(Types.DECIMAL).withSize(1));
        addMetadata(tbIndex, ColumnMetadata.named("TB_INDEX").withIndex(9).ofType(Types.VARCHAR).withSize(3));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(7).ofType(Types.DECIMAL).withSize(9));
    }

}

