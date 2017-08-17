package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookOktmo is a Querydsl query type for QRefBookOktmo
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookOktmo extends com.querydsl.sql.RelationalPathBase<QRefBookOktmo> {

    private static final long serialVersionUID = 1168693915;

    public static final QRefBookOktmo refBookOktmo = new QRefBookOktmo("REF_BOOK_OKTMO");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final NumberPath<Byte> razd = createNumber("razd", Byte.class);

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookOktmo> refBookOktmoPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetOktmoFk = createInvForeignKey(id, "OKTMO");

    public final com.querydsl.sql.ForeignKey<QRefBookFondDetail> _refBookFondDetOktmoFk = createInvForeignKey(id, "OKTMO");

    public final com.querydsl.sql.ForeignKey<QRefBookRegion> _refBookRegionOktmoFk = createInvForeignKey(id, "OKTMO");

    public QRefBookOktmo(String variable) {
        super(QRefBookOktmo.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_OKTMO");
        addMetadata();
    }

    public QRefBookOktmo(String variable, String schema, String table) {
        super(QRefBookOktmo.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookOktmo(Path<? extends QRefBookOktmo> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_OKTMO");
        addMetadata();
    }

    public QRefBookOktmo(PathMetadata metadata) {
        super(QRefBookOktmo.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_OKTMO");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(2).ofType(Types.VARCHAR).withSize(11).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(3).ofType(Types.VARCHAR).withSize(500).notNull());
        addMetadata(razd, ColumnMetadata.named("RAZD").withIndex(7).ofType(Types.DECIMAL).withSize(1));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(6).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(5).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(4).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

