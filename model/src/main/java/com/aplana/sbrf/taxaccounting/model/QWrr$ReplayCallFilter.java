package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QWrr$ReplayCallFilter is a Querydsl query type for QWrr$ReplayCallFilter
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QWrr$ReplayCallFilter extends com.querydsl.sql.RelationalPathBase<QWrr$ReplayCallFilter> {

    private static final long serialVersionUID = 1337670535;

    public static final QWrr$ReplayCallFilter wrr$ReplayCallFilter = new QWrr$ReplayCallFilter("WRR$_REPLAY_CALL_FILTER");

    public final NumberPath<java.math.BigInteger> callCounterBegin = createNumber("callCounterBegin", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> callCounterEnd = createNumber("callCounterEnd", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> fileId = createNumber("fileId", java.math.BigInteger.class);

    public QWrr$ReplayCallFilter(String variable) {
        super(QWrr$ReplayCallFilter.class, forVariable(variable), "SYS", "WRR$_REPLAY_CALL_FILTER");
        addMetadata();
    }

    public QWrr$ReplayCallFilter(String variable, String schema, String table) {
        super(QWrr$ReplayCallFilter.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QWrr$ReplayCallFilter(Path<? extends QWrr$ReplayCallFilter> path) {
        super(path.getType(), path.getMetadata(), "SYS", "WRR$_REPLAY_CALL_FILTER");
        addMetadata();
    }

    public QWrr$ReplayCallFilter(PathMetadata metadata) {
        super(QWrr$ReplayCallFilter.class, metadata, "SYS", "WRR$_REPLAY_CALL_FILTER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(callCounterBegin, ColumnMetadata.named("CALL_COUNTER_BEGIN").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(callCounterEnd, ColumnMetadata.named("CALL_COUNTER_END").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(fileId, ColumnMetadata.named("FILE_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

