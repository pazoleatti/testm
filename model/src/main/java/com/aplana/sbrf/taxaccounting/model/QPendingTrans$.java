package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPendingTrans$ is a Querydsl query type for QPendingTrans$
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPendingTrans$ extends com.querydsl.sql.RelationalPathBase<QPendingTrans$> {

    private static final long serialVersionUID = -14641930;

    public static final QPendingTrans$ pendingTrans$ = new QPendingTrans$("PENDING_TRANS$");

    public final DateTimePath<org.joda.time.DateTime> failTime = createDateTime("failTime", org.joda.time.DateTime.class);

    public final StringPath globalCommit_ = createString("globalCommit_");

    public final SimplePath<byte[]> globalForeignId = createSimple("globalForeignId", byte[].class);

    public final StringPath globalOracleId = createString("globalOracleId");

    public final NumberPath<java.math.BigInteger> globalTranFmt = createNumber("globalTranFmt", java.math.BigInteger.class);

    public final StringPath heuristicDflt = createString("heuristicDflt");

    public final DateTimePath<org.joda.time.DateTime> heuristicTime = createDateTime("heuristicTime", org.joda.time.DateTime.class);

    public final StringPath localTranId = createString("localTranId");

    public final DateTimePath<org.joda.time.DateTime> recoTime = createDateTime("recoTime", org.joda.time.DateTime.class);

    public final SimplePath<byte[]> recoVector = createSimple("recoVector", byte[].class);

    public final SimplePath<byte[]> sessionVector = createSimple("sessionVector", byte[].class);

    public final NumberPath<java.math.BigInteger> spare1 = createNumber("spare1", java.math.BigInteger.class);

    public final StringPath spare2 = createString("spare2");

    public final NumberPath<java.math.BigInteger> spare3 = createNumber("spare3", java.math.BigInteger.class);

    public final StringPath spare4 = createString("spare4");

    public final StringPath state = createString("state");

    public final StringPath status = createString("status");

    public final StringPath topDbUser = createString("topDbUser");

    public final StringPath topOsHost = createString("topOsHost");

    public final StringPath topOsTerminal = createString("topOsTerminal");

    public final StringPath topOsUser = createString("topOsUser");

    public final StringPath tranComment = createString("tranComment");

    public final NumberPath<java.math.BigInteger> type_ = createNumber("type_", java.math.BigInteger.class);

    public QPendingTrans$(String variable) {
        super(QPendingTrans$.class, forVariable(variable), "SYS", "PENDING_TRANS$");
        addMetadata();
    }

    public QPendingTrans$(String variable, String schema, String table) {
        super(QPendingTrans$.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPendingTrans$(Path<? extends QPendingTrans$> path) {
        super(path.getType(), path.getMetadata(), "SYS", "PENDING_TRANS$");
        addMetadata();
    }

    public QPendingTrans$(PathMetadata metadata) {
        super(QPendingTrans$.class, metadata, "SYS", "PENDING_TRANS$");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(failTime, ColumnMetadata.named("FAIL_TIME").withIndex(12).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(globalCommit_, ColumnMetadata.named("GLOBAL_COMMIT#").withIndex(19).ofType(Types.VARCHAR).withSize(16));
        addMetadata(globalForeignId, ColumnMetadata.named("GLOBAL_FOREIGN_ID").withIndex(4).ofType(Types.VARBINARY).withSize(64));
        addMetadata(globalOracleId, ColumnMetadata.named("GLOBAL_ORACLE_ID").withIndex(3).ofType(Types.VARCHAR).withSize(64));
        addMetadata(globalTranFmt, ColumnMetadata.named("GLOBAL_TRAN_FMT").withIndex(2).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(heuristicDflt, ColumnMetadata.named("HEURISTIC_DFLT").withIndex(8).ofType(Types.VARCHAR).withSize(1));
        addMetadata(heuristicTime, ColumnMetadata.named("HEURISTIC_TIME").withIndex(13).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(localTranId, ColumnMetadata.named("LOCAL_TRAN_ID").withIndex(1).ofType(Types.VARCHAR).withSize(22).notNull());
        addMetadata(recoTime, ColumnMetadata.named("RECO_TIME").withIndex(14).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(recoVector, ColumnMetadata.named("RECO_VECTOR").withIndex(10).ofType(Types.VARBINARY).withSize(4).notNull());
        addMetadata(sessionVector, ColumnMetadata.named("SESSION_VECTOR").withIndex(9).ofType(Types.VARBINARY).withSize(4).notNull());
        addMetadata(spare1, ColumnMetadata.named("SPARE1").withIndex(20).ofType(Types.DECIMAL).withSize(22));
        addMetadata(spare2, ColumnMetadata.named("SPARE2").withIndex(21).ofType(Types.VARCHAR).withSize(30));
        addMetadata(spare3, ColumnMetadata.named("SPARE3").withIndex(22).ofType(Types.DECIMAL).withSize(22));
        addMetadata(spare4, ColumnMetadata.named("SPARE4").withIndex(23).ofType(Types.VARCHAR).withSize(30));
        addMetadata(state, ColumnMetadata.named("STATE").withIndex(6).ofType(Types.VARCHAR).withSize(16).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(7).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(topDbUser, ColumnMetadata.named("TOP_DB_USER").withIndex(15).ofType(Types.VARCHAR).withSize(30));
        addMetadata(topOsHost, ColumnMetadata.named("TOP_OS_HOST").withIndex(17).ofType(Types.VARCHAR).withSize(128));
        addMetadata(topOsTerminal, ColumnMetadata.named("TOP_OS_TERMINAL").withIndex(18).ofType(Types.VARCHAR).withSize(255));
        addMetadata(topOsUser, ColumnMetadata.named("TOP_OS_USER").withIndex(16).ofType(Types.VARCHAR).withSize(64));
        addMetadata(tranComment, ColumnMetadata.named("TRAN_COMMENT").withIndex(5).ofType(Types.VARCHAR).withSize(255));
        addMetadata(type_, ColumnMetadata.named("TYPE#").withIndex(11).ofType(Types.DECIMAL).withSize(22));
    }

}

