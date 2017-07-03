package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoWfsLocalTxns is a Querydsl query type for QSdoWfsLocalTxns
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoWfsLocalTxns extends com.querydsl.sql.RelationalPathBase<QSdoWfsLocalTxns> {

    private static final long serialVersionUID = -1373375791;

    public static final QSdoWfsLocalTxns sdoWfsLocalTxns = new QSdoWfsLocalTxns("SDO_WFS_LOCAL_TXNS");

    public final StringPath sessionid = createString("sessionid");

    public QSdoWfsLocalTxns(String variable) {
        super(QSdoWfsLocalTxns.class, forVariable(variable), "MDSYS", "SDO_WFS_LOCAL_TXNS");
        addMetadata();
    }

    public QSdoWfsLocalTxns(String variable, String schema, String table) {
        super(QSdoWfsLocalTxns.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoWfsLocalTxns(Path<? extends QSdoWfsLocalTxns> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_WFS_LOCAL_TXNS");
        addMetadata();
    }

    public QSdoWfsLocalTxns(PathMetadata metadata) {
        super(QSdoWfsLocalTxns.class, metadata, "MDSYS", "SDO_WFS_LOCAL_TXNS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sessionid, ColumnMetadata.named("SESSIONID").withIndex(1).ofType(Types.VARCHAR).withSize(30));
    }

}

