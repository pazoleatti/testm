package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QXdb$xidxImpT is a Querydsl query type for QXdb$xidxImpT
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QXdb$xidxImpT extends com.querydsl.sql.RelationalPathBase<QXdb$xidxImpT> {

    private static final long serialVersionUID = 227064440;

    public static final QXdb$xidxImpT xdb$xidxImpT = new QXdb$xidxImpT("XDB$XIDX_IMP_T");

    public final StringPath data = createString("data");

    public final NumberPath<java.math.BigInteger> grppos = createNumber("grppos", java.math.BigInteger.class);

    public final StringPath id = createString("id");

    public final StringPath indexName = createString("indexName");

    public final StringPath schemaName = createString("schemaName");

    public QXdb$xidxImpT(String variable) {
        super(QXdb$xidxImpT.class, forVariable(variable), "XDB", "XDB$XIDX_IMP_T");
        addMetadata();
    }

    public QXdb$xidxImpT(String variable, String schema, String table) {
        super(QXdb$xidxImpT.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QXdb$xidxImpT(Path<? extends QXdb$xidxImpT> path) {
        super(path.getType(), path.getMetadata(), "XDB", "XDB$XIDX_IMP_T");
        addMetadata();
    }

    public QXdb$xidxImpT(PathMetadata metadata) {
        super(QXdb$xidxImpT.class, metadata, "XDB", "XDB$XIDX_IMP_T");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(data, ColumnMetadata.named("DATA").withIndex(4).ofType(Types.CLOB).withSize(4000));
        addMetadata(grppos, ColumnMetadata.named("GRPPOS").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(3).ofType(Types.VARCHAR).withSize(40));
        addMetadata(indexName, ColumnMetadata.named("INDEX_NAME").withIndex(1).ofType(Types.VARCHAR).withSize(40));
        addMetadata(schemaName, ColumnMetadata.named("SCHEMA_NAME").withIndex(2).ofType(Types.VARCHAR).withSize(40));
    }

}

