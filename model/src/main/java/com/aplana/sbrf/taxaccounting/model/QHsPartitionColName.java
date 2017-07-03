package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QHsPartitionColName is a Querydsl query type for QHsPartitionColName
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QHsPartitionColName extends com.querydsl.sql.RelationalPathBase<QHsPartitionColName> {

    private static final long serialVersionUID = -1042475799;

    public static final QHsPartitionColName hsPartitionColName = new QHsPartitionColName("HS_PARTITION_COL_NAME");

    public QHsPartitionColName(String variable) {
        super(QHsPartitionColName.class, forVariable(variable), "SYS", "HS_PARTITION_COL_NAME");
        addMetadata();
    }

    public QHsPartitionColName(String variable, String schema, String table) {
        super(QHsPartitionColName.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHsPartitionColName(Path<? extends QHsPartitionColName> path) {
        super(path.getType(), path.getMetadata(), "SYS", "HS_PARTITION_COL_NAME");
        addMetadata();
    }

    public QHsPartitionColName(PathMetadata metadata) {
        super(QHsPartitionColName.class, metadata, "SYS", "HS_PARTITION_COL_NAME");
        addMetadata();
    }

    public void addMetadata() {
    }

}

