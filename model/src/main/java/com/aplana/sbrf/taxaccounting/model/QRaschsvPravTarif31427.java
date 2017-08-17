package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvPravTarif31427 is a Querydsl query type for QRaschsvPravTarif31427
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvPravTarif31427 extends com.querydsl.sql.RelationalPathBase<QRaschsvPravTarif31427> {

    private static final long serialVersionUID = -2092495105;

    public static final QRaschsvPravTarif31427 raschsvPravTarif31427 = new QRaschsvPravTarif31427("RASCHSV_PRAV_TARIF3_1_427");

    public final DateTimePath<org.joda.time.LocalDateTime> dataZapAkOrg = createDateTime("dataZapAkOrg", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> doh2489mpr = createNumber("doh2489mpr", Long.class);

    public final NumberPath<Long> doh248Per = createNumber("doh248Per", Long.class);

    public final NumberPath<java.math.BigDecimal> dohDoh54279mpr = createNumber("dohDoh54279mpr", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> dohDoh5427Per = createNumber("dohDoh5427Per", java.math.BigDecimal.class);

    public final NumberPath<Long> dohKr54279mpr = createNumber("dohKr54279mpr", Long.class);

    public final NumberPath<Long> dohKr5427Per = createNumber("dohKr5427Per", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath nomZapAkOrg = createString("nomZapAkOrg");

    public final NumberPath<Long> raschsvObyazPlatSvId = createNumber("raschsvObyazPlatSvId", Long.class);

    public final NumberPath<Integer> srChisl9mpr = createNumber("srChisl9mpr", Integer.class);

    public final NumberPath<Integer> srChislPer = createNumber("srChislPer", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvPravTarif31427> raschsvPravTarif31427Pk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvObyazPlatSv> raschsvTarif3427ObPlFk = createForeignKey(raschsvObyazPlatSvId, "ID");

    public QRaschsvPravTarif31427(String variable) {
        super(QRaschsvPravTarif31427.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_PRAV_TARIF3_1_427");
        addMetadata();
    }

    public QRaschsvPravTarif31427(String variable, String schema, String table) {
        super(QRaschsvPravTarif31427.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvPravTarif31427(Path<? extends QRaschsvPravTarif31427> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_PRAV_TARIF3_1_427");
        addMetadata();
    }

    public QRaschsvPravTarif31427(PathMetadata metadata) {
        super(QRaschsvPravTarif31427.class, metadata, "NDFL_UNSTABLE", "RASCHSV_PRAV_TARIF3_1_427");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataZapAkOrg, ColumnMetadata.named("DATA_ZAP_AK_ORG").withIndex(11).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(doh2489mpr, ColumnMetadata.named("DOH248_9MPR").withIndex(5).ofType(Types.DECIMAL).withSize(15));
        addMetadata(doh248Per, ColumnMetadata.named("DOH248_PER").withIndex(6).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohDoh54279mpr, ColumnMetadata.named("DOH_DOH5_427_9MPR").withIndex(9).ofType(Types.DECIMAL).withSize(7).withDigits(2));
        addMetadata(dohDoh5427Per, ColumnMetadata.named("DOH_DOH5_427_PER").withIndex(10).ofType(Types.DECIMAL).withSize(7).withDigits(2));
        addMetadata(dohKr54279mpr, ColumnMetadata.named("DOH_KR5_427_9MPR").withIndex(7).ofType(Types.DECIMAL).withSize(15));
        addMetadata(dohKr5427Per, ColumnMetadata.named("DOH_KR5_427_PER").withIndex(8).ofType(Types.DECIMAL).withSize(15));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(nomZapAkOrg, ColumnMetadata.named("NOM_ZAP_AK_ORG").withIndex(12).ofType(Types.VARCHAR).withSize(18));
        addMetadata(raschsvObyazPlatSvId, ColumnMetadata.named("RASCHSV_OBYAZ_PLAT_SV_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(srChisl9mpr, ColumnMetadata.named("SR_CHISL_9MPR").withIndex(3).ofType(Types.DECIMAL).withSize(7));
        addMetadata(srChislPer, ColumnMetadata.named("SR_CHISL_PER").withIndex(4).ofType(Types.DECIMAL).withSize(7));
    }

}

