package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSdoWsConferenceResults is a Querydsl query type for QSdoWsConferenceResults
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSdoWsConferenceResults extends com.querydsl.sql.RelationalPathBase<QSdoWsConferenceResults> {

    private static final long serialVersionUID = 904240125;

    public static final QSdoWsConferenceResults sdoWsConferenceResults = new QSdoWsConferenceResults("SDO_WS_CONFERENCE_RESULTS");

    public final StringPath conferenceId = createString("conferenceId");

    public final SimplePath<Object> result = createSimple("result", Object.class);

    public final com.querydsl.sql.PrimaryKey<QSdoWsConferenceResults> sdoWsConfRes_pk = createPrimaryKey(conferenceId);

    public QSdoWsConferenceResults(String variable) {
        super(QSdoWsConferenceResults.class, forVariable(variable), "MDSYS", "SDO_WS_CONFERENCE_RESULTS");
        addMetadata();
    }

    public QSdoWsConferenceResults(String variable, String schema, String table) {
        super(QSdoWsConferenceResults.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSdoWsConferenceResults(Path<? extends QSdoWsConferenceResults> path) {
        super(path.getType(), path.getMetadata(), "MDSYS", "SDO_WS_CONFERENCE_RESULTS");
        addMetadata();
    }

    public QSdoWsConferenceResults(PathMetadata metadata) {
        super(QSdoWsConferenceResults.class, metadata, "MDSYS", "SDO_WS_CONFERENCE_RESULTS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(conferenceId, ColumnMetadata.named("CONFERENCE_ID").withIndex(1).ofType(Types.VARCHAR).withSize(128).notNull());
        addMetadata(result, ColumnMetadata.named("RESULT").withIndex(2).ofType(2007).withSize(2000).notNull());
    }

}

