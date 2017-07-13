package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QStateChange is a Querydsl query type for QStateChange
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QStateChange extends com.querydsl.sql.RelationalPathBase<QStateChange> {

    private static final long serialVersionUID = -409042332;

    public static final QStateChange stateChange = new QStateChange("STATE_CHANGE");

    public final NumberPath<Byte> fromId = createNumber("fromId", Byte.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Byte> toId = createNumber("toId", Byte.class);

    public final com.querydsl.sql.PrimaryKey<QStateChange> stateChangePk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QState> stateChangeFromFk = createForeignKey(fromId, "ID");

    public final com.querydsl.sql.ForeignKey<QState> stateChangeToFk = createForeignKey(toId, "ID");

    public QStateChange(String variable) {
        super(QStateChange.class, forVariable(variable), "NDFL_UNSTABLE", "STATE_CHANGE");
        addMetadata();
    }

    public QStateChange(String variable, String schema, String table) {
        super(QStateChange.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QStateChange(Path<? extends QStateChange> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "STATE_CHANGE");
        addMetadata();
    }

    public QStateChange(PathMetadata metadata) {
        super(QStateChange.class, metadata, "NDFL_UNSTABLE", "STATE_CHANGE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(fromId, ColumnMetadata.named("FROM_ID").withIndex(2).ofType(Types.DECIMAL).withSize(1));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(toId, ColumnMetadata.named("TO_ID").withIndex(3).ofType(Types.DECIMAL).withSize(1).notNull());
    }

}

