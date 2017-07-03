package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordRefSys is a Querydsl query type for QSdoCoordRefSys
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordRefSys extends com.querydsl.sql.RelationalPathBase<QSdoCoordRefSys> {

    private static final long serialVersionUID = 1323690958;

    public static final QSdoCoordRefSys sdoCoordRefSys = new QSdoCoordRefSys("SDO_COORD_REF_SYS");

    public final NumberPath<Long> cmpdHorizSrid = createNumber("cmpdHorizSrid", Long.class);

    public final NumberPath<Long> cmpdVertSrid = createNumber("cmpdVertSrid", Long.class);

    public final StringPath coordRefSysKind = createString("coordRefSysKind");

    public final StringPath coordRefSysName = createString("coordRefSysName");

    public final NumberPath<Long> coordSysId = createNumber("coordSysId", Long.class);

    public final StringPath dataSource = createString("dataSource");

    public final NumberPath<Long> datumId = createNumber("datumId", Long.class);

    public final NumberPath<Long> geogCrsDatumId = createNumber("geogCrsDatumId", Long.class);

    public final StringPath informationSource = createString("informationSource");

    public final StringPath isLegacy = createString("isLegacy");

    public final StringPath isValid = createString("isValid");

    public final NumberPath<Long> legacyCode = createNumber("legacyCode", Long.class);

    public final SimplePath<Object> legacyCsBounds = createSimple("legacyCsBounds", Object.class);

    public final StringPath legacyWktext = createString("legacyWktext");

    public final NumberPath<Long> projectionConvId = createNumber("projectionConvId", Long.class);

    public final NumberPath<Long> sourceGeogSrid = createNumber("sourceGeogSrid", Long.class);

    public final NumberPath<Long> srid = createNumber("srid", Long.class);

    public final StringPath supportsSdoGeometry = createString("supportsSdoGeometry");

    public final com.querydsl.sql.PrimaryKey<QSdoCoordRefSys> coordRefSystemPrim = createPrimaryKey(srid);

    public final com.querydsl.sql.ForeignKey<QSdoCoordSys> coordRefSysForeignCs = createForeignKey(coordSysId, "COORD_SYS_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOps> coordRefSysForeignProj = createForeignKey(projectionConvId, "COORD_OP_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> coordRefSysForeignLegacy = createForeignKey(legacyCode, "SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> coordRefSysForeignGeog = createForeignKey(sourceGeogSrid, "SRID");

    public final com.querydsl.sql.ForeignKey<QSdoDatums> coordRefSysForeignDatum = createForeignKey(datumId, "DATUM_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> coordRefSysForeignVert = createForeignKey(cmpdVertSrid, "SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> coordRefSysForeignHoriz = createForeignKey(cmpdHorizSrid, "SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpPaths> _coordOpPathForeignTarget = createInvForeignKey(srid, "SINGLE_OP_TARGET_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> _coordRefSysForeignGeog = createInvForeignKey(srid, "SOURCE_GEOG_SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> _coordRefSysForeignLegacy = createInvForeignKey(srid, "LEGACY_CODE");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOps> _coordOperationForeignSource = createInvForeignKey(srid, "SOURCE_SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> _coordRefSysForeignHoriz = createInvForeignKey(srid, "CMPD_HORIZ_SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> _coordRefSysForeignVert = createInvForeignKey(srid, "CMPD_VERT_SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOps> _coordOperationForeignTarget = createInvForeignKey(srid, "TARGET_SRID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordOpPaths> _coordOpPathForeignSource = createInvForeignKey(srid, "SINGLE_OP_SOURCE_ID");

    public QSdoCoordRefSys(String variable) {
        super(QSdoCoordRefSys.class, forVariable(variable), "MDSYS", "SDO_COORD_REF_SYS");
        addMetadata();
    }

    public QSdoCoordRefSys(String variable, String schema, String table) {
        super(QSdoCoordRefSys.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordRefSys(Path<? extends QSdoCoordRefSys> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_REF_SYS");
        addMetadata();
    }

    public QSdoCoordRefSys(PathMetadata metadata) {
        super(QSdoCoordRefSys.class, metadata, "MDSYS", "SDO_COORD_REF_SYS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(cmpdHorizSrid, ColumnMetadata.named("CMPD_HORIZ_SRID").withIndex(9).ofType(Types.DECIMAL).withSize(10));
        addMetadata(cmpdVertSrid, ColumnMetadata.named("CMPD_VERT_SRID").withIndex(10).ofType(Types.DECIMAL).withSize(10));
        addMetadata(coordRefSysKind, ColumnMetadata.named("COORD_REF_SYS_KIND").withIndex(3).ofType(Types.VARCHAR).withSize(24).notNull());
        addMetadata(coordRefSysName, ColumnMetadata.named("COORD_REF_SYS_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(80).notNull());
        addMetadata(coordSysId, ColumnMetadata.named("COORD_SYS_ID").withIndex(4).ofType(Types.DECIMAL).withSize(10));
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(12).ofType(Types.VARCHAR).withSize(40));
        addMetadata(datumId, ColumnMetadata.named("DATUM_ID").withIndex(5).ofType(Types.DECIMAL).withSize(10));
        addMetadata(geogCrsDatumId, ColumnMetadata.named("GEOG_CRS_DATUM_ID").withIndex(6).ofType(Types.DECIMAL).withSize(10));
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(11).ofType(Types.VARCHAR).withSize(254));
        addMetadata(isLegacy, ColumnMetadata.named("IS_LEGACY").withIndex(13).ofType(Types.VARCHAR).withSize(5).notNull());
        addMetadata(isValid, ColumnMetadata.named("IS_VALID").withIndex(17).ofType(Types.VARCHAR).withSize(5));
        addMetadata(legacyCode, ColumnMetadata.named("LEGACY_CODE").withIndex(14).ofType(Types.DECIMAL).withSize(10));
        addMetadata(legacyCsBounds, ColumnMetadata.named("LEGACY_CS_BOUNDS").withIndex(16).ofType(Types.OTHER).withSize(1));
        addMetadata(legacyWktext, ColumnMetadata.named("LEGACY_WKTEXT").withIndex(15).ofType(Types.VARCHAR).withSize(2046));
        addMetadata(projectionConvId, ColumnMetadata.named("PROJECTION_CONV_ID").withIndex(8).ofType(Types.DECIMAL).withSize(10));
        addMetadata(sourceGeogSrid, ColumnMetadata.named("SOURCE_GEOG_SRID").withIndex(7).ofType(Types.DECIMAL).withSize(10));
        addMetadata(srid, ColumnMetadata.named("SRID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(supportsSdoGeometry, ColumnMetadata.named("SUPPORTS_SDO_GEOMETRY").withIndex(18).ofType(Types.VARCHAR).withSize(5));
    }

}

