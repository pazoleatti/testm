package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvPrimTarif22425 is a Querydsl query type for QRaschsvSvPrimTarif22425
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvPrimTarif22425 extends com.querydsl.sql.RelationalPathBase<QRaschsvSvPrimTarif22425> {

    private static final long serialVersionUID = -618952819;

    public static final QRaschsvSvPrimTarif22425 raschsvSvPrimTarif22425 = new QRaschsvSvPrimTarif22425("RASCHSV_SV_PRIM_TARIF2_2_425");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvPrimTarif22425> raschsvSvPrimTarif2425Pk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvTarif2425ObPlFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvInoGrazd> _raschsvIGrazdTarif2425Fk = createInvForeignKey(id, "RASCHSV_SV_PRIM_TARIF2_425_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplatIt425> _raschsvVyplatTarif2425Fk = createInvForeignKey(id, "RASCHSV_SV_PRIM_TARIF2_425_ID");

    public QRaschsvSvPrimTarif22425(String variable) {
        super(QRaschsvSvPrimTarif22425.class, forVariable(variable), "NDFL_1_0", "RASCHSV_SV_PRIM_TARIF2_2_425");
        addMetadata();
    }

    public QRaschsvSvPrimTarif22425(String variable, String schema, String table) {
        super(QRaschsvSvPrimTarif22425.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvPrimTarif22425(Path<? extends QRaschsvSvPrimTarif22425> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_SV_PRIM_TARIF2_2_425");
        addMetadata();
    }

    public QRaschsvSvPrimTarif22425(PathMetadata metadata) {
        super(QRaschsvSvPrimTarif22425.class, metadata, "NDFL_1_0", "RASCHSV_SV_PRIM_TARIF2_2_425");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

