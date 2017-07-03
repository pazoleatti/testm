package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoCoordSys is a Querydsl query type for QSdoCoordSys
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoCoordSys extends com.querydsl.sql.RelationalPathBase<QSdoCoordSys> {

    private static final long serialVersionUID = 961371321;

    public static final QSdoCoordSys sdoCoordSys = new QSdoCoordSys("SDO_COORD_SYS");

    public final NumberPath<Long> coordSysId = createNumber("coordSysId", Long.class);

    public final StringPath coordSysName = createString("coordSysName");

    public final StringPath coordSysType = createString("coordSysType");

    public final StringPath dataSource = createString("dataSource");

    public final NumberPath<Integer> dimension = createNumber("dimension", Integer.class);

    public final StringPath informationSource = createString("informationSource");

    public final com.querydsl.sql.PrimaryKey<QSdoCoordSys> coordinateSystemPrim = createPrimaryKey(coordSysId);

    public final com.querydsl.sql.ForeignKey<QSdoCoordRefSys> _coordRefSysForeignCs = createInvForeignKey(coordSysId, "COORD_SYS_ID");

    public final com.querydsl.sql.ForeignKey<QSdoCoordAxes> _coordAxisForeignCs = createInvForeignKey(coordSysId, "COORD_SYS_ID");

    public QSdoCoordSys(String variable) {
        super(QSdoCoordSys.class, forVariable(variable), "MDSYS", "SDO_COORD_SYS");
        addMetadata();
    }

    public QSdoCoordSys(String variable, String schema, String table) {
        super(QSdoCoordSys.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoCoordSys(Path<? extends QSdoCoordSys> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_COORD_SYS");
        addMetadata();
    }

    public QSdoCoordSys(PathMetadata metadata) {
        super(QSdoCoordSys.class, metadata, "MDSYS", "SDO_COORD_SYS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(coordSysId, ColumnMetadata.named("COORD_SYS_ID").withIndex(1).ofType(Types.DECIMAL).withSize(10).notNull());
        addMetadata(coordSysName, ColumnMetadata.named("COORD_SYS_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(254).notNull());
        addMetadata(coordSysType, ColumnMetadata.named("COORD_SYS_TYPE").withIndex(3).ofType(Types.VARCHAR).withSize(24));
        addMetadata(dataSource, ColumnMetadata.named("DATA_SOURCE").withIndex(6).ofType(Types.VARCHAR).withSize(50));
        addMetadata(dimension, ColumnMetadata.named("DIMENSION").withIndex(4).ofType(Types.DECIMAL).withSize(5));
        addMetadata(informationSource, ColumnMetadata.named("INFORMATION_SOURCE").withIndex(5).ofType(Types.VARCHAR).withSize(254));
    }

}

