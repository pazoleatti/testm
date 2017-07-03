package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoProjectionsOldSnapshot is a Querydsl query type for QSdoProjectionsOldSnapshot
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoProjectionsOldSnapshot extends com.querydsl.sql.RelationalPathBase<QSdoProjectionsOldSnapshot> {

    private static final long serialVersionUID = -1950961208;

    public static final QSdoProjectionsOldSnapshot sdoProjectionsOldSnapshot = new QSdoProjectionsOldSnapshot("SDO_PROJECTIONS_OLD_SNAPSHOT");

    public final StringPath name = createString("name");

    public QSdoProjectionsOldSnapshot(String variable) {
        super(QSdoProjectionsOldSnapshot.class, forVariable(variable), "MDSYS", "SDO_PROJECTIONS_OLD_SNAPSHOT");
        addMetadata();
    }

    public QSdoProjectionsOldSnapshot(String variable, String schema, String table) {
        super(QSdoProjectionsOldSnapshot.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoProjectionsOldSnapshot(Path<? extends QSdoProjectionsOldSnapshot> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_PROJECTIONS_OLD_SNAPSHOT");
        addMetadata();
    }

    public QSdoProjectionsOldSnapshot(PathMetadata metadata) {
        super(QSdoProjectionsOldSnapshot.class, metadata, "MDSYS", "SDO_PROJECTIONS_OLD_SNAPSHOT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(1).ofType(Types.VARCHAR).withSize(64));
    }

}

