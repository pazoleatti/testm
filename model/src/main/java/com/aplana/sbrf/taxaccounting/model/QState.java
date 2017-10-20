package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QState is a Querydsl query type for QState
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QState extends com.querydsl.sql.RelationalPathBase<QState> {

    private static final long serialVersionUID = 1103954324;

    public static final QState state = new QState("STATE");

    public final NumberPath<Byte> id = createNumber("id", Byte.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QState> statePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationData> _declarationDataStateFk = createInvForeignKey(id, "STATE");

    public QState(String variable) {
        super(QState.class, forVariable(variable), "NDFL_UNSTABLE", "STATE");
        addMetadata();
    }

    public QState(String variable, String schema, String table) {
        super(QState.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QState(Path<? extends QState> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "STATE");
        addMetadata();
    }

    public QState(PathMetadata metadata) {
        super(QState.class, metadata, "NDFL_UNSTABLE", "STATE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(20));
    }

}

