package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvInoGrazd is a Querydsl query type for QRaschsvSvInoGrazd
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvInoGrazd extends com.querydsl.sql.RelationalPathBase<QRaschsvSvInoGrazd> {

    private static final long serialVersionUID = 688889224;

    public static final QRaschsvSvInoGrazd raschsvSvInoGrazd = new QRaschsvSvInoGrazd("RASCHSV_SV_INO_GRAZD");

    public final StringPath familia = createString("familia");

    public final StringPath grazd = createString("grazd");

    public final StringPath imya = createString("imya");

    public final StringPath innfl = createString("innfl");

    public final StringPath otchestvo = createString("otchestvo");

    public final NumberPath<Long> raschsvSvPrimTarif2425Id = createNumber("raschsvSvPrimTarif2425Id", Long.class);

    public final NumberPath<Long> raschsvSvSum1TipId = createNumber("raschsvSvSum1TipId", Long.class);

    public final StringPath snils = createString("snils");

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvInoGrazd> raschsvSvInoGrazdPk = createPrimaryKey(raschsvSvPrimTarif2425Id, raschsvSvSum1TipId);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvSum1tip> raschsvSvInoGrazdSumFk = createForeignKey(raschsvSvSum1TipId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif22425> raschsvIGrazdTarif2425Fk = createForeignKey(raschsvSvPrimTarif2425Id, "ID");

    public QRaschsvSvInoGrazd(String variable) {
        super(QRaschsvSvInoGrazd.class, forVariable(variable), "NDFL_1_0", "RASCHSV_SV_INO_GRAZD");
        addMetadata();
    }

    public QRaschsvSvInoGrazd(String variable, String schema, String table) {
        super(QRaschsvSvInoGrazd.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvInoGrazd(Path<? extends QRaschsvSvInoGrazd> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_SV_INO_GRAZD");
        addMetadata();
    }

    public QRaschsvSvInoGrazd(PathMetadata metadata) {
        super(QRaschsvSvInoGrazd.class, metadata, "NDFL_1_0", "RASCHSV_SV_INO_GRAZD");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(familia, ColumnMetadata.named("FAMILIA").withIndex(6).ofType(Types.VARCHAR).withSize(60));
        addMetadata(grazd, ColumnMetadata.named("GRAZD").withIndex(5).ofType(Types.VARCHAR).withSize(3));
        addMetadata(imya, ColumnMetadata.named("IMYA").withIndex(7).ofType(Types.VARCHAR).withSize(60));
        addMetadata(innfl, ColumnMetadata.named("INNFL").withIndex(3).ofType(Types.VARCHAR).withSize(12));
        addMetadata(otchestvo, ColumnMetadata.named("OTCHESTVO").withIndex(8).ofType(Types.VARCHAR).withSize(60));
        addMetadata(raschsvSvPrimTarif2425Id, ColumnMetadata.named("RASCHSV_SV_PRIM_TARIF2_425_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvSvSum1TipId, ColumnMetadata.named("RASCHSV_SV_SUM1_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(snils, ColumnMetadata.named("SNILS").withIndex(4).ofType(Types.VARCHAR).withSize(14));
    }

}

