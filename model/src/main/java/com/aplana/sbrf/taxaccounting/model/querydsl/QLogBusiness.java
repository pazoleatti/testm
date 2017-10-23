package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLogBusiness is a Querydsl query type for QLogBusiness
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLogBusiness extends com.querydsl.sql.RelationalPathBase<QLogBusiness> {

    private static final long serialVersionUID = -964869608;

    public static final QLogBusiness logBusiness = new QLogBusiness("LOG_BUSINESS");

    public final NumberPath<Integer> declarationDataId = createNumber("declarationDataId", Integer.class);

    public final NumberPath<Short> eventId = createNumber("eventId", Short.class);

    public final NumberPath<Integer> formDataId = createNumber("formDataId", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<org.joda.time.LocalDateTime> logDate = createDateTime("logDate", org.joda.time.LocalDateTime.class);

    public final StringPath note = createString("note");

    public final StringPath roles = createString("roles");

    public final StringPath userDepartmentName = createString("userDepartmentName");

    public final StringPath userLogin = createString("userLogin");

    public final com.querydsl.sql.PrimaryKey<QLogBusiness> logBusinessPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QDeclarationData> logBusinessFkDeclarationId = createForeignKey(declarationDataId, "ID");

    public final com.querydsl.sql.ForeignKey<QEvent> logBusinessFkEventId = createForeignKey(eventId, "ID");

    public QLogBusiness(String variable) {
        super(QLogBusiness.class, forVariable(variable), "NDFL_UNSTABLE", "LOG_BUSINESS");
        addMetadata();
    }

    public QLogBusiness(String variable, String schema, String table) {
        super(QLogBusiness.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLogBusiness(String variable, String schema) {
        super(QLogBusiness.class, forVariable(variable), schema, "LOG_BUSINESS");
        addMetadata();
    }

    public QLogBusiness(Path<? extends QLogBusiness> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "LOG_BUSINESS");
        addMetadata();
    }

    public QLogBusiness(PathMetadata metadata) {
        super(QLogBusiness.class, metadata, "NDFL_UNSTABLE", "LOG_BUSINESS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(declarationDataId, ColumnMetadata.named("DECLARATION_DATA_ID").withIndex(6).ofType(Types.DECIMAL).withSize(9));
        addMetadata(eventId, ColumnMetadata.named("EVENT_ID").withIndex(3).ofType(Types.DECIMAL).withSize(3).notNull());
        addMetadata(formDataId, ColumnMetadata.named("FORM_DATA_ID").withIndex(7).ofType(Types.DECIMAL).withSize(9));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(logDate, ColumnMetadata.named("LOG_DATE").withIndex(2).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(note, ColumnMetadata.named("NOTE").withIndex(8).ofType(Types.VARCHAR).withSize(510));
        addMetadata(roles, ColumnMetadata.named("ROLES").withIndex(5).ofType(Types.VARCHAR).withSize(2000).notNull());
        addMetadata(userDepartmentName, ColumnMetadata.named("USER_DEPARTMENT_NAME").withIndex(9).ofType(Types.VARCHAR).withSize(4000).notNull());
        addMetadata(userLogin, ColumnMetadata.named("USER_LOGIN").withIndex(4).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

