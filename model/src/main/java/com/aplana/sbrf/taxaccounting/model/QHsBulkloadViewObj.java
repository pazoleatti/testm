package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QHsBulkloadViewObj is a Querydsl query type for QHsBulkloadViewObj
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QHsBulkloadViewObj extends com.querydsl.sql.RelationalPathBase<QHsBulkloadViewObj> {

    private static final long serialVersionUID = -1109888014;

    public static final QHsBulkloadViewObj hsBulkloadViewObj = new QHsBulkloadViewObj("HS_BULKLOAD_VIEW_OBJ");

    public final StringPath schemaName = createString("schemaName");

    public final NumberPath<java.math.BigInteger> tempObjId = createNumber("tempObjId", java.math.BigInteger.class);

    public final StringPath viewName = createString("viewName");

    public QHsBulkloadViewObj(String variable) {
        super(QHsBulkloadViewObj.class, forVariable(variable), "SYS", "HS_BULKLOAD_VIEW_OBJ");
        addMetadata();
    }

    public QHsBulkloadViewObj(String variable, String schema, String table) {
        super(QHsBulkloadViewObj.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHsBulkloadViewObj(Path<? extends QHsBulkloadViewObj> path) {
        super(path.getType(), path.getMetadata(), "SYS", "HS_BULKLOAD_VIEW_OBJ");
        addMetadata();
    }

    public QHsBulkloadViewObj(PathMetadata metadata) {
        super(QHsBulkloadViewObj.class, metadata, "SYS", "HS_BULKLOAD_VIEW_OBJ");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(schemaName, ColumnMetadata.named("SCHEMA_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(tempObjId, ColumnMetadata.named("TEMP_OBJ_ID").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(viewName, ColumnMetadata.named("VIEW_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(30));
    }

}

