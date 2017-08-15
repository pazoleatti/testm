package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvedPatent is a Querydsl query type for QRaschsvSvedPatent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvedPatent extends com.querydsl.sql.RelationalPathBase<QRaschsvSvedPatent> {

    private static final long serialVersionUID = 1094407065;

    public static final QRaschsvSvedPatent raschsvSvedPatent = new QRaschsvSvedPatent("RASCHSV_SVED_PATENT");

    public final DateTimePath<org.joda.time.LocalDateTime> dataKonDeyst = createDateTime("dataKonDeyst", org.joda.time.LocalDateTime.class);

    public final DateTimePath<org.joda.time.LocalDateTime> dataNachDeyst = createDateTime("dataNachDeyst", org.joda.time.LocalDateTime.class);

    public final StringPath nomPatent = createString("nomPatent");

    public final NumberPath<Long> raschsvSvPrimTarif9427Id = createNumber("raschsvSvPrimTarif9427Id", Long.class);

    public final NumberPath<Long> raschsvSvSum1TipId = createNumber("raschsvSvSum1TipId", Long.class);

    public final StringPath vydDeyatPatent = createString("vydDeyatPatent");

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvedPatent> raschsvSvedPatentPk = createPrimaryKey(raschsvSvPrimTarif9427Id, raschsvSvSum1TipId);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvSum1tip> raschsvSvedPatentSumFk = createForeignKey(raschsvSvSum1TipId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif91427> raschsvSvedPTarif9427Fk = createForeignKey(raschsvSvPrimTarif9427Id, "ID");

    public QRaschsvSvedPatent(String variable) {
        super(QRaschsvSvedPatent.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_SVED_PATENT");
        addMetadata();
    }

    public QRaschsvSvedPatent(String variable, String schema, String table) {
        super(QRaschsvSvedPatent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvedPatent(Path<? extends QRaschsvSvedPatent> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_SVED_PATENT");
        addMetadata();
    }

    public QRaschsvSvedPatent(PathMetadata metadata) {
        super(QRaschsvSvedPatent.class, metadata, "NDFL_UNSTABLE", "RASCHSV_SVED_PATENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataKonDeyst, ColumnMetadata.named("DATA_KON_DEYST").withIndex(6).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(dataNachDeyst, ColumnMetadata.named("DATA_NACH_DEYST").withIndex(5).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(nomPatent, ColumnMetadata.named("NOM_PATENT").withIndex(3).ofType(Types.VARCHAR).withSize(20));
        addMetadata(raschsvSvPrimTarif9427Id, ColumnMetadata.named("RASCHSV_SV_PRIM_TARIF9_427_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvSvSum1TipId, ColumnMetadata.named("RASCHSV_SV_SUM1_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(vydDeyatPatent, ColumnMetadata.named("VYD_DEYAT_PATENT").withIndex(4).ofType(Types.VARCHAR).withSize(6));
    }

}

