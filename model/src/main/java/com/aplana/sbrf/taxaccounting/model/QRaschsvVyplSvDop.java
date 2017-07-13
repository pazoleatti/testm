package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvVyplSvDop is a Querydsl query type for QRaschsvVyplSvDop
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvVyplSvDop extends com.querydsl.sql.RelationalPathBase<QRaschsvVyplSvDop> {

    private static final long serialVersionUID = -1940228108;

    public static final QRaschsvVyplSvDop raschsvVyplSvDop = new QRaschsvVyplSvDop("RASCHSV_VYPL_SV_DOP");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> nachislSvVs3 = createNumber("nachislSvVs3", java.math.BigDecimal.class);

    public final NumberPath<Long> raschsvPersSvStrahLicId = createNumber("raschsvPersSvStrahLicId", Long.class);

    public final NumberPath<java.math.BigDecimal> vyplSvVs3 = createNumber("vyplSvVs3", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvVyplSvDop> raschvVyplSvDopPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvPersSvStrahLic> raschsvVyplSvDopLicFk = createForeignKey(raschsvPersSvStrahLicId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplSvDopMt> _raschsvVsvDopMtVsvDopFk = createInvForeignKey(id, "RASCHSV_VYPL_SV_DOP_ID");

    public QRaschsvVyplSvDop(String variable) {
        super(QRaschsvVyplSvDop.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_VYPL_SV_DOP");
        addMetadata();
    }

    public QRaschsvVyplSvDop(String variable, String schema, String table) {
        super(QRaschsvVyplSvDop.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvVyplSvDop(Path<? extends QRaschsvVyplSvDop> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_VYPL_SV_DOP");
        addMetadata();
    }

    public QRaschsvVyplSvDop(PathMetadata metadata) {
        super(QRaschsvVyplSvDop.class, metadata, "NDFL_UNSTABLE", "RASCHSV_VYPL_SV_DOP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(nachislSvVs3, ColumnMetadata.named("NACHISL_SV_VS3").withIndex(3).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(raschsvPersSvStrahLicId, ColumnMetadata.named("RASCHSV_PERS_SV_STRAH_LIC_ID").withIndex(4).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(vyplSvVs3, ColumnMetadata.named("VYPL_SV_VS3").withIndex(2).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

