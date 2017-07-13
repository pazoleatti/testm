package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvVypl is a Querydsl query type for QRaschsvSvVypl
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvVypl extends com.querydsl.sql.RelationalPathBase<QRaschsvSvVypl> {

    private static final long serialVersionUID = 1017885137;

    public static final QRaschsvSvVypl raschsvSvVypl = new QRaschsvSvVypl("RASCHSV_SV_VYPL");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> nachislSvVs3 = createNumber("nachislSvVs3", java.math.BigDecimal.class);

    public final NumberPath<Long> raschsvPersSvStrahLicId = createNumber("raschsvPersSvStrahLicId", Long.class);

    public final NumberPath<java.math.BigDecimal> sumVyplVs3 = createNumber("sumVyplVs3", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> vyplOpsDogVs3 = createNumber("vyplOpsDogVs3", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> vyplOpsVs3 = createNumber("vyplOpsVs3", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvVypl> raschsvSvVyplPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvPersSvStrahLic> raschsvSvVyplStrahLicFk = createForeignKey(raschsvPersSvStrahLicId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvVyplMk> _raschsvSvVyplMkSvVyplFk = createInvForeignKey(id, "RASCHSV_SV_VYPL_ID");

    public QRaschsvSvVypl(String variable) {
        super(QRaschsvSvVypl.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_SV_VYPL");
        addMetadata();
    }

    public QRaschsvSvVypl(String variable, String schema, String table) {
        super(QRaschsvSvVypl.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvVypl(Path<? extends QRaschsvSvVypl> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_SV_VYPL");
        addMetadata();
    }

    public QRaschsvSvVypl(PathMetadata metadata) {
        super(QRaschsvSvVypl.class, metadata, "NDFL_UNSTABLE", "RASCHSV_SV_VYPL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(nachislSvVs3, ColumnMetadata.named("NACHISL_SV_VS3").withIndex(5).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(raschsvPersSvStrahLicId, ColumnMetadata.named("RASCHSV_PERS_SV_STRAH_LIC_ID").withIndex(6).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sumVyplVs3, ColumnMetadata.named("SUM_VYPL_VS3").withIndex(2).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(vyplOpsDogVs3, ColumnMetadata.named("VYPL_OPS_DOG_VS3").withIndex(4).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(vyplOpsVs3, ColumnMetadata.named("VYPL_OPS_VS3").withIndex(3).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

