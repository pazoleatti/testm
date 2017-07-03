package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$anyattr is a Querydsl query type for QXdb$anyattr
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$anyattr extends com.querydsl.sql.RelationalPathBase<QXdb$anyattr> {

    private static final long serialVersionUID = 538735858;

    public static final QXdb$anyattr xdb$anyattr = new QXdb$anyattr("XDB$ANYATTR");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$anyattr(String variable) {
        super(QXdb$anyattr.class, forVariable(variable), "XDB", "XDB$ANYATTR");
        addMetadata();
    }

    public QXdb$anyattr(String variable, String schema, String table) {
        super(QXdb$anyattr.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$anyattr(Path<? extends QXdb$anyattr> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$ANYATTR");
        addMetadata();
    }

    public QXdb$anyattr(PathMetadata metadata) {
        super(QXdb$anyattr.class, metadata, "XDB", "XDB$ANYATTR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

