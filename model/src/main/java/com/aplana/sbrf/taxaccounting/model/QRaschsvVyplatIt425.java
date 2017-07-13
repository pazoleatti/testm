package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvVyplatIt425 is a Querydsl query type for QRaschsvVyplatIt425
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvVyplatIt425 extends com.querydsl.sql.RelationalPathBase<QRaschsvVyplatIt425> {

    private static final long serialVersionUID = -1055803477;

    public static final QRaschsvVyplatIt425 raschsvVyplatIt425 = new QRaschsvVyplatIt425("RASCHSV_VYPLAT_IT_425");

    public final NumberPath<Long> raschsvSvPrimTarif2425Id = createNumber("raschsvSvPrimTarif2425Id", Long.class);

    public final NumberPath<Long> raschsvSvSum1TipId = createNumber("raschsvSvSum1TipId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvVyplatIt425> raschsvVyplatIt425Pk = createPrimaryKey(raschsvSvPrimTarif2425Id, raschsvSvSum1TipId);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif22425> raschsvVyplatTarif2425Fk = createForeignKey(raschsvSvPrimTarif2425Id, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvSum1tip> raschsvVyplatIt425SumFk = createForeignKey(raschsvSvSum1TipId, "ID");

    public QRaschsvVyplatIt425(String variable) {
        super(QRaschsvVyplatIt425.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_VYPLAT_IT_425");
        addMetadata();
    }

    public QRaschsvVyplatIt425(String variable, String schema, String table) {
        super(QRaschsvVyplatIt425.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvVyplatIt425(Path<? extends QRaschsvVyplatIt425> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_VYPLAT_IT_425");
        addMetadata();
    }

    public QRaschsvVyplatIt425(PathMetadata metadata) {
        super(QRaschsvVyplatIt425.class, metadata, "NDFL_UNSTABLE", "RASCHSV_VYPLAT_IT_425");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(raschsvSvPrimTarif2425Id, ColumnMetadata.named("RASCHSV_SV_PRIM_TARIF2_425_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvSvSum1TipId, ColumnMetadata.named("RASCHSV_SV_SUM1_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

