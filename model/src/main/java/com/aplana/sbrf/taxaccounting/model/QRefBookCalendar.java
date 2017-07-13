package com.aplana.sbrf.taxaccounting.model;

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

    private static final long serialVersionUID = 1595178685;

    public static final QRefBookCalendar refBookCalendar = new QRefBookCalendar("REF_BOOK_CALENDAR");

    public final DateTimePath<java.sql.Timestamp> cdate = createDateTime("cdate", java.sql.Timestamp.class);

    public final NumberPath<Byte> ctype = createNumber("ctype", Byte.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookCalendar> refBookCalendarPk = createPrimaryKey(cdate);

    public QRefBookCalendar(String variable) {
        super(QRefBookCalendar.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_CALENDAR");
        addMetadata();
    }

    public QRefBookCalendar(String variable, String schema, String table) {
        super(QRefBookCalendar.class, forVariable(variable), schema, table);
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
    }

}

