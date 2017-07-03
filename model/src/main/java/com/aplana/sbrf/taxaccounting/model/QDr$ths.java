package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDr$ths is a Querydsl query type for QDr$ths
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDr$ths extends com.querydsl.sql.RelationalPathBase<QDr$ths> {

    private static final long serialVersionUID = -570255674;

    public static final QDr$ths dr$ths = new QDr$ths("DR$THS");

    public final StringPath thsCase = createString("thsCase");

    public final NumberPath<java.math.BigInteger> thsId = createNumber("thsId", java.math.BigInteger.class);

    public final StringPath thsName = createString("thsName");

    public final NumberPath<java.math.BigInteger> thsOwner_ = createNumber("thsOwner_", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QDr$ths> sysC004089 = createPrimaryKey(thsId);

    public final com.querydsl.sql.ForeignKey<QDr$thsPhrase> _sysC004093 = createInvForeignKey(thsId, "THP_THSID");

    public QDr$ths(String variable) {
        super(QDr$ths.class, forVariable(variable), "CTXSYS", "DR$THS");
        addMetadata();
    }

    public QDr$ths(String variable, String schema, String table) {
        super(QDr$ths.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDr$ths(Path<? extends QDr$ths> path) {
        super(path.getType(), path.getMetadata(), "CTXSYS", "DR$THS");
        addMetadata();
    }

    public QDr$ths(PathMetadata metadata) {
        super(QDr$ths.class, metadata, "CTXSYS", "DR$THS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(thsCase, ColumnMetadata.named("THS_CASE").withIndex(4).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(thsId, ColumnMetadata.named("THS_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(thsName, ColumnMetadata.named("THS_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(30).notNull());
        addMetadata(thsOwner_, ColumnMetadata.named("THS_OWNER#").withIndex(3).ofType(Types.DECIMAL).withSize(22).notNull());
    }

}

