package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvVyplatIt427 is a Querydsl query type for QRaschsvVyplatIt427
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvVyplatIt427 extends com.querydsl.sql.RelationalPathBase<QRaschsvVyplatIt427> {

    private static final long serialVersionUID = -1055803475;

    public static final QRaschsvVyplatIt427 raschsvVyplatIt427 = new QRaschsvVyplatIt427("RASCHSV_VYPLAT_IT_427");

    public final NumberPath<Long> raschsvSvPrimTarif9427Id = createNumber("raschsvSvPrimTarif9427Id", Long.class);

    public final NumberPath<Long> raschsvSvSum1TipId = createNumber("raschsvSvSum1TipId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvVyplatIt427> raschsvVyplatIt427Pk = createPrimaryKey(raschsvSvPrimTarif9427Id, raschsvSvSum1TipId);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvSum1tip> raschsvVyplatIt427SumFk = createForeignKey(raschsvSvSum1TipId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif91427> raschsvVyplatTarif9427Fk = createForeignKey(raschsvSvPrimTarif9427Id, "ID");

    public QRaschsvVyplatIt427(String variable) {
        super(QRaschsvVyplatIt427.class, forVariable(variable), "NDFL_1_0", "RASCHSV_VYPLAT_IT_427");
        addMetadata();
    }

    public QRaschsvVyplatIt427(String variable, String schema, String table) {
        super(QRaschsvVyplatIt427.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvVyplatIt427(Path<? extends QRaschsvVyplatIt427> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_VYPLAT_IT_427");
        addMetadata();
    }

    public QRaschsvVyplatIt427(PathMetadata metadata) {
        super(QRaschsvVyplatIt427.class, metadata, "NDFL_1_0", "RASCHSV_VYPLAT_IT_427");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(raschsvSvPrimTarif9427Id, ColumnMetadata.named("RASCHSV_SV_PRIM_TARIF9_427_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvSvSum1TipId, ColumnMetadata.named("RASCHSV_SV_SUM1_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

