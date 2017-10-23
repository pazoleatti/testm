package com.aplana.sbrf.taxaccounting.model.querydsl;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookCalendar is a Querydsl query type for QRefBookCalendar
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookCalendar extends com.querydsl.sql.RelationalPathBase<QRefBookCalendar> {

    private static final long serialVersionUID = -999436690;

    public static final QRefBookCalendar refBookCalendar = new QRefBookCalendar("REF_BOOK_CALENDAR");

    public final DateTimePath<org.joda.time.LocalDateTime> cdate = createDateTime("cdate", org.joda.time.LocalDateTime.class);

    public final NumberPath<Byte> ctype = createNumber("ctype", Byte.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookCalendar> refBookCalendarPk = createPrimaryKey(cdate);

    public QRefBookCalendar(String variable) {
        super(QRefBookCalendar.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_CALENDAR");
        addMetadata();
    }

    public QRefBookCalendar(String variable, String schema, String table) {
        super(QRefBookCalendar.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookCalendar(String variable, String schema) {
        super(QRefBookCalendar.class, forVariable(variable), schema, "REF_BOOK_CALENDAR");
        addMetadata();
    }

    public QRefBookCalendar(Path<? extends QRefBookCalendar> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_CALENDAR");
        addMetadata();
    }

    public QRefBookCalendar(PathMetadata metadata) {
        super(QRefBookCalendar.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_CALENDAR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(cdate, ColumnMetadata.named("CDATE").withIndex(1).ofType(Types.TIMESTAMP).withSize(7).notNull());
        addMetadata(ctype, ColumnMetadata.named("CTYPE").withIndex(2).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(3).ofType(Types.DECIMAL).withSize(18));
    }

}

