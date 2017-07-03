package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$simpleType is a Querydsl query type for QXdb$simpleType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$simpleType extends com.querydsl.sql.RelationalPathBase<QXdb$simpleType> {

    private static final long serialVersionUID = 1137328343;

    public static final QXdb$simpleType xdb$simpleType = new QXdb$simpleType("XDB$SIMPLE_TYPE");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$simpleType(String variable) {
        super(QXdb$simpleType.class, forVariable(variable), "XDB", "XDB$SIMPLE_TYPE");
        addMetadata();
    }

    public QXdb$simpleType(String variable, String schema, String table) {
        super(QXdb$simpleType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$simpleType(Path<? extends QXdb$simpleType> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$SIMPLE_TYPE");
        addMetadata();
    }

    public QXdb$simpleType(PathMetadata metadata) {
        super(QXdb$simpleType.class, metadata, "XDB", "XDB$SIMPLE_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

