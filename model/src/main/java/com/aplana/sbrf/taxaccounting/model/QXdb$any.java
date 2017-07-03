package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$any is a Querydsl query type for QXdb$any
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$any extends com.querydsl.sql.RelationalPathBase<QXdb$any> {

    private static final long serialVersionUID = -273792127;

    public static final QXdb$any xdb$any = new QXdb$any("XDB$ANY");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$any(String variable) {
        super(QXdb$any.class, forVariable(variable), "XDB", "XDB$ANY");
        addMetadata();
    }

    public QXdb$any(String variable, String schema, String table) {
        super(QXdb$any.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$any(Path<? extends QXdb$any> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$ANY");
        addMetadata();
    }

    public QXdb$any(PathMetadata metadata) {
        super(QXdb$any.class, metadata, "XDB", "XDB$ANY");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

