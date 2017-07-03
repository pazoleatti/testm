package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvPravTarif51427 is a Querydsl query type for QRaschsvPravTarif51427
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvPravTarif51427 extends com.querydsl.sql.RelationalPathBase<QRaschsvPravTarif51427> {

    private static final long serialVersionUID = -2090648063;

    public static final QRaschsvPravTarif51427 raschsvPravTarif51427 = new QRaschsvPravTarif51427("RASCHSV_PRAV_TARIF5_1_427");

    public final NumberPath<Long> doh34615vs = createNumber("doh34615vs", Long.class);

    public final NumberPath<Long> doh6427 = createNumber("doh6427", Long.class);

    public final NumberPath<java.math.BigDecimal> dolDoh6427 = createNumber("dolDoh6427", java.math.BigDecimal.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvPravTarif51427> raschsvPravTarif51427Pk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvTarif5427ObPlFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public QRaschsvPravTarif51427(String variable) {
        super(QRaschsvPravTarif51427.class, forVariable(variable), "NDFL_1_0", "RASCHSV_PRAV_TARIF5_1_427");
        addMetadata();
    }

    public QRaschsvPravTarif51427(String variable, String schema, String table) {
        super(QRaschsvPravTarif51427.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvPravTarif51427(Path<? extends QRaschsvPravTarif51427> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_PRAV_TARIF5_1_427");
        addMetadata();
    }

    public QRaschsvPravTarif51427(PathMetadata metadata) {
        super(QRaschsvPravTarif51427.class, metadata, "NDFL_1_0", "RASCHSV_PRAV_TARIF5_1_427");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(doh34615vs, ColumnMetadata.named("DOH346_15VS").withIndex(3).ofType(Types.DECIMAL).withSize(15));
        addMetadata(doh6427, ColumnMetadata.named("DOH6_427").withIndex(4).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dolDoh6427, ColumnMetadata.named("DOL_DOH6_427").withIndex(5).ofType(Types.DECIMAL).withSize(7).withDigits(2));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
    }

}

