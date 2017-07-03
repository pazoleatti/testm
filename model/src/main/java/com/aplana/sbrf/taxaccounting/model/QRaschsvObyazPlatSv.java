package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvObyazPlatSv is a Querydsl query type for QRaschsvObyazPlatSv
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvObyazPlatSv extends com.querydsl.sql.RelationalPathBase<QRaschsvObyazPlatSv> {

    private static final long serialVersionUID = 1793425186;

    public static final QRaschsvObyazPlatSv raschsvObyazPlatSv = new QRaschsvObyazPlatSv("RASCHSV_OBYAZ_PLAT_SV");

    public final NumberPath<Long> declarationDataId = createNumber("declarationDataId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath oktmo = createString("oktmo");

    public final com.querydsl.sql.PrimaryKey<QRaschsvObyazPlatSv> raschsvObyazPlatSvPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationData> raschsvObyazPlatDeclaratFk = createForeignKey(declarationDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif22425> _raschsvTarif2425ObPlFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvPravTarif31427> _raschsvTarif3427ObPlFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvOssVnm> _raschsvOssVnmObPlatSvFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplFinFb> _raschsvVyplFinFbObPlFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvPravTarif71427> _raschsvTarif7427ObPlFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvUplPrevOss> _raschsvUplPrevObPlatFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvRashOssZak> _raschsvOssZakObPlatSvFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvUplPer> _raschsvUplPerObPlatSvFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif13422> _raschsvTarif13422ObPlFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvOpsOms> _raschSvOpsOmsObPlatSvFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvPrimTarif91427> _raschsvTarif9427Fk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvPravTarif51427> _raschsvTarif5427ObPlFk = createInvForeignKey(id, "RASCHSV_OBYAZ_PLAT_SV_ID");

    public QRaschsvObyazPlatSv(String variable) {
        super(QRaschsvObyazPlatSv.class, forVariable(variable), "NDFL_1_0", "RASCHSV_OBYAZ_PLAT_SV");
        addMetadata();
    }

    public QRaschsvObyazPlatSv(String variable, String schema, String table) {
        super(QRaschsvObyazPlatSv.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvObyazPlatSv(Path<? extends QRaschsvObyazPlatSv> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "RASCHSV_OBYAZ_PLAT_SV");
        addMetadata();
    }

    public QRaschsvObyazPlatSv(PathMetadata metadata) {
        super(QRaschsvObyazPlatSv.class, metadata, "NDFL_1_0", "RASCHSV_OBYAZ_PLAT_SV");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(oktmo, ColumnMetadata.named("OKTMO").withIndex(2).ofType(Types.VARCHAR).withSize(11));
    }

}

