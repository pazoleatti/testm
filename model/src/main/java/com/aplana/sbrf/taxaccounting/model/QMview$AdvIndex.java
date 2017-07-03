package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMview$AdvIndex is a Querydsl query type for QMview$AdvIndex
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMview$AdvIndex extends com.querydsl.sql.RelationalPathBase<QMview$AdvIndex> {

    private static final long serialVersionUID = -315836882;

    public static final QMview$AdvIndex mview$AdvIndex = new QMview$AdvIndex("MVIEW$_ADV_INDEX");

    public final StringPath columnName = createString("columnName");

    public final StringPath indexContent = createString("indexContent");

    public final StringPath indexName = createString("indexName");

    public final NumberPath<java.math.BigInteger> indexType = createNumber("indexType", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> mvindex_ = createNumber("mvindex_", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> rank_ = createNumber("rank_", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> runid_ = createNumber("runid_", java.math.BigInteger.class);

    public final StringPath summaryOwner = createString("summaryOwner");

    public QMview$AdvIndex(String variable) {
        super(QMview$AdvIndex.class, forVariable(variable), "SYSTEM", "MVIEW$_ADV_INDEX");
        addMetadata();
    }

    public QMview$AdvIndex(String variable, String schema, String table) {
        super(QMview$AdvIndex.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMview$AdvIndex(Path<? extends QMview$AdvIndex> path) {
        super(path.getType(), path.getMetadata(), "SYSTEM", "MVIEW$_ADV_INDEX");
        addMetadata();
    }

    public QMview$AdvIndex(PathMetadata metadata) {
        super(QMview$AdvIndex.class, metadata, "SYSTEM", "MVIEW$_ADV_INDEX");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(columnName, ColumnMetadata.named("COLUMN_NAME").withIndex(6).ofType(Types.VARCHAR).withSize(32));
        addMetadata(indexContent, ColumnMetadata.named("INDEX_CONTENT").withIndex(7).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(indexName, ColumnMetadata.named("INDEX_NAME").withIndex(5).ofType(Types.VARCHAR).withSize(50));
        addMetadata(indexType, ColumnMetadata.named("INDEX_TYPE").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(mvindex_, ColumnMetadata.named("MVINDEX#").withIndex(3).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(rank_, ColumnMetadata.named("RANK#").withIndex(2).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(runid_, ColumnMetadata.named("RUNID#").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(summaryOwner, ColumnMetadata.named("SUMMARY_OWNER").withIndex(8).ofType(Types.VARCHAR).withSize(32));
    }

}

