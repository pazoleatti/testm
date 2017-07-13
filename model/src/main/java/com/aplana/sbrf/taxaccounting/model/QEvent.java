package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QEvent is a Querydsl query type for QEvent
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QEvent extends com.querydsl.sql.RelationalPathBase<QEvent> {

    private static final long serialVersionUID = 1091088285;

    public static final QEvent event = new QEvent("EVENT");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final com.querydsl.sql.PrimaryKey<QEvent> eventPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QLogBusiness> _logBusinessFkEventId = createInvForeignKey(id, "EVENT_ID");

    public final com.querydsl.sql.ForeignKey<QRoleEvent> _roleEventFkEventId = createInvForeignKey(id, "EVENT_ID");

    public final com.querydsl.sql.ForeignKey<QLogSystem> _logSystemFkEventId = createInvForeignKey(id, "EVENT_ID");

    public final com.querydsl.sql.ForeignKey<QTemplateChanges> _templateChangesFkEvent = createInvForeignKey(id, "EVENT");

    public QEvent(String variable) {
        super(QEvent.class, forVariable(variable), "NDFL_UNSTABLE", "EVENT");
        addMetadata();
    }

    public QEvent(String variable, String schema, String table) {
        super(QEvent.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QEvent(Path<? extends QEvent> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "EVENT");
        addMetadata();
    }

    public QEvent(PathMetadata metadata) {
        super(QEvent.class, metadata, "NDFL_UNSTABLE", "EVENT");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(510).notNull());
    }

}

