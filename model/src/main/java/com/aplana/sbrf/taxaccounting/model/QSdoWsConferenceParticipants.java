package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoWsConferenceParticipants is a Querydsl query type for QSdoWsConferenceParticipants
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoWsConferenceParticipants extends com.querydsl.sql.RelationalPathBase<QSdoWsConferenceParticipants> {

    private static final long serialVersionUID = 787710969;

    public static final QSdoWsConferenceParticipants sdoWsConferenceParticipants = new QSdoWsConferenceParticipants("SDO_WS_CONFERENCE_PARTICIPANTS");

    public final StringPath conferenceId = createString("conferenceId");

    public final NumberPath<java.math.BigInteger> hasApproved = createNumber("hasApproved", java.math.BigInteger.class);

    public final StringPath participant = createString("participant");

    public final com.querydsl.sql.PrimaryKey<QSdoWsConferenceParticipants> sdoWsConfPartPk = createPrimaryKey(conferenceId, participant);

    public final com.querydsl.sql.ForeignKey<QSdoWsConference> sdoWsConfPartFk = createForeignKey(conferenceId, "CONFERENCE_ID");

    public QSdoWsConferenceParticipants(String variable) {
        super(QSdoWsConferenceParticipants.class, forVariable(variable), "MDSYS", "SDO_WS_CONFERENCE_PARTICIPANTS");
        addMetadata();
    }

    public QSdoWsConferenceParticipants(String variable, String schema, String table) {
        super(QSdoWsConferenceParticipants.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoWsConferenceParticipants(Path<? extends QSdoWsConferenceParticipants> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_WS_CONFERENCE_PARTICIPANTS");
        addMetadata();
    }

    public QSdoWsConferenceParticipants(PathMetadata metadata) {
        super(QSdoWsConferenceParticipants.class, metadata, "MDSYS", "SDO_WS_CONFERENCE_PARTICIPANTS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(conferenceId, ColumnMetadata.named("CONFERENCE_ID").withIndex(1).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(hasApproved, ColumnMetadata.named("HAS_APPROVED").withIndex(3).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(participant, ColumnMetadata.named("PARTICIPANT").withIndex(2).ofType(Types.VARCHAR).withSize(128).notNull());
    }

}

