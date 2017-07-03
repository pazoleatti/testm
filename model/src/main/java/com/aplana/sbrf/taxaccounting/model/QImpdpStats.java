package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QImpdpStats is a Querydsl query type for QImpdpStats
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QImpdpStats extends com.querydsl.sql.RelationalPathBase<QImpdpStats> {

    private static final long serialVersionUID = -833194236;

    public static final QImpdpStats impdpStats = new QImpdpStats("IMPDP_STATS");

    public final StringPath c1 = createString("c1");

    public final StringPath c2 = createString("c2");

    public final StringPath c3 = createString("c3");

    public final StringPath c4 = createString("c4");

    public final StringPath c5 = createString("c5");

    public final StringPath ch1 = createString("ch1");

    public final StringPath cl1 = createString("cl1");

    public final DateTimePath<org.joda.time.DateTime> d1 = createDateTime("d1", org.joda.time.DateTime.class);

    public final NumberPath<java.math.BigInteger> flags = createNumber("flags", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n1 = createNumber("n1", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n10 = createNumber("n10", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n11 = createNumber("n11", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n12 = createNumber("n12", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n2 = createNumber("n2", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n3 = createNumber("n3", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n4 = createNumber("n4", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n5 = createNumber("n5", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n6 = createNumber("n6", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n7 = createNumber("n7", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n8 = createNumber("n8", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> n9 = createNumber("n9", java.math.BigInteger.class);

    public final SimplePath<byte[]> r1 = createSimple("r1", byte[].class);

    public final SimplePath<byte[]> r2 = createSimple("r2", byte[].class);

    public final StringPath statid = createString("statid");

    public final StringPath type = createString("type");

    public final NumberPath<java.math.BigInteger> version = createNumber("version", java.math.BigInteger.class);

    public QImpdpStats(String variable) {
        super(QImpdpStats.class, forVariable(variable), "SYS", "IMPDP_STATS");
        addMetadata();
    }

    public QImpdpStats(String variable, String schema, String table) {
        super(QImpdpStats.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QImpdpStats(Path<? extends QImpdpStats> path) {
        super(path.getType(), path.getMetadata(), "SYS", "IMPDP_STATS");
        addMetadata();
    }

    public QImpdpStats(PathMetadata metadata) {
        super(QImpdpStats.class, metadata, "SYS", "IMPDP_STATS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(c1, ColumnMetadata.named("C1").withIndex(5).ofType(Types.VARCHAR).withSize(30));
        addMetadata(c2, ColumnMetadata.named("C2").withIndex(6).ofType(Types.VARCHAR).withSize(30));
        addMetadata(c3, ColumnMetadata.named("C3").withIndex(7).ofType(Types.VARCHAR).withSize(30));
        addMetadata(c4, ColumnMetadata.named("C4").withIndex(8).ofType(Types.VARCHAR).withSize(30));
        addMetadata(c5, ColumnMetadata.named("C5").withIndex(9).ofType(Types.VARCHAR).withSize(30));
        addMetadata(ch1, ColumnMetadata.named("CH1").withIndex(25).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(cl1, ColumnMetadata.named("CL1").withIndex(26).ofType(Types.CLOB).withSize(4000));
        addMetadata(d1, ColumnMetadata.named("D1").withIndex(22).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(flags, ColumnMetadata.named("FLAGS").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n1, ColumnMetadata.named("N1").withIndex(10).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n10, ColumnMetadata.named("N10").withIndex(19).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n11, ColumnMetadata.named("N11").withIndex(20).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n12, ColumnMetadata.named("N12").withIndex(21).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n2, ColumnMetadata.named("N2").withIndex(11).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n3, ColumnMetadata.named("N3").withIndex(12).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n4, ColumnMetadata.named("N4").withIndex(13).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n5, ColumnMetadata.named("N5").withIndex(14).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n6, ColumnMetadata.named("N6").withIndex(15).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n7, ColumnMetadata.named("N7").withIndex(16).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n8, ColumnMetadata.named("N8").withIndex(17).ofType(Types.DECIMAL).withSize(22));
        addMetadata(n9, ColumnMetadata.named("N9").withIndex(18).ofType(Types.DECIMAL).withSize(22));
        addMetadata(r1, ColumnMetadata.named("R1").withIndex(23).ofType(Types.VARBINARY).withSize(32));
        addMetadata(r2, ColumnMetadata.named("R2").withIndex(24).ofType(Types.VARBINARY).withSize(32));
        addMetadata(statid, ColumnMetadata.named("STATID").withIndex(1).ofType(Types.VARCHAR).withSize(30));
        addMetadata(type, ColumnMetadata.named("TYPE").withIndex(2).ofType(Types.CHAR).withSize(1));
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.DECIMAL).withSize(22));
    }

}

