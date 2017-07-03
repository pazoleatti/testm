package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvVyplMk is a Querydsl query type for QRaschsvSvVyplMk
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvVyplMk extends com.querydsl.sql.RelationalPathBase<QRaschsvSvVyplMk> {

    private static final long serialVersionUID = -1064924337;

    public static final QRaschsvSvVyplMk raschsvSvVyplMk = new QRaschsvSvVyplMk("RASCHSV_SV_VYPL_MK");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath kodKatLic = createString("kodKatLic");

    public final StringPath mesyac = createString("mesyac");

    public final NumberPath<java.math.BigDecimal> nachislSv = createNumber("nachislSv", java.math.BigDecimal.class);

    public final NumberPath<Long> raschsvSvVyplId = createNumber("raschsvSvVyplId", Long.class);

    public final NumberPath<java.math.BigDecimal> sumVypl = createNumber("sumVypl", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> vyplOps = createNumber("vyplOps", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> vyplOpsDog = createNumber("vyplOpsDog", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvVyplMk> raschvVyplMkPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvSvVypl> raschsvSvVyplMkSvVyplFk = createForeignKey(raschsvSvVyplId, "ID");

    public QRaschsvSvVyplMk(String variable) {
        super(QRaschsvSvVyplMk.class, forVariable(variable), "NDFL_1_0", "RASCHSV_SV_VYPL_MK");
        addMetadata();
    }

    public QRaschsvSvVyplMk(String variable, String schema, String table) {
        super(QRaschsvSvVyplMk.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvVyplMk(Path<? extends QRaschsvSvVyplMk> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_SV_VYPL_MK");
        addMetadata();
    }

    public QRaschsvSvVyplMk(PathMetadata metadata) {
        super(QRaschsvSvVyplMk.class, metadata, "NDFL_1_0", "RASCHSV_SV_VYPL_MK");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kodKatLic, ColumnMetadata.named("KOD_KAT_LIC").withIndex(4).ofType(Types.VARCHAR).withSize(4));
        addMetadata(mesyac, ColumnMetadata.named("MESYAC").withIndex(3).ofType(Types.VARCHAR).withSize(2));
        addMetadata(nachislSv, ColumnMetadata.named("NACHISL_SV").withIndex(8).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(raschsvSvVyplId, ColumnMetadata.named("RASCHSV_SV_VYPL_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sumVypl, ColumnMetadata.named("SUM_VYPL").withIndex(5).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(vyplOps, ColumnMetadata.named("VYPL_OPS").withIndex(6).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(vyplOpsDog, ColumnMetadata.named("VYPL_OPS_DOG").withIndex(7).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

