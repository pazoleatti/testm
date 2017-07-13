package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookDocState is a Querydsl query type for QRefBookDocState
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookDocState extends com.querydsl.sql.RelationalPathBase<QRefBookDocState> {

    private static final long serialVersionUID = -1690937928;

    public static final QRefBookDocState refBookDocState = new QRefBookDocState("REF_BOOK_DOC_STATE");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath knd = createString("knd");

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QRefBookDocState> refBookDocStatePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationData> _declDataDocStateFk = createInvForeignKey(id, "DOC_STATE_ID");

    public QRefBookDocState(String variable) {
        super(QRefBookDocState.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_DOC_STATE");
        addMetadata();
    }

    public QRefBookDocState(String variable, String schema, String table) {
        super(QRefBookDocState.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookDocState(Path<? extends QRefBookDocState> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_DOC_STATE");
        addMetadata();
    }

    public QRefBookDocState(PathMetadata metadata) {
        super(QRefBookDocState.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_DOC_STATE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(knd, ColumnMetadata.named("KND").withIndex(2).ofType(Types.VARCHAR).withSize(7));
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

