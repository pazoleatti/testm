package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$complexType is a Querydsl query type for QXdb$complexType
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$complexType extends com.querydsl.sql.RelationalPathBase<QXdb$complexType> {

    private static final long serialVersionUID = -1563036513;

    public static final QXdb$complexType xdb$complexType = new QXdb$complexType("XDB$COMPLEX_TYPE");

    public final SimplePath<Object> sysNcRowinfo$ = createSimple("sysNcRowinfo$", Object.class);

    public QXdb$complexType(String variable) {
        super(QXdb$complexType.class, forVariable(variable), "XDB", "XDB$COMPLEX_TYPE");
        addMetadata();
    }

    public QXdb$complexType(String variable, String schema, String table) {
        super(QXdb$complexType.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$complexType(Path<? extends QXdb$complexType> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$COMPLEX_TYPE");
        addMetadata();
    }

    public QXdb$complexType(PathMetadata metadata) {
        super(QXdb$complexType.class, metadata, "XDB", "XDB$COMPLEX_TYPE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(sysNcRowinfo$, ColumnMetadata.named("SYS_NC_ROWINFO$").withIndex(1).ofType(2007).withSize(2000));
    }

}

