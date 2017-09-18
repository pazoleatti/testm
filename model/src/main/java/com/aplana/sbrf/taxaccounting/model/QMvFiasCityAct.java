package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMvFiasCityAct is a Querydsl query type for QMvFiasCityAct
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMvFiasCityAct extends com.querydsl.sql.RelationalPathBase<QMvFiasCityAct> {

    private static final long serialVersionUID = 1358236108;

    public static final QMvFiasCityAct mvFiasCityAct = new QMvFiasCityAct("MV_FIAS_CITY_ACT");

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

    public final com.querydsl.sql.PrimaryKey<QMvFiasCityAct> mvFiasCityActPk = createPrimaryKey(id);

    public QMvFiasCityAct(String variable) {
        super(QMvFiasCityAct.class, forVariable(variable), "NDFL_UNSTABLE", "MV_FIAS_CITY_ACT");
        addMetadata();
    }

    public QMvFiasCityAct(String variable, String schema, String table) {
        super(QMvFiasCityAct.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMvFiasCityAct(Path<? extends QMvFiasCityAct> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "MV_FIAS_CITY_ACT");
        addMetadata();
    }

    public QMvFiasCityAct(PathMetadata metadata) {
        super(QMvFiasCityAct.class, metadata, "NDFL_UNSTABLE", "MV_FIAS_CITY_ACT");
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

