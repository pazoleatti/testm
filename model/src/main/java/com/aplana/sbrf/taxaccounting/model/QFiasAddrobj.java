package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QFiasAddrobj is a Querydsl query type for QFiasAddrobj
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QFiasAddrobj extends com.querydsl.sql.RelationalPathBase<QFiasAddrobj> {

    private static final long serialVersionUID = 951540788;

    public static final QFiasAddrobj fiasAddrobj = new QFiasAddrobj("FIAS_ADDROBJ");

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

    public final StringPath formalname = createString("formalname");

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

    public final com.querydsl.sql.PrimaryKey<QFiasAddrobj> fiasAddrobjPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFiasAddrobj> fiasAddrobjParentidFk = createForeignKey(parentguid, "ID");

    public final com.querydsl.sql.ForeignKey<QFiasAddrobj> _fiasAddrobjParentidFk = createInvForeignKey(id, "PARENTGUID");

    public QFiasAddrobj(String variable) {
        super(QFiasAddrobj.class, forVariable(variable), "NDFL_UNSTABLE", "FIAS_ADDROBJ");
        addMetadata();
    }

    public QFiasAddrobj(String variable, String schema, String table) {
        super(QFiasAddrobj.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QFiasAddrobj(Path<? extends QFiasAddrobj> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "FIAS_ADDROBJ");
        addMetadata();
    }

    public QFiasAddrobj(PathMetadata metadata) {
        super(QFiasAddrobj.class, metadata, "NDFL_UNSTABLE", "FIAS_ADDROBJ");
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
        addMetadata(formalname, ColumnMetadata.named("FORMALNAME").withIndex(2).ofType(Types.VARCHAR).withSize(120).notNull());
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

