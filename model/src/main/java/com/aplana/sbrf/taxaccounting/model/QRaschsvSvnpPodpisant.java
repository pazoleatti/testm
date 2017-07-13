package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvSvnpPodpisant is a Querydsl query type for QRaschsvSvnpPodpisant
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvSvnpPodpisant extends com.querydsl.sql.RelationalPathBase<QRaschsvSvnpPodpisant> {

    private static final long serialVersionUID = 952995582;

    public static final QRaschsvSvnpPodpisant raschsvSvnpPodpisant = new QRaschsvSvnpPodpisant("RASCHSV_SVNP_PODPISANT");

    public final NumberPath<Long> declarationDataId = createNumber("declarationDataId", Long.class);

    public final StringPath familia = createString("familia");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imya = createString("imya");

    public final NumberPath<Integer> nomKorr = createNumber("nomKorr", Integer.class);

    public final StringPath otchestvo = createString("otchestvo");

    public final StringPath podpisantNaimDoc = createString("podpisantNaimDoc");

    public final StringPath podpisantNaimOrg = createString("podpisantNaimOrg");

    public final StringPath podpisantPrPodp = createString("podpisantPrPodp");

    public final StringPath svnpInnyl = createString("svnpInnyl");

    public final StringPath svnpKpp = createString("svnpKpp");

    public final StringPath svnpNaimOrg = createString("svnpNaimOrg");

    public final StringPath svnpOkved = createString("svnpOkved");

    public final StringPath svnpSvReorgForm = createString("svnpSvReorgForm");

    public final StringPath svnpSvReorgInnyl = createString("svnpSvReorgInnyl");

    public final StringPath svnpSvReorgKpp = createString("svnpSvReorgKpp");

    public final StringPath svnpTlph = createString("svnpTlph");

    public final com.querydsl.sql.PrimaryKey<QRaschsvSvnpPodpisant> raschsvSvnpPodpisantPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationData> raschsvSvnpPodpDeclaratFk = createForeignKey(declarationDataId, "ID");

    public QRaschsvSvnpPodpisant(String variable) {
        super(QRaschsvSvnpPodpisant.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_SVNP_PODPISANT");
        addMetadata();
    }

    public QRaschsvSvnpPodpisant(String variable, String schema, String table) {
        super(QRaschsvSvnpPodpisant.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvSvnpPodpisant(Path<? extends QRaschsvSvnpPodpisant> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_SVNP_PODPISANT");
        addMetadata();
    }

    public QRaschsvSvnpPodpisant(PathMetadata metadata) {
        super(QRaschsvSvnpPodpisant.class, metadata, "NDFL_UNSTABLE", "RASCHSV_SVNP_PODPISANT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(2).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(familia, ColumnMetadata.named("FAMILIA").withIndex(11).ofType(Types.VARCHAR).withSize(60));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(imya, ColumnMetadata.named("IMYA").withIndex(12).ofType(Types.VARCHAR).withSize(60));
        addMetadata(nomKorr, ColumnMetadata.named("NOM_KORR").withIndex(17).ofType(Types.DECIMAL).withSize(7));
        addMetadata(otchestvo, ColumnMetadata.named("OTCHESTVO").withIndex(13).ofType(Types.VARCHAR).withSize(60));
        addMetadata(podpisantNaimDoc, ColumnMetadata.named("PODPISANT_NAIM_DOC").withIndex(15).ofType(Types.VARCHAR).withSize(120));
        addMetadata(podpisantNaimOrg, ColumnMetadata.named("PODPISANT_NAIM_ORG").withIndex(16).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(podpisantPrPodp, ColumnMetadata.named("PODPISANT_PR_PODP").withIndex(14).ofType(Types.VARCHAR).withSize(1));
        addMetadata(svnpInnyl, ColumnMetadata.named("SVNP_INNYL").withIndex(6).ofType(Types.VARCHAR).withSize(10));
        addMetadata(svnpKpp, ColumnMetadata.named("SVNP_KPP").withIndex(7).ofType(Types.VARCHAR).withSize(9));
        addMetadata(svnpNaimOrg, ColumnMetadata.named("SVNP_NAIM_ORG").withIndex(5).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(svnpOkved, ColumnMetadata.named("SVNP_OKVED").withIndex(3).ofType(Types.VARCHAR).withSize(8));
        addMetadata(svnpSvReorgForm, ColumnMetadata.named("SVNP_SV_REORG_FORM").withIndex(8).ofType(Types.VARCHAR).withSize(1));
        addMetadata(svnpSvReorgInnyl, ColumnMetadata.named("SVNP_SV_REORG_INNYL").withIndex(9).ofType(Types.VARCHAR).withSize(10));
        addMetadata(svnpSvReorgKpp, ColumnMetadata.named("SVNP_SV_REORG_KPP").withIndex(10).ofType(Types.VARCHAR).withSize(9));
        addMetadata(svnpTlph, ColumnMetadata.named("SVNP_TLPH").withIndex(4).ofType(Types.VARCHAR).withSize(20));
    }

}

