package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvedObuch is a Querydsl query type for QRaschsvSvedObuch
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvedObuch extends com.querydsl.sql.RelationalPathBase<QRaschsvSvedObuch> {

    private static final long serialVersionUID = 1142789270;

    public static final QRaschsvSvedObuch raschsvSvedObuch = new QRaschsvSvedObuch("RASCHSV_SVED_OBUCH");

    public final StringPath familia = createString("familia");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imya = createString("imya");

    public final StringPath otchestvo = createString("otchestvo");

    public final NumberPath<Long> raschsvSvPrimTarif1422Id = createNumber("raschsvSvPrimTarif1422Id", Long.class);

    public final NumberPath<Long> raschsvSvSum1TipId = createNumber("raschsvSvSum1TipId", Long.class);

    public final DateTimePath<org.joda.time.DateTime> spravData = createDateTime("spravData", org.joda.time.DateTime.class);

    public final StringPath spravNodeName = createString("spravNodeName");

    public final StringPath spravNomer = createString("spravNomer");

    public final StringPath unikNomer = createString("unikNomer");

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvedObuch> raschsvSvedObuchPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvSum1tip> raschsvSvedObuchSumFk = createForeignKey(raschsvSvSum1TipId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif13422> raschsvSvedObTarif1422Fk = createForeignKey(raschsvSvPrimTarif1422Id, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvReestrMdo> _raschsvSvReestrMdoObFk = createInvForeignKey(id, "RASCHSV_SVED_OBUCH_ID");

    public QRaschsvSvedObuch(String variable) {
        super(QRaschsvSvedObuch.class, forVariable(variable), "NDFL_1_0", "RASCHSV_SVED_OBUCH");
        addMetadata();
    }

    public QRaschsvSvedObuch(String variable, String schema, String table) {
        super(QRaschsvSvedObuch.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvedObuch(Path<? extends QRaschsvSvedObuch> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_SVED_OBUCH");
        addMetadata();
    }

    public QRaschsvSvedObuch(PathMetadata metadata) {
        super(QRaschsvSvedObuch.class, metadata, "NDFL_1_0", "RASCHSV_SVED_OBUCH");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(familia, ColumnMetadata.named("FAMILIA").withIndex(5).ofType(Types.VARCHAR).withSize(60));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(imya, ColumnMetadata.named("IMYA").withIndex(6).ofType(Types.VARCHAR).withSize(60));
        addMetadata(otchestvo, ColumnMetadata.named("OTCHESTVO").withIndex(7).ofType(Types.VARCHAR).withSize(60));
        addMetadata(raschsvSvPrimTarif1422Id, ColumnMetadata.named("RASCHSV_SV_PRIM_TARIF1_422_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvSvSum1TipId, ColumnMetadata.named("RASCHSV_SV_SUM1_TIP_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(spravData, ColumnMetadata.named("SPRAV_DATA").withIndex(9).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(spravNodeName, ColumnMetadata.named("SPRAV_NODE_NAME").withIndex(10).ofType(Types.VARCHAR).withSize(20));
        addMetadata(spravNomer, ColumnMetadata.named("SPRAV_NOMER").withIndex(8).ofType(Types.VARCHAR).withSize(10));
        addMetadata(unikNomer, ColumnMetadata.named("UNIK_NOMER").withIndex(4).ofType(Types.VARCHAR).withSize(3));
    }

}

