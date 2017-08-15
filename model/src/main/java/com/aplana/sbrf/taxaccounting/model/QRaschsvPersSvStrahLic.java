package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRaschsvPersSvStrahLic is a Querydsl query type for QRaschsvPersSvStrahLic
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRaschsvPersSvStrahLic extends com.querydsl.sql.RelationalPathBase<QRaschsvPersSvStrahLic> {

    private static final long serialVersionUID = 684533302;

    public static final QRaschsvPersSvStrahLic raschsvPersSvStrahLic = new QRaschsvPersSvStrahLic("RASCHSV_PERS_SV_STRAH_LIC");

    public final DateTimePath<org.joda.time.LocalDateTime> dataRozd = createDateTime("dataRozd", org.joda.time.LocalDateTime.class);

    public final NumberPath<Long> declarationDataId = createNumber("declarationDataId", Long.class);

    public final StringPath familia = createString("familia");

    public final StringPath grazd = createString("grazd");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imya = createString("imya");

    public final StringPath innfl = createString("innfl");

    public final StringPath kodVidDoc = createString("kodVidDoc");

    public final NumberPath<Integer> nomer = createNumber("nomer", Integer.class);

    public final NumberPath<Short> nomKorr = createNumber("nomKorr", Short.class);

    public final StringPath otchestvo = createString("otchestvo");

    public final StringPath otchetGod = createString("otchetGod");

    public final StringPath period = createString("period");

    public final NumberPath<Long> personId = createNumber("personId", Long.class);

    public final StringPath pol = createString("pol");

    public final StringPath prizOms = createString("prizOms");

    public final StringPath prizOps = createString("prizOps");

    public final StringPath prizOss = createString("prizOss");

    public final StringPath serNomDoc = createString("serNomDoc");

    public final StringPath snils = createString("snils");

    public final DateTimePath<org.joda.time.LocalDateTime> svData = createDateTime("svData", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRaschsvPersSvStrahLic> persSvStrahFacePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookPerson> rsvPSvStrahLicPersonFk = createForeignKey(personId, "ID");

    public final com.querydsl.sql.ForeignKey<QDeclarationData> persSvStrahFaceDeclaratFk = createForeignKey(declarationDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvVyplSvDop> _raschsvVyplSvDopLicFk = createInvForeignKey(id, "RASCHSV_PERS_SV_STRAH_LIC_ID");

    public final com.querydsl.sql.ForeignKey<QRaschsvSvVypl> _raschsvSvVyplStrahLicFk = createInvForeignKey(id, "RASCHSV_PERS_SV_STRAH_LIC_ID");

    public QRaschsvPersSvStrahLic(String variable) {
        super(QRaschsvPersSvStrahLic.class, forVariable(variable), "NDFL_UNSTABLE", "RASCHSV_PERS_SV_STRAH_LIC");
        addMetadata();
    }

    public QRaschsvPersSvStrahLic(String variable, String schema, String table) {
        super(QRaschsvPersSvStrahLic.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRaschsvPersSvStrahLic(Path<? extends QRaschsvPersSvStrahLic> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "RASCHSV_PERS_SV_STRAH_LIC");
        addMetadata();
    }

    public QRaschsvPersSvStrahLic(PathMetadata metadata) {
        super(QRaschsvPersSvStrahLic.class, metadata, "NDFL_UNSTABLE", "RASCHSV_PERS_SV_STRAH_LIC");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dataRozd, ColumnMetadata.named("DATA_ROZD").withIndex(8).ofType(Types.TIMESTAMP).withSize(7));
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(19).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(familia, ColumnMetadata.named("FAMILIA").withIndex(16).ofType(Types.VARCHAR).withSize(60));
        addMetadata(grazd, ColumnMetadata.named("GRAZD").withIndex(9).ofType(Types.VARCHAR).withSize(3));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(imya, ColumnMetadata.named("IMYA").withIndex(17).ofType(Types.VARCHAR).withSize(60));
        addMetadata(innfl, ColumnMetadata.named("INNFL").withIndex(6).ofType(Types.VARCHAR).withSize(12));
        addMetadata(kodVidDoc, ColumnMetadata.named("KOD_VID_DOC").withIndex(11).ofType(Types.VARCHAR).withSize(2));
        addMetadata(nomer, ColumnMetadata.named("NOMER").withIndex(4).ofType(Types.DECIMAL).withSize(7));
        addMetadata(nomKorr, ColumnMetadata.named("NOM_KORR").withIndex(20).ofType(Types.DECIMAL).withSize(3));
        addMetadata(otchestvo, ColumnMetadata.named("OTCHESTVO").withIndex(18).ofType(Types.VARCHAR).withSize(60));
        addMetadata(otchetGod, ColumnMetadata.named("OTCHET_GOD").withIndex(3).ofType(Types.VARCHAR).withSize(4));
        addMetadata(period, ColumnMetadata.named("PERIOD").withIndex(2).ofType(Types.VARCHAR).withSize(2));
        addMetadata(personId, ColumnMetadata.named("PERSON_ID").withIndex(21).ofType(Types.DECIMAL).withSize(18));
        addMetadata(pol, ColumnMetadata.named("POL").withIndex(10).ofType(Types.VARCHAR).withSize(1));
        addMetadata(prizOms, ColumnMetadata.named("PRIZ_OMS").withIndex(14).ofType(Types.VARCHAR).withSize(1));
        addMetadata(prizOps, ColumnMetadata.named("PRIZ_OPS").withIndex(13).ofType(Types.VARCHAR).withSize(1));
        addMetadata(prizOss, ColumnMetadata.named("PRIZ_OSS").withIndex(15).ofType(Types.VARCHAR).withSize(1));
        addMetadata(serNomDoc, ColumnMetadata.named("SER_NOM_DOC").withIndex(12).ofType(Types.VARCHAR).withSize(25));
        addMetadata(snils, ColumnMetadata.named("SNILS").withIndex(7).ofType(Types.VARCHAR).withSize(14));
        addMetadata(svData, ColumnMetadata.named("SV_DATA").withIndex(5).ofType(Types.TIMESTAMP).withSize(7));
    }

}

