package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvKolLicTip is a Querydsl query type for QRaschsvKolLicTip
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvKolLicTip extends com.querydsl.sql.RelationalPathBase<QRaschsvKolLicTip> {

    private static final long serialVersionUID = 362253998;

    public static final QRaschsvKolLicTip raschsvKolLicTip = new QRaschsvKolLicTip("RASCHSV_KOL_LIC_TIP");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> kol1mPosl3m = createNumber("kol1mPosl3m", Integer.class);

    public final NumberPath<Integer> kol2mPosl3m = createNumber("kol2mPosl3m", Integer.class);

    public final NumberPath<Integer> kol3mPosl3m = createNumber("kol3mPosl3m", Integer.class);

    public final NumberPath<Integer> kolVsegoPer = createNumber("kolVsegoPer", Integer.class);

    public final NumberPath<Integer> kolVsegoPosl3m = createNumber("kolVsegoPosl3m", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvKolLicTip> raschKolLicTipPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRaschsvOssVnmKol> _raschsvOssVnmKolTipFk = createInvForeignKey(id, "RASCHSV_KOL_LIC_TIP_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOpsOmsRaschKol> _raschsvSvPMKolTipFk = createInvForeignKey(id, "RASCHSV_KOL_LIC_TIP_ID");

    public QRaschsvKolLicTip(String variable) {
        super(QRaschsvKolLicTip.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_KOL_LIC_TIP");
        addMetadata();
    }

    public QRaschsvKolLicTip(String variable, String schema, String table) {
        super(QRaschsvKolLicTip.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvKolLicTip(Path<? extends QRaschsvKolLicTip> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_KOL_LIC_TIP");
        addMetadata();
    }

    public QRaschsvKolLicTip(PathMetadata metadata) {
        super(QRaschsvKolLicTip.class, metadata, "NDFL_UNSTABLE", "RASCHSV_KOL_LIC_TIP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(kol1mPosl3m, ColumnMetadata.named("KOL_1M_POSL_3M").withIndex(4).ofType(Types.DECIMAL).withSize(7));
        addMetadata(kol2mPosl3m, ColumnMetadata.named("KOL_2M_POSL_3M").withIndex(5).ofType(Types.DECIMAL).withSize(7));
        addMetadata(kol3mPosl3m, ColumnMetadata.named("KOL_3M_POSL_3M").withIndex(6).ofType(Types.DECIMAL).withSize(7));
        addMetadata(kolVsegoPer, ColumnMetadata.named("KOL_VSEGO_PER").withIndex(2).ofType(Types.DECIMAL).withSize(7));
        addMetadata(kolVsegoPosl3m, ColumnMetadata.named("KOL_VSEGO_POSL_3M").withIndex(3).ofType(Types.DECIMAL).withSize(7));
    }

}

