package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$importTtInfo is a Querydsl query type for QXdb$importTtInfo
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$importTtInfo extends com.querydsl.sql.RelationalPathBase<QXdb$importTtInfo> {

    private static final long serialVersionUID = 1373906270;

    public static final QXdb$importTtInfo xdb$importTtInfo = new QXdb$importTtInfo("XDB$IMPORT_TT_INFO");

    public final SimplePath<byte[]> flags = createSimple("flags", byte[].class);

    public final SimplePath<byte[]> guid = createSimple("guid", byte[].class);

    public final SimplePath<byte[]> id = createSimple("id", byte[].class);

    public final StringPath localname = createString("localname");

    public final SimplePath<byte[]> nmspcid = createSimple("nmspcid", byte[].class);

    public QXdb$importTtInfo(String variable) {
        super(QXdb$importTtInfo.class, forVariable(variable), "XDB", "XDB$IMPORT_TT_INFO");
        addMetadata();
    }

    public QXdb$importTtInfo(String variable, String schema, String table) {
        super(QXdb$importTtInfo.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$importTtInfo(Path<? extends QXdb$importTtInfo> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$IMPORT_TT_INFO");
        addMetadata();
    }

    public QXdb$importTtInfo(PathMetadata metadata) {
        super(QXdb$importTtInfo.class, metadata, "XDB", "XDB$IMPORT_TT_INFO");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(flags, ColumnMetadata.named("FLAGS").withIndex(4).ofType(Types.VARBINARY).withSize(4));
        addMetadata(guid, ColumnMetadata.named("GUID").withIndex(1).ofType(Types.VARBINARY).withSize(16));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(5).ofType(Types.VARBINARY).withSize(8));
        addMetadata(localname, ColumnMetadata.named("LOCALNAME").withIndex(3).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(nmspcid, ColumnMetadata.named("NMSPCID").withIndex(2).ofType(Types.VARBINARY).withSize(8));
    }

}

