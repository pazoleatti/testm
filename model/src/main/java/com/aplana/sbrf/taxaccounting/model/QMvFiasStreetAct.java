package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QMvFiasStreetAct is a Querydsl query type for QMvFiasStreetAct
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QMvFiasStreetAct extends com.querydsl.sql.RelationalPathBase<QMvFiasStreetAct> {

    private static final long serialVersionUID = 417502964;

    public static final QMvFiasStreetAct mvFiasStreetAct = new QMvFiasStreetAct("MV_FIAS_STREET_ACT");

    public final NumberPath<Long> aoid = createNumber("aoid", Long.class);

    public final NumberPath<Long> aolevel = createNumber("aolevel", Long.class);

    public final StringPath areacode = createString("areacode");

    public final StringPath autocode = createString("autocode");

    public final NumberPath<Integer> centstatus = createNumber("centstatus", Integer.class);

    public final StringPath citycode = createString("citycode");

    public final StringPath ctarcode = createString("ctarcode");

    public final NumberPath<Integer> currstatus = createNumber("currstatus", Integer.class);

    public final NumberPath<Byte> divtype = createNumber("divtype", Byte.class);

    public final StringPath extrcode = createString("extrcode");

    public final StringPath fname = createString("fname");

    public final StringPath formalname = createString("formalname");

    public final StringPath ftype = createString("ftype");

    public final NumberPath<java.math.BigInteger> hasChild = createNumber("hasChild", java.math.BigInteger.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Byte> livestatus = createNumber("livestatus", Byte.class);

    public final StringPath offname = createString("offname");

    public final NumberPath<Integer> operstatus = createNumber("operstatus", Integer.class);

    public final NumberPath<Long> parentguid = createNumber("parentguid", Long.class);

    public final StringPath placecode = createString("placecode");

    public final StringPath plancode = createString("plancode");

    public final StringPath postalcode = createString("postalcode");

    public final StringPath regioncode = createString("regioncode");

    public final StringPath sextcode = createString("sextcode");

    public final StringPath shortname = createString("shortname");

    public final StringPath streetcode = createString("streetcode");

    public final com.querydsl.sql.PrimaryKey<QMvFiasStreetAct> mvFiasStreetActPk = createPrimaryKey(id);

    public QMvFiasStreetAct(String variable) {
        super(QMvFiasStreetAct.class, forVariable(variable), "NDFL_UNSTABLE", "MV_FIAS_STREET_ACT");
        addMetadata();
    }

    public QMvFiasStreetAct(String variable, String schema, String table) {
        super(QMvFiasStreetAct.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QMvFiasStreetAct(Path<? extends QMvFiasStreetAct> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "MV_FIAS_STREET_ACT");
        addMetadata();
    }

    public QMvFiasStreetAct(PathMetadata metadata) {
        super(QMvFiasStreetAct.class, metadata, "NDFL_UNSTABLE", "MV_FIAS_STREET_ACT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(aoid, ColumnMetadata.named("AOID").withIndex(21).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(aolevel, ColumnMetadata.named("AOLEVEL").withIndex(23).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(areacode, ColumnMetadata.named("AREACODE").withIndex(5).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(autocode, ColumnMetadata.named("AUTOCODE").withIndex(4).ofType(Types.VARCHAR).withSize(1).notNull());
        addMetadata(centstatus, ColumnMetadata.named("CENTSTATUS").withIndex(14).ofType(Types.DECIMAL).withSize(2).notNull());
        addMetadata(citycode, ColumnMetadata.named("CITYCODE").withIndex(6).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(ctarcode, ColumnMetadata.named("CTARCODE").withIndex(7).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(currstatus, ColumnMetadata.named("CURRSTATUS").withIndex(16).ofType(Types.DECIMAL).withSize(2).notNull());
        addMetadata(divtype, ColumnMetadata.named("DIVTYPE").withIndex(17).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(extrcode, ColumnMetadata.named("EXTRCODE").withIndex(11).ofType(Types.VARCHAR).withSize(4).notNull());
        addMetadata(fname, ColumnMetadata.named("FNAME").withIndex(24).ofType(Types.VARCHAR).withSize(480));
        addMetadata(formalname, ColumnMetadata.named("FORMALNAME").withIndex(2).ofType(Types.VARCHAR).withSize(120).notNull());
        addMetadata(ftype, ColumnMetadata.named("FTYPE").withIndex(25).ofType(Types.VARCHAR).withSize(10));
        addMetadata(hasChild, ColumnMetadata.named("HAS_CHILD").withIndex(26).ofType(Types.DECIMAL).withSize(22));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(livestatus, ColumnMetadata.named("LIVESTATUS").withIndex(13).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(offname, ColumnMetadata.named("OFFNAME").withIndex(18).ofType(Types.VARCHAR).withSize(120));
        addMetadata(operstatus, ColumnMetadata.named("OPERSTATUS").withIndex(15).ofType(Types.DECIMAL).withSize(2).notNull());
        addMetadata(parentguid, ColumnMetadata.named("PARENTGUID").withIndex(20).ofType(Types.DECIMAL).withSize(18));
        addMetadata(placecode, ColumnMetadata.named("PLACECODE").withIndex(8).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(plancode, ColumnMetadata.named("PLANCODE").withIndex(9).ofType(Types.VARCHAR).withSize(4).notNull());
        addMetadata(postalcode, ColumnMetadata.named("POSTALCODE").withIndex(19).ofType(Types.VARCHAR).withSize(6));
        addMetadata(regioncode, ColumnMetadata.named("REGIONCODE").withIndex(3).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(sextcode, ColumnMetadata.named("SEXTCODE").withIndex(12).ofType(Types.VARCHAR).withSize(3).notNull());
        addMetadata(shortname, ColumnMetadata.named("SHORTNAME").withIndex(22).ofType(Types.VARCHAR).withSize(10));
        addMetadata(streetcode, ColumnMetadata.named("STREETCODE").withIndex(10).ofType(Types.VARCHAR).withSize(4).notNull());
    }

}

