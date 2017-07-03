package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QHs$ParallelMetadata is a Querydsl query type for QHs$ParallelMetadata
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QHs$ParallelMetadata extends com.querydsl.sql.RelationalPathBase<QHs$ParallelMetadata> {

    private static final long serialVersionUID = 1287166130;

    public static final QHs$ParallelMetadata hs$ParallelMetadata = new QHs$ParallelMetadata("HS$_PARALLEL_METADATA");

    public final StringPath dblink = createString("dblink");

    public final StringPath histColumn = createString("histColumn");

    public final StringPath histColumnType = createString("histColumnType");

    public final StringPath histogram = createString("histogram");

    public final StringPath indAvailable = createString("indAvailable");

    public final NumberPath<java.math.BigInteger> ncolAvgVal = createNumber("ncolAvgVal", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ncolMaxVal = createNumber("ncolMaxVal", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ncolMinVal = createNumber("ncolMinVal", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> numBuckets = createNumber("numBuckets", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> numPartitionColumns = createNumber("numPartitionColumns", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> numPartitions = createNumber("numPartitions", java.math.BigInteger.class);

    public final StringPath parallel = createString("parallel");

    public final NumberPath<java.math.BigInteger> parallelDegree = createNumber("parallelDegree", java.math.BigInteger.class);

    public final SimplePath<Object> partitionColNames = createSimple("partitionColNames", Object.class);

    public final SimplePath<Object> partitionColTypes = createSimple("partitionColTypes", Object.class);

    public final StringPath rangePartitioned = createString("rangePartitioned");

    public final StringPath remoteSchemaName = createString("remoteSchemaName");

    public final StringPath remoteTableName = createString("remoteTableName");

    public final StringPath sampleCap = createString("sampleCap");

    public final StringPath sampleColumn = createString("sampleColumn");

    public final StringPath sampleColumnType = createString("sampleColumnType");

    public final StringPath sampled = createString("sampled");

    public final com.querydsl.sql.PrimaryKey<QHs$ParallelMetadata> hsParallelMetadataPk = createPrimaryKey(dblink, remoteSchemaName, remoteTableName);

    public QHs$ParallelMetadata(String variable) {
        super(QHs$ParallelMetadata.class, forVariable(variable), "SYS", "HS$_PARALLEL_METADATA");
        addMetadata();
    }

    public QHs$ParallelMetadata(String variable, String schema, String table) {
        super(QHs$ParallelMetadata.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHs$ParallelMetadata(Path<? extends QHs$ParallelMetadata> path) {
        super(path.getType(), path.getMetadata(), "SYS", "HS$_PARALLEL_METADATA");
        addMetadata();
    }

    public QHs$ParallelMetadata(PathMetadata metadata) {
        super(QHs$ParallelMetadata.class, metadata, "SYS", "HS$_PARALLEL_METADATA");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dblink, ColumnMetadata.named("DBLINK").withIndex(1).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(histColumn, ColumnMetadata.named("HIST_COLUMN").withIndex(11).ofType(Types.VARCHAR).withSize(30));
        addMetadata(histColumnType, ColumnMetadata.named("HIST_COLUMN_TYPE").withIndex(12).ofType(Types.VARCHAR).withSize(30));
        addMetadata(histogram, ColumnMetadata.named("HISTOGRAM").withIndex(8).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(indAvailable, ColumnMetadata.named("IND_AVAILABLE").withIndex(9).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(ncolAvgVal, ColumnMetadata.named("NCOL_AVG_VAL").withIndex(20).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ncolMaxVal, ColumnMetadata.named("NCOL_MAX_VAL").withIndex(21).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ncolMinVal, ColumnMetadata.named("NCOL_MIN_VAL").withIndex(19).ofType(Types.DECIMAL).withSize(22));
        addMetadata(numBuckets, ColumnMetadata.named("NUM_BUCKETS").withIndex(22).ofType(Types.DECIMAL).withSize(22));
        addMetadata(numPartitionColumns, ColumnMetadata.named("NUM_PARTITION_COLUMNS").withIndex(16).ofType(Types.DECIMAL).withSize(22));
        addMetadata(numPartitions, ColumnMetadata.named("NUM_PARTITIONS").withIndex(15).ofType(Types.DECIMAL).withSize(22));
        addMetadata(parallel, ColumnMetadata.named("PARALLEL").withIndex(4).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(parallelDegree, ColumnMetadata.named("PARALLEL_DEGREE").withIndex(5).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(partitionColNames, ColumnMetadata.named("PARTITION_COL_NAMES").withIndex(17).ofType(Types.OTHER).withSize(16));
        addMetadata(partitionColTypes, ColumnMetadata.named("PARTITION_COL_TYPES").withIndex(18).ofType(Types.OTHER).withSize(16));
        addMetadata(rangePartitioned, ColumnMetadata.named("RANGE_PARTITIONED").withIndex(6).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(remoteSchemaName, ColumnMetadata.named("REMOTE_SCHEMA_NAME").withIndex(3).ofType(Types.VARCHAR).withSize(30).notNull());
        addMetadata(remoteTableName, ColumnMetadata.named("REMOTE_TABLE_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(30).notNull());
        addMetadata(sampleCap, ColumnMetadata.named("SAMPLE_CAP").withIndex(10).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(sampleColumn, ColumnMetadata.named("SAMPLE_COLUMN").withIndex(13).ofType(Types.VARCHAR).withSize(30));
        addMetadata(sampleColumnType, ColumnMetadata.named("SAMPLE_COLUMN_TYPE").withIndex(14).ofType(Types.VARCHAR).withSize(30));
        addMetadata(sampled, ColumnMetadata.named("SAMPLED").withIndex(7).ofType(Types.VARCHAR).withSize(1).notNull());
    }

}

