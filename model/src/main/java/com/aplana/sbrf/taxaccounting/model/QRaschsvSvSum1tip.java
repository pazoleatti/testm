package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvSum1tip is a Querydsl query type for QRaschsvSvSum1tip
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvSum1tip extends com.querydsl.sql.RelationalPathBase<QRaschsvSvSum1tip> {

    private static final long serialVersionUID = -1434434429;

    public static final QRaschsvSvSum1tip raschsvSvSum1tip = new QRaschsvSvSum1tip("RASCHSV_SV_SUM_1TIP");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> sum1mPosl3m = createNumber("sum1mPosl3m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sum2mPosl3m = createNumber("sum2mPosl3m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sum3mPosl3m = createNumber("sum3mPosl3m", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumVsegoPer = createNumber("sumVsegoPer", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> sumVsegoPosl3m = createNumber("sumVsegoPosl3m", java.math.BigDecimal.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvSum1tip> raschSvSum1TipPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvOssVnmSum> _raschsvOssVnmSumTipFk = createInvForeignKey(id, "RASCHSV_SV_SUM1_TIP_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplatIt427> _raschsvVyplatIt427SumFk = createInvForeignKey(id, "RASCHSV_SV_SUM1_TIP_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplatIt422> _raschsvVyplatIt422SumFk = createInvForeignKey(id, "RASCHSV_SV_SUM1_TIP_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOpsOmsRaschSum> _raschsvOpsOmsRSumTipFk = createInvForeignKey(id, "RASCHSV_SV_SUM1_TIP_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvInoGrazd> _raschsvSvInoGrazdSumFk = createInvForeignKey(id, "RASCHSV_SV_SUM1_TIP_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvedObuch> _raschsvSvedObuchSumFk = createInvForeignKey(id, "RASCHSV_SV_SUM1_TIP_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvedPatent> _raschsvSvedPatentSumFk = createInvForeignKey(id, "RASCHSV_SV_SUM1_TIP_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplatIt425> _raschsvVyplatIt425SumFk = createInvForeignKey(id, "RASCHSV_SV_SUM1_TIP_ID");

    public QRaschsvSvSum1tip(String variable) {
        super(QRaschsvSvSum1tip.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_SV_SUM_1TIP");
        addMetadata();
    }

    public QRaschsvSvSum1tip(String variable, String schema, String table) {
        super(QRaschsvSvSum1tip.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvSum1tip(Path<? extends QRaschsvSvSum1tip> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_SV_SUM_1TIP");
        addMetadata();
    }

    public QRaschsvSvSum1tip(PathMetadata metadata) {
        super(QRaschsvSvSum1tip.class, metadata, "NDFL_UNSTABLE", "RASCHSV_SV_SUM_1TIP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(sum1mPosl3m, ColumnMetadata.named("SUM_1M_POSL_3M").withIndex(4).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sum2mPosl3m, ColumnMetadata.named("SUM_2M_POSL_3M").withIndex(5).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sum3mPosl3m, ColumnMetadata.named("SUM_3M_POSL_3M").withIndex(6).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sumVsegoPer, ColumnMetadata.named("SUM_VSEGO_PER").withIndex(2).ofType(Types.DECIMAL).withSize(19).withDigits(2));
        addMetadata(sumVsegoPosl3m, ColumnMetadata.named("SUM_VSEGO_POSL_3M").withIndex(3).ofType(Types.DECIMAL).withSize(19).withDigits(2));
    }

}

