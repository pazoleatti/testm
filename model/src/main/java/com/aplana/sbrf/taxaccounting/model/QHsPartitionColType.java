package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QHsPartitionColType is a Querydsl query type for QHsPartitionColType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QHsPartitionColType extends com.querydsl.sql.RelationalPathBase<QHsPartitionColType> {

    private static final long serialVersionUID = -1042273896;

    public static final QHsPartitionColType hsPartitionColType = new QHsPartitionColType("HS_PARTITION_COL_TYPE");

    public QHsPartitionColType(String variable) {
        super(QHsPartitionColType.class, forVariable(variable), "SYS", "HS_PARTITION_COL_TYPE");
        addMetadata();
    }

    public QHsPartitionColType(String variable, String schema, String table) {
        super(QHsPartitionColType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHsPartitionColType(Path<? extends QHsPartitionColType> path) {
        super(path.getType(), path.getMetadata(), "SYS", "HS_PARTITION_COL_TYPE");
        addMetadata();
    }

    public QHsPartitionColType(PathMetadata metadata) {
        super(QHsPartitionColType.class, metadata, "SYS", "HS_PARTITION_COL_TYPE");
        addMetadata();
    }

    public void addMetadata() {
    }

}

