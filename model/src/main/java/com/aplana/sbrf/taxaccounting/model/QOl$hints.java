package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOl$hints is a Querydsl query type for QOl$hints
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOl$hints extends com.querydsl.sql.RelationalPathBase<QOl$hints> {

    private static final long serialVersionUID = -1604919966;

    public static final QOl$hints ol$hints = new QOl$hints("OL$HINTS");

    public final NumberPath<Float> bytes = createNumber("bytes", Float.class);

    public final NumberPath<Float> cardinality = createNumber("cardinality", Float.class);

    public final StringPath category = createString("category");

    public final NumberPath<Float> cost = createNumber("cost", Float.class);

    public final NumberPath<java.math.BigInteger> hint_ = createNumber("hint_", java.math.BigInteger.class);

    public final StringPath hintString = createString("hintString");

    public final StringPath hintText = createString("hintText");

    public final NumberPath<java.math.BigInteger> hintTextlen = createNumber("hintTextlen", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> hintTextoff = createNumber("hintTextoff", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> hintType = createNumber("hintType", java.math.BigInteger.class);

    public final StringPath joinPred = createString("joinPred");

    public final NumberPath<java.math.BigInteger> node_ = createNumber("node_", java.math.BigInteger.class);

    public final StringPath olName = createString("olName");

    public final NumberPath<java.math.BigInteger> refId = createNumber("refId", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> spare1 = createNumber("spare1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> spare2 = createNumber("spare2", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> stage_ = createNumber("stage_", java.math.BigInteger.class);

    public final StringPath tableName = createString("tableName");

    public final NumberPath<java.math.BigInteger> tablePos = createNumber("tablePos", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> tableTin = createNumber("tableTin", java.math.BigInteger.class);

    public final StringPath userTableName = createString("userTableName");

    public QOl$hints(String variable) {
        super(QOl$hints.class, forVariable(variable), "SYSTEM", "OL$HINTS");
        addMetadata();
    }

    public QOl$hints(String variable, String schema, String table) {
        super(QOl$hints.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOl$hints(Path<? extends QOl$hints> path) {
        super(path.getType(), path.getMetadata(), "SYSTEM", "OL$HINTS");
        addMetadata();
    }

    public QOl$hints(PathMetadata metadata) {
        super(QOl$hints.class, metadata, "SYSTEM", "OL$HINTS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(bytes, ColumnMetadata.named("BYTES").withIndex(15).ofType(Types.FLOAT).withSize(126));
        addMetadata(cardinality, ColumnMetadata.named("CARDINALITY").withIndex(14).ofType(Types.FLOAT).withSize(126));
        addMetadata(category, ColumnMetadata.named("CATEGORY").withIndex(3).ofType(Types.VARCHAR).withSize(30));
        addMetadata(cost, ColumnMetadata.named("COST").withIndex(13).ofType(Types.FLOAT).withSize(126));
        addMetadata(hint_, ColumnMetadata.named("HINT#").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(hintString, ColumnMetadata.named("HINT_STRING").withIndex(21).ofType(Types.CLOB).withSize(4000));
        addMetadata(hintText, ColumnMetadata.named("HINT_TEXT").withIndex(5).ofType(Types.VARCHAR).withSize(512));
        addMetadata(hintTextlen, ColumnMetadata.named("HINT_TEXTLEN").withIndex(17).ofType(Types.DECIMAL).withSize(22));
        addMetadata(hintTextoff, ColumnMetadata.named("HINT_TEXTOFF").withIndex(16).ofType(Types.DECIMAL).withSize(22));
        addMetadata(hintType, ColumnMetadata.named("HINT_TYPE").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(joinPred, ColumnMetadata.named("JOIN_PRED").withIndex(18).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(node_, ColumnMetadata.named("NODE#").withIndex(7).ofType(Types.DECIMAL).withSize(22));
        addMetadata(olName, ColumnMetadata.named("OL_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(refId, ColumnMetadata.named("REF_ID").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(spare1, ColumnMetadata.named("SPARE1").withIndex(19).ofType(Types.DECIMAL).withSize(22));
        addMetadata(spare2, ColumnMetadata.named("SPARE2").withIndex(20).ofType(Types.DECIMAL).withSize(22));
        addMetadata(stage_, ColumnMetadata.named("STAGE#").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(tableName, ColumnMetadata.named("TABLE_NAME").withIndex(8).ofType(Types.VARCHAR).withSize(30));
        addMetadata(tablePos, ColumnMetadata.named("TABLE_POS").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(tableTin, ColumnMetadata.named("TABLE_TIN").withIndex(9).ofType(Types.DECIMAL).withSize(22));
        addMetadata(userTableName, ColumnMetadata.named("USER_TABLE_NAME").withIndex(12).ofType(Types.VARCHAR).withSize(64));
    }

}

