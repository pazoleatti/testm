package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$attrgroupRef is a Querydsl query type for QXdb$attrgroupRef
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$attrgroupRef extends com.querydsl.sql.RelationalPathBase<QXdb$attrgroupRef> {

    private static final long serialVersionUID = 37997712;

    public static final QXdb$attrgroupRef xdb$attrgroupRef = new QXdb$attrgroupRef("XDB$ATTRGROUP_REF");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$attrgroupRef(String variable) {
        super(QXdb$attrgroupRef.class, forVariable(variable), "XDB", "XDB$ATTRGROUP_REF");
        addMetadata();
    }

    public QXdb$attrgroupRef(String variable, String schema, String table) {
        super(QXdb$attrgroupRef.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$attrgroupRef(Path<? extends QXdb$attrgroupRef> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$ATTRGROUP_REF");
        addMetadata();
    }

    public QXdb$attrgroupRef(PathMetadata metadata) {
        super(QXdb$attrgroupRef.class, metadata, "XDB", "XDB$ATTRGROUP_REF");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

