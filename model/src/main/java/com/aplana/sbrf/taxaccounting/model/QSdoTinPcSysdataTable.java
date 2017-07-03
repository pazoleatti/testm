package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoTinPcSysdataTable is a Querydsl query type for QSdoTinPcSysdataTable
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoTinPcSysdataTable extends com.querydsl.sql.RelationalPathBase<QSdoTinPcSysdataTable> {

    private static final long serialVersionUID = -1899377502;

    public static final QSdoTinPcSysdataTable sdoTinPcSysdataTable = new QSdoTinPcSysdataTable("SDO_TIN_PC_SYSDATA_TABLE");

    public final StringPath columnName = createString("columnName");

    public final StringPath depTableName = createString("depTableName");

    public final StringPath depTableSchema = createString("depTableSchema");

    public final StringPath sdoOwner = createString("sdoOwner");

    public final StringPath tableName = createString("tableName");

    public final com.querydsl.sql.PrimaryKey<QSdoTinPcSysdataTable> sdoPkTinPc = createPrimaryKey(depTableName, depTableSchema);

    public QSdoTinPcSysdataTable(String variable) {
        super(QSdoTinPcSysdataTable.class, forVariable(variable), "MDSYS", "SDO_TIN_PC_SYSDATA_TABLE");
        addMetadata();
    }

    public QSdoTinPcSysdataTable(String variable, String schema, String table) {
        super(QSdoTinPcSysdataTable.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoTinPcSysdataTable(Path<? extends QSdoTinPcSysdataTable> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_TIN_PC_SYSDATA_TABLE");
        addMetadata();
    }

    public QSdoTinPcSysdataTable(PathMetadata metadata) {
        super(QSdoTinPcSysdataTable.class, metadata, "MDSYS", "SDO_TIN_PC_SYSDATA_TABLE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(columnName, ColumnMetadata.named("COLUMN_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(1024).notNull());
        addMetadata(depTableName, ColumnMetadata.named("DEP_TABLE_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(depTableSchema, ColumnMetadata.named("DEP_TABLE_SCHEMA").withIndex(4).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(sdoOwner, ColumnMetadata.named("SDO_OWNER").withIndex(1).ofType(Types.VARCHAR).withSize(32).notNull());
        addMetadata(tableName, ColumnMetadata.named("TABLE_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(32).notNull());
    }

}

