package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvVyplatIt422 is a Querydsl query type for QRaschsvVyplatIt422
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvVyplatIt422 extends com.querydsl.sql.RelationalPathBase<QRaschsvVyplatIt422> {

    private static final long serialVersionUID = -1055803480;

    public static final QRaschsvVyplatIt422 raschsvVyplatIt422 = new QRaschsvVyplatIt422("RASCHSV_VYPLAT_IT_422");

    public final NumberPath<Long> raschsvSvPrimTarif1422Id = createNumber("raschsvSvPrimTarif1422Id", Long.class);

    public final NumberPath<Long> raschsvSvSum1TipId = createNumber("raschsvSvSum1TipId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvVyplatIt422> raschsvVyplatIt422Pk = createPrimaryKey(raschsvSvPrimTarif1422Id, raschsvSvSum1TipId);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvSum1tip> raschsvVyplatIt422SumFk = createForeignKey(raschsvSvSum1TipId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif13422> raschsvVyplatTarif3422Fk = createForeignKey(raschsvSvPrimTarif1422Id, "ID");

    public QRaschsvVyplatIt422(String variable) {
        super(QRaschsvVyplatIt422.class, forVariable(variable), "NDFL_1_0", "RASCHSV_VYPLAT_IT_422");
        addMetadata();
    }

    public QRaschsvVyplatIt422(String variable, String schema, String table) {
        super(QRaschsvVyplatIt422.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvVyplatIt422(Path<? extends QRaschsvVyplatIt422> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_VYPLAT_IT_422");
        addMetadata();
    }

    public QRaschsvVyplatIt422(PathMetadata metadata) {
        super(QRaschsvVyplatIt422.class, metadata, "NDFL_1_0", "RASCHSV_VYPLAT_IT_422");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(raschsvSvPrimTarif1422Id, ColumnMetadata.named("RASCHSV_SV_PRIM_TARIF1_422_ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvSvSum1TipId, ColumnMetadata.named("RASCHSV_SV_SUM1_TIP_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

