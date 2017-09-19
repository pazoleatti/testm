package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMvFiasAreaAct is a Querydsl query type for QMvFiasAreaAct
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMvFiasAreaAct extends com.querydsl.sql.RelationalPathBase<QMvFiasAreaAct> {

    private static final long serialVersionUID = -173676694;

    public static final QMvFiasAreaAct mvFiasAreaAct = new QMvFiasAreaAct("MV_FIAS_AREA_ACT");

    public final NumberPath<Long> aoid = createNumber("aoid", Long.class);

    public final NumberPath<Long> aolevel = createNumber("aolevel", Long.class);

    public final NumberPath<Integer> currstatus = createNumber("currstatus", Integer.class);

    public final StringPath fname = createString("fname");

    public final StringPath formalname = createString("formalname");

    public final StringPath ftype = createString("ftype");

    public final NumberPath<java.math.BigInteger> hasChild = createNumber("hasChild", java.math.BigInteger.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Byte> livestatus = createNumber("livestatus", Byte.class);

    public final NumberPath<Long> parentguid = createNumber("parentguid", Long.class);

    public final StringPath postalcode = createString("postalcode");

    public final StringPath regioncode = createString("regioncode");

    public final StringPath shortname = createString("shortname");

    public QMvFiasAreaAct(String variable) {
        super(QMvFiasAreaAct.class, forVariable(variable), "NDFL_UNSTABLE", "MV_FIAS_AREA_ACT");
        addMetadata();
    }

    public QMvFiasAreaAct(String variable, String schema, String table) {
        super(QMvFiasAreaAct.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMvFiasAreaAct(Path<? extends QMvFiasAreaAct> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "MV_FIAS_AREA_ACT");
        addMetadata();
    }

    public QMvFiasAreaAct(PathMetadata metadata) {
        super(QMvFiasAreaAct.class, metadata, "NDFL_UNSTABLE", "MV_FIAS_AREA_ACT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(aoid, ColumnMetadata.named("AOID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(aolevel, ColumnMetadata.named("AOLEVEL").withIndex(8).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(currstatus, ColumnMetadata.named("CURRSTATUS").withIndex(7).ofType(Types.DECIMAL).withSize(2).notNull());
        addMetadata(fname, ColumnMetadata.named("FNAME").withIndex(11).ofType(Types.VARCHAR).withSize(480));
        addMetadata(formalname, ColumnMetadata.named("FORMALNAME").withIndex(3).ofType(Types.VARCHAR).withSize(120).notNull());
        addMetadata(ftype, ColumnMetadata.named("FTYPE").withIndex(12).ofType(Types.VARCHAR).withSize(10));
        addMetadata(hasChild, ColumnMetadata.named("HAS_CHILD").withIndex(13).ofType(Types.DECIMAL).withSize(22));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(livestatus, ColumnMetadata.named("LIVESTATUS").withIndex(6).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(parentguid, ColumnMetadata.named("PARENTGUID").withIndex(10).ofType(Types.DECIMAL).withSize(18));
        addMetadata(postalcode, ColumnMetadata.named("POSTALCODE").withIndex(9).ofType(Types.VARCHAR).withSize(6));
        addMetadata(regioncode, ColumnMetadata.named("REGIONCODE").withIndex(5).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(shortname, ColumnMetadata.named("SHORTNAME").withIndex(4).ofType(Types.VARCHAR).withSize(10));
    }

}

