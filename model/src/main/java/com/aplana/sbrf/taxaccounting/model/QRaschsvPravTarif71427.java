package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvPravTarif71427 is a Querydsl query type for QRaschsvPravTarif71427
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvPravTarif71427 extends com.querydsl.sql.RelationalPathBase<QRaschsvPravTarif71427> {

    private static final long serialVersionUID = -2088801021;

    public static final QRaschsvPravTarif71427 raschsvPravTarif71427 = new QRaschsvPravTarif71427("RASCHSV_PRAV_TARIF7_1_427");

    public final NumberPath<Long> dohCelPostPer = createNumber("dohCelPostPer", Long.class);

    public final NumberPath<Long> dohCelPostPred = createNumber("dohCelPostPred", Long.class);

    public final NumberPath<Long> dohEkDeyatPer = createNumber("dohEkDeyatPer", Long.class);

    public final NumberPath<Long> dohEkDeyatPred = createNumber("dohEkDeyatPred", Long.class);

    public final NumberPath<Long> dohGrantPer = createNumber("dohGrantPer", Long.class);

    public final NumberPath<Long> dohGrantPred = createNumber("dohGrantPred", Long.class);

    public final NumberPath<Long> dohVsPer = createNumber("dohVsPer", Long.class);

    public final NumberPath<Long> dohVsPred = createNumber("dohVsPred", Long.class);

    public final NumberPath<java.math.BigDecimal> dolDohPer = createNumber("dolDohPer", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> dolDohPred = createNumber("dolDohPred", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvPravTarif71427> raschsvPravTarif71427Pk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvTarif7427ObPlFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public QRaschsvPravTarif71427(String variable) {
        super(QRaschsvPravTarif71427.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_PRAV_TARIF7_1_427");
        addMetadata();
    }

    public QRaschsvPravTarif71427(String variable, String schema, String table) {
        super(QRaschsvPravTarif71427.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvPravTarif71427(Path<? extends QRaschsvPravTarif71427> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_PRAV_TARIF7_1_427");
        addMetadata();
    }

    public QRaschsvPravTarif71427(PathMetadata metadata) {
        super(QRaschsvPravTarif71427.class, metadata, "NDFL_UNSTABLE", "RASCHSV_PRAV_TARIF7_1_427");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dohCelPostPer, ColumnMetadata.named("DOH_CEL_POST_PER").withIndex(6).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohCelPostPred, ColumnMetadata.named("DOH_CEL_POST_PRED").withIndex(5).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohEkDeyatPer, ColumnMetadata.named("DOH_EK_DEYAT_PER").withIndex(10).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohEkDeyatPred, ColumnMetadata.named("DOH_EK_DEYAT_PRED").withIndex(9).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohGrantPer, ColumnMetadata.named("DOH_GRANT_PER").withIndex(8).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohGrantPred, ColumnMetadata.named("DOH_GRANT_PRED").withIndex(7).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohVsPer, ColumnMetadata.named("DOH_VS_PER").withIndex(4).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohVsPred, ColumnMetadata.named("DOH_VS_PRED").withIndex(3).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dolDohPer, ColumnMetadata.named("DOL_DOH_PER").withIndex(12).ofType(Types.DECIMAL).withSize(7).withDigits(2));
        addMetadata(dolDohPred, ColumnMetadata.named("DOL_DOH_PRED").withIndex(11).ofType(Types.DECIMAL).withSize(7).withDigits(2));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

