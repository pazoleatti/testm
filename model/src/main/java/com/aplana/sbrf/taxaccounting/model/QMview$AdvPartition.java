package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMview$AdvPartition is a Querydsl query type for QMview$AdvPartition
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMview$AdvPartition extends com.querydsl.sql.RelationalPathBase<QMview$AdvPartition> {

    private static final long serialVersionUID = 1131188806;

    public static final QMview$AdvPartition mview$AdvPartition = new QMview$AdvPartition("MVIEW$_ADV_PARTITION");

    public final StringPath queryText = createString("queryText");

    public final NumberPath<java.math.BigInteger> rank_ = createNumber("rank_", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> runid_ = createNumber("runid_", java.math.BigInteger.class);

    public final StringPath summaryOwner = createString("summaryOwner");

    public QMview$AdvPartition(String variable) {
        super(QMview$AdvPartition.class, forVariable(variable), "SYSTEM", "MVIEW$_ADV_PARTITION");
        addMetadata();
    }

    public QMview$AdvPartition(String variable, String schema, String table) {
        super(QMview$AdvPartition.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMview$AdvPartition(Path<? extends QMview$AdvPartition> path) {
        super(path.getType(), path.getMetadata(), "SYSTEM", "MVIEW$_ADV_PARTITION");
        addMetadata();
    }

    public QMview$AdvPartition(PathMetadata metadata) {
        super(QMview$AdvPartition.class, metadata, "SYSTEM", "MVIEW$_ADV_PARTITION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(queryText, ColumnMetadata.named("QUERY_TEXT").withIndex(4).ofType(Types.LONGVARCHAR).withSize(0));
        addMetadata(rank_, ColumnMetadata.named("RANK#").withIndex(2).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(runid_, ColumnMetadata.named("RUNID#").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(summaryOwner, ColumnMetadata.named("SUMMARY_OWNER").withIndex(3).ofType(Types.VARCHAR).withSize(32));
    }

}

