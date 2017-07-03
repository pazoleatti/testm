package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$attrgroupDef is a Querydsl query type for QXdb$attrgroupDef
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$attrgroupDef extends com.querydsl.sql.RelationalPathBase<QXdb$attrgroupDef> {

    private static final long serialVersionUID = 37984258;

    public static final QXdb$attrgroupDef xdb$attrgroupDef = new QXdb$attrgroupDef("XDB$ATTRGROUP_DEF");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$attrgroupDef(String variable) {
        super(QXdb$attrgroupDef.class, forVariable(variable), "XDB", "XDB$ATTRGROUP_DEF");
        addMetadata();
    }

    public QXdb$attrgroupDef(String variable, String schema, String table) {
        super(QXdb$attrgroupDef.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$attrgroupDef(Path<? extends QXdb$attrgroupDef> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$ATTRGROUP_DEF");
        addMetadata();
    }

    public QXdb$attrgroupDef(PathMetadata metadata) {
        super(QXdb$attrgroupDef.class, metadata, "XDB", "XDB$ATTRGROUP_DEF");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

