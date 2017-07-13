package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRoleEvent is a Querydsl query type for QRoleEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRoleEvent extends com.querydsl.sql.RelationalPathBase<QRoleEvent> {

    private static final long serialVersionUID = 950001479;

    public static final QRoleEvent roleEvent = new QRoleEvent("ROLE_EVENT");

    public final NumberPath<Integer> eventId = createNumber("eventId", Integer.class);

    public final NumberPath<Integer> roleId = createNumber("roleId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QRoleEvent> roleEventPk = createPrimaryKey(eventId, roleId);

    public final com.querydsl.sql.ForeignKey<QEvent> roleEventFkEventId = createForeignKey(eventId, "ID");

    public final com.querydsl.sql.ForeignKey<QSecRole> roleEventFkRoleId = createForeignKey(roleId, "ID");

    public QRoleEvent(String variable) {
        super(QRoleEvent.class, forVariable(variable), "NDFL_UNSTABLE", "ROLE_EVENT");
        addMetadata();
    }

    public QRoleEvent(String variable, String schema, String table) {
        super(QRoleEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRoleEvent(Path<? extends QRoleEvent> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "ROLE_EVENT");
        addMetadata();
    }

    public QRoleEvent(PathMetadata metadata) {
        super(QRoleEvent.class, metadata, "NDFL_UNSTABLE", "ROLE_EVENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(eventId, ColumnMetadata.named("EVENT_ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(roleId, ColumnMetadata.named("ROLE_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

