package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QDr$thsPhrase is a Querydsl query type for QDr$thsPhrase
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QDr$thsPhrase extends com.querydsl.sql.RelationalPathBase<QDr$thsPhrase> {

    private static final long serialVersionUID = 868119839;

    public static final QDr$thsPhrase dr$thsPhrase = new QDr$thsPhrase("DR$THS_PHRASE");

    public final NumberPath<java.math.BigInteger> thpId = createNumber("thpId", java.math.BigInteger.class);

    public final StringPath thpNote = createString("thpNote");

    public final StringPath thpPhrase = createString("thpPhrase");

    public final StringPath thpQualify = createString("thpQualify");

    public final NumberPath<java.math.BigInteger> thpRingid = createNumber("thpRingid", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> thpThsid = createNumber("thpThsid", java.math.BigInteger.class);

    public final com.querydsl.sql.PrimaryKey<QDr$thsPhrase> sysC004092 = createPrimaryKey(thpId);

    public final com.querydsl.sql.ForeignKey<QDr$ths> sysC004093 = createForeignKey(thpThsid, "THS_ID");

    public QDr$thsPhrase(String variable) {
        super(QDr$thsPhrase.class, forVariable(variable), "CTXSYS", "DR$THS_PHRASE");
        addMetadata();
    }

    public QDr$thsPhrase(String variable, String schema, String table) {
        super(QDr$thsPhrase.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QDr$thsPhrase(Path<? extends QDr$thsPhrase> path) {
        super(path.getType(), path.getMetadata(), "CTXSYS", "DR$THS_PHRASE");
        addMetadata();
    }

    public QDr$thsPhrase(PathMetadata metadata) {
        super(QDr$thsPhrase.class, metadata, "CTXSYS", "DR$THS_PHRASE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(thpId, ColumnMetadata.named("THP_ID").withIndex(1).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(thpNote, ColumnMetadata.named("THP_NOTE").withIndex(5).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(thpPhrase, ColumnMetadata.named("THP_PHRASE").withIndex(3).ofType(Types.VARCHAR).withSize(256).notNull());
        addMetadata(thpQualify, ColumnMetadata.named("THP_QUALIFY").withIndex(4).ofType(Types.VARCHAR).withSize(256));
        addMetadata(thpRingid, ColumnMetadata.named("THP_RINGID").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(thpThsid, ColumnMetadata.named("THP_THSID").withIndex(2).ofType(Types.DECIMAL).withSize(22));
    }

}

