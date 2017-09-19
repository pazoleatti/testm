package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookTariffPayer is a Querydsl query type for QRefBookTariffPayer
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookTariffPayer extends com.querydsl.sql.RelationalPathBase<QRefBookTariffPayer> {

    private static final long serialVersionUID = -375768910;

    public static final QRefBookTariffPayer refBookTariffPayer = new QRefBookTariffPayer("REF_BOOK_TARIFF_PAYER");

    public final StringPath code = createString("code");

    public final NumberPath<Byte> forOpsDop = createNumber("forOpsDop", Byte.class);

    public final NumberPath<Byte> forOpsOms = createNumber("forOpsOms", Byte.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookTariffPayer> refBookTariffPayerPk = createPrimaryKey(id);

    public QRefBookTariffPayer(String variable) {
        super(QRefBookTariffPayer.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_TARIFF_PAYER");
        addMetadata();
    }

    public QRefBookTariffPayer(String variable, String schema, String table) {
        super(QRefBookTariffPayer.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookTariffPayer(Path<? extends QRefBookTariffPayer> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_TARIFF_PAYER");
        addMetadata();
    }

    public QRefBookTariffPayer(PathMetadata metadata) {
        super(QRefBookTariffPayer.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_TARIFF_PAYER");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(forOpsDop, ColumnMetadata.named("FOR_OPS_DOP").withIndex(8).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(forOpsOms, ColumnMetadata.named("FOR_OPS_OMS").withIndex(7).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(2000).notNull());
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

