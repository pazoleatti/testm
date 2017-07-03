package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QOl$ is a Querydsl query type for QOl$
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QOl$ extends com.querydsl.sql.RelationalPathBase<QOl$> {

    private static final long serialVersionUID = 363155370;

    public static final QOl$ ol$ = new QOl$("OL$");

    public final StringPath category = createString("category");

    public final StringPath creator = createString("creator");

    public final NumberPath<java.math.BigInteger> flags = createNumber("flags", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> hashValue = createNumber("hashValue", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> hashValue2 = createNumber("hashValue2", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> hintcount = createNumber("hintcount", java.math.BigInteger.class);

    public final StringPath olName = createString("olName");

    public final SimplePath<byte[]> signature = createSimple("signature", byte[].class);

    public final NumberPath<java.math.BigInteger> spare1 = createNumber("spare1", java.math.BigInteger.class);

    public final StringPath spare2 = createString("spare2");

    public final StringPath sqlText = createString("sqlText");

    public final NumberPath<java.math.BigInteger> textlen = createNumber("textlen", java.math.BigInteger.class);

    public final DateTimePath<org.joda.time.DateTime> timestamp = createDateTime("timestamp", org.joda.time.DateTime.class);

    public final StringPath version = createString("version");

    public QOl$(String variable) {
        super(QOl$.class, forVariable(variable), "SYSTEM", "OL$");
        addMetadata();
    }

    public QOl$(String variable, String schema, String table) {
        super(QOl$.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QOl$(Path<? extends QOl$> path) {
        super(path.getType(), path.getMetadata(), "SYSTEM", "OL$");
        addMetadata();
    }

    public QOl$(PathMetadata metadata) {
        super(QOl$.class, metadata, "SYSTEM", "OL$");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(category, ColumnMetadata.named("CATEGORY").withIndex(7).ofType(Types.VARCHAR).withSize(30));
        addMetadata(creator, ColumnMetadata.named("CREATOR").withIndex(9).ofType(Types.VARCHAR).withSize(30));
        addMetadata(flags, ColumnMetadata.named("FLAGS").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(hashValue, ColumnMetadata.named("HASH_VALUE").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(hashValue2, ColumnMetadata.named("HASH_VALUE2").withIndex(6).ofType(Types.DECIMAL).withSize(22));
        addMetadata(hintcount, ColumnMetadata.named("HINTCOUNT").withIndex(12).ofType(Types.DECIMAL).withSize(22));
        addMetadata(olName, ColumnMetadata.named("OL_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(signature, ColumnMetadata.named("SIGNATURE").withIndex(4).ofType(Types.VARBINARY).withSize(16));
        addMetadata(spare1, ColumnMetadata.named("SPARE1").withIndex(13).ofType(Types.DECIMAL).withSize(22));
        addMetadata(spare2, ColumnMetadata.named("SPARE2").withIndex(14).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(sqlText, ColumnMetadata.named("SQL_TEXT").withIndex(2).ofType(Types.LONGVARCHAR).withSize(0));
        addMetadata(textlen, ColumnMetadata.named("TEXTLEN").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(timestamp, ColumnMetadata.named("TIMESTAMP").withIndex(10).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(8).ofType(Types.VARCHAR).withSize(64));
    }

}

