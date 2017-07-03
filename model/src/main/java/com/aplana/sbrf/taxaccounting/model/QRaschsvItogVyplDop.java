package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvItogVyplDop is a Querydsl query type for QRaschsvItogVyplDop
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvItogVyplDop extends com.querydsl.sql.RelationalPathBase<QRaschsvItogVyplDop> {

    private static final long serialVersionUID = -1792998508;

    public static final QRaschsvItogVyplDop raschsvItogVyplDop = new QRaschsvItogVyplDop("RASCHSV_ITOG_VYPL_DOP");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigInteger> kolFl = createNumber("kolFl", java.math.BigInteger.class);

    public final StringPath mesyac = createString("mesyac");

    public final NumberPath<Long> raschsvItogStrahLicId = createNumber("raschsvItogStrahLicId", Long.class);

    public final NumberPath<java.math.BigDecimal> sumNachisl = createNumber("sumNachisl", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumVypl = createNumber("sumVypl", java.math.BigDecimal.class);

    public final StringPath tarif = createString("tarif");

    public final com.querydsl.sql.PrimaryKey<QRaschsvItogVyplDop> raschsvItogVyplDopPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvItogStrahLic> raschsvItogVyplDopStrahFk = createForeignKey(raschsvItogStrahLicId, "ID");

    public QRaschsvItogVyplDop(String variable) {
        super(QRaschsvItogVyplDop.class, forVariable(variable), "NDFL_1_0", "RASCHSV_ITOG_VYPL_DOP");
        addMetadata();
    }

    public QRaschsvItogVyplDop(String variable, String schema, String table) {
        super(QRaschsvItogVyplDop.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvItogVyplDop(Path<? extends QRaschsvItogVyplDop> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_ITOG_VYPL_DOP");
        addMetadata();
    }

    public QRaschsvItogVyplDop(PathMetadata metadata) {
        super(QRaschsvItogVyplDop.class, metadata, "NDFL_1_0", "RASCHSV_ITOG_VYPL_DOP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kolFl, ColumnMetadata.named("KOL_FL").withIndex(5).ofType(Types.DECIMAL).withSize(20).notNull());
        addMetadata(mesyac, ColumnMetadata.named("MESYAC").withIndex(3).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(raschsvItogStrahLicId, ColumnMetadata.named("RASCHSV_ITOG_STRAH_LIC_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sumNachisl, ColumnMetadata.named("SUM_NACHISL").withIndex(7).ofType(Types.DECIMAL).withSize(25).withDigits(2));
        addMetadata(sumVypl, ColumnMetadata.named("SUM_VYPL").withIndex(6).ofType(Types.DECIMAL).withSize(25).withDigits(2).notNull());
        addMetadata(tarif, ColumnMetadata.named("TARIF").withIndex(4).ofType(Types.VARCHAR).withSize(2).notNull());
    }

}

