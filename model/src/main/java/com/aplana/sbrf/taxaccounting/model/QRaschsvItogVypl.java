package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvItogVypl is a Querydsl query type for QRaschsvItogVypl
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvItogVypl extends com.querydsl.sql.RelationalPathBase<QRaschsvItogVypl> {

    private static final long serialVersionUID = 1905145841;

    public static final QRaschsvItogVypl raschsvItogVypl = new QRaschsvItogVypl("RASCHSV_ITOG_VYPL");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kodKatLic = createString("kodKatLic");

    public final NumberPath<java.math.BigDecimal> kolFl = createNumber("kolFl", java.math.BigDecimal.class);

    public final StringPath mesyac = createString("mesyac");

    public final NumberPath<Long> raschsvItogStrahLicId = createNumber("raschsvItogStrahLicId", Long.class);

    public final NumberPath<java.math.BigDecimal> sumNachisl = createNumber("sumNachisl", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumVypl = createNumber("sumVypl", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> vyplOps = createNumber("vyplOps", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> vyplOpsDog = createNumber("vyplOpsDog", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvItogVypl> raschsvItogVyplPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvItogStrahLic> raschsvItogVyplStrahFk = createForeignKey(raschsvItogStrahLicId, "ID");

    public QRaschsvItogVypl(String variable) {
        super(QRaschsvItogVypl.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_ITOG_VYPL");
        addMetadata();
    }

    public QRaschsvItogVypl(String variable, String schema, String table) {
        super(QRaschsvItogVypl.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvItogVypl(Path<? extends QRaschsvItogVypl> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_ITOG_VYPL");
        addMetadata();
    }

    public QRaschsvItogVypl(PathMetadata metadata) {
        super(QRaschsvItogVypl.class, metadata, "NDFL_UNSTABLE", "RASCHSV_ITOG_VYPL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kodKatLic, ColumnMetadata.named("KOD_KAT_LIC").withIndex(4).ofType(Types.VARCHAR).withSize(4).notNull());
        addMetadata(kolFl, ColumnMetadata.named("KOL_FL").withIndex(5).ofType(Types.DECIMAL).withSize(20).notNull());
        addMetadata(mesyac, ColumnMetadata.named("MESYAC").withIndex(3).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(raschsvItogStrahLicId, ColumnMetadata.named("RASCHSV_ITOG_STRAH_LIC_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sumNachisl, ColumnMetadata.named("SUM_NACHISL").withIndex(9).ofType(Types.DECIMAL).withSize(25).withDigits(2));
        addMetadata(sumVypl, ColumnMetadata.named("SUM_VYPL").withIndex(6).ofType(Types.DECIMAL).withSize(25).withDigits(2).notNull());
        addMetadata(vyplOps, ColumnMetadata.named("VYPL_OPS").withIndex(7).ofType(Types.DECIMAL).withSize(25).withDigits(2));
        addMetadata(vyplOpsDog, ColumnMetadata.named("VYPL_OPS_DOG").withIndex(8).ofType(Types.DECIMAL).withSize(25).withDigits(2));
    }

}

