package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QLogSystem is a Querydsl query type for QLogSystem
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QLogSystem extends com.querydsl.sql.RelationalPathBase<QLogSystem> {

    private static final long serialVersionUID = -1700101610;

    public static final QLogSystem logSystem = new QLogSystem("LOG_SYSTEM");

    public final NumberPath<Integer> auditFormTypeId = createNumber("auditFormTypeId", Integer.class);

    public final StringPath declarationTypeName = createString("declarationTypeName");

    public final StringPath departmentName = createString("departmentName");

    public final NumberPath<Short> eventId = createNumber("eventId", Short.class);

    public final NumberPath<Integer> formDepartmentId = createNumber("formDepartmentId", Integer.class);

    public final NumberPath<Integer> formKindId = createNumber("formKindId", Integer.class);

    public final NumberPath<Integer> formTypeId = createNumber("formTypeId", Integer.class);

    public final StringPath formTypeName = createString("formTypeName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath ip = createString("ip");

    public final NumberPath<Byte> isError = createNumber("isError", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> logDate = createDateTime("logDate", org.joda.time.LocalDateTime.class);

    public final StringPath logId = createString("logId");

    public final StringPath note = createString("note");

    public final StringPath reportPeriodName = createString("reportPeriodName");

    public final StringPath roles = createString("roles");

    public final StringPath server = createString("server");

    public final StringPath userDepartmentName = createString("userDepartmentName");

    public final StringPath userLogin = createString("userLogin");

    public final com.querydsl.sql.PrimaryKey<QLogSystem> logSystemPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QLog> logSystemLogFk = createForeignKey(logId, "ID");

    public final com.querydsl.sql.ForeignKey<QAuditFormType> logSystemFkAuditFormType = createForeignKey(auditFormTypeId, "ID");

    public final com.querydsl.sql.ForeignKey<QFormKind> logSystemFkKind = createForeignKey(formKindId, "ID");

    public final com.querydsl.sql.ForeignKey<QEvent> logSystemFkEventId = createForeignKey(eventId, "ID");

    public QLogSystem(String variable) {
        super(QLogSystem.class, forVariable(variable), "NDFL_UNSTABLE", "LOG_SYSTEM");
        addMetadata();
    }

    public QLogSystem(String variable, String schema, String table) {
        super(QLogSystem.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QLogSystem(Path<? extends QLogSystem> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "LOG_SYSTEM");
        addMetadata();
    }

    public QLogSystem(PathMetadata metadata) {
        super(QLogSystem.class, metadata, "NDFL_UNSTABLE", "LOG_SYSTEM");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(auditFormTypeId, ColumnMetadata.named("AUDIT_FORM_TYPE_ID").withIndex(18).ofType(Types.DECIMAL).withSize(9));
        addMetadata(declarationTypeName, ColumnMetadata.named("DECLARATION_TYPE_NAME").withIndex(12).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(departmentName, ColumnMetadata.named("DEPARTMENT_NAME").withIndex(9).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(eventId, ColumnMetadata.named("EVENT_ID").withIndex(4).ofType(Types.DECIMAL).withSize(3).notNull());
        addMetadata(formDepartmentId, ColumnMetadata.named("FORM_DEPARTMENT_ID").withIndex(14).ofType(Types.DECIMAL).withSize(9));
        addMetadata(formKindId, ColumnMetadata.named("FORM_KIND_ID").withIndex(6).ofType(Types.DECIMAL).withSize(9));
        addMetadata(formTypeId, ColumnMetadata.named("FORM_TYPE_ID").withIndex(15).ofType(Types.DECIMAL).withSize(9));
        addMetadata(formTypeName, ColumnMetadata.named("FORM_TYPE_NAME").withIndex(13).ofType(Types.VARCHAR).withSize(1000));
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(ip, ColumnMetadata.named("IP").withIndex(3).ofType(Types.VARCHAR).withSize(39));
        addMetadata(isError, ColumnMetadata.named("IS_ERROR").withIndex(16).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(logDate, ColumnMetadata.named("LOG_DATE").withIndex(2).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(logId, ColumnMetadata.named("LOG_ID").withIndex(19).ofType(Types.VARCHAR).withSize(36));
        addMetadata(note, ColumnMetadata.named("NOTE").withIndex(7).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(reportPeriodName, ColumnMetadata.named("REPORT_PERIOD_NAME").withIndex(8).ofType(Types.VARCHAR).withSize(100));
        addMetadata(roles, ColumnMetadata.named("ROLES").withIndex(5).ofType(Types.VARCHAR).withSize(2000));
        addMetadata(server, ColumnMetadata.named("SERVER").withIndex(17).ofType(Types.VARCHAR).withSize(200));
        addMetadata(userDepartmentName, ColumnMetadata.named("USER_DEPARTMENT_NAME").withIndex(10).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(userLogin, ColumnMetadata.named("USER_LOGIN").withIndex(11).ofType(Types.VARCHAR).withSize(255).notNull());
    }

}

