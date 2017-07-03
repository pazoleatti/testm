package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoWsConference is a Querydsl query type for QSdoWsConference
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoWsConference extends com.querydsl.sql.RelationalPathBase<QSdoWsConference> {

    private static final long serialVersionUID = -212335175;

    public static final QSdoWsConference sdoWsConference = new QSdoWsConference("SDO_WS_CONFERENCE");

    public final StringPath conferenceId = createString("conferenceId");

    public final SimplePath<Object> request = createSimple("request", Object.class);

    public final com.querydsl.sql.PrimaryKey<QSdoWsConference> sdoWsConfPk = createPrimaryKey(conferenceId);

    public final com.querydsl.sql.ForeignKey<QSdoWsConferenceParticipants> _sdoWsConfPartFk = createInvForeignKey(conferenceId, "CONFERENCE_ID");

    public QSdoWsConference(String variable) {
        super(QSdoWsConference.class, forVariable(variable), "MDSYS", "SDO_WS_CONFERENCE");
        addMetadata();
    }

    public QSdoWsConference(String variable, String schema, String table) {
        super(QSdoWsConference.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoWsConference(Path<? extends QSdoWsConference> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_WS_CONFERENCE");
        addMetadata();
    }

    public QSdoWsConference(PathMetadata metadata) {
        super(QSdoWsConference.class, metadata, "MDSYS", "SDO_WS_CONFERENCE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(conferenceId, ColumnMetadata.named("CONFERENCE_ID").withIndex(1).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(request, ColumnMetadata.named("REQUEST").withIndex(2).ofType(2007).withSize(2000).notNull());
    }

}

