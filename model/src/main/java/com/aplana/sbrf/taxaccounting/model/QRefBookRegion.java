package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QRefBookRegion is a Querydsl query type for QRefBookRegion
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QRefBookRegion extends com.querydsl.sql.RelationalPathBase<QRefBookRegion> {

    private static final long serialVersionUID = 1949728307;

    public static final QRefBookRegion refBookRegion = new QRefBookRegion("REF_BOOK_REGION");

    public final StringPath code = createString("code");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath okatoDefinition = createString("okatoDefinition");

    public final NumberPath<Long> oktmo = createNumber("oktmo", Long.class);

    public final StringPath oktmoDefinition = createString("oktmoDefinition");

    public final NumberPath<Integer> recordId = createNumber("recordId", Integer.class);

    public final NumberPath<Byte> status = createNumber("status", Byte.class);

    public final DateTimePath<org.joda.time.LocalDateTime> version = createDateTime("version", org.joda.time.LocalDateTime.class);

    public final com.querydsl.sql.PrimaryKey<QRefBookRegion> refBookRegionPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QRefBookOktmo> refBookRegionOktmoFk = createForeignKey(oktmo, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookFondDetail> _refBookFondDetRegionFk = createInvForeignKey(id, "REGION");

    public final com.querydsl.sql.ForeignKey<QDepartment> _departmentRegionIdFk = createInvForeignKey(id, "REGION_ID");

    public final com.querydsl.sql.ForeignKey<QRefBookNdflDetail> _refBookNdflDetRegionFk = createInvForeignKey(id, "REGION");

    public QRefBookRegion(String variable) {
        super(QRefBookRegion.class, forVariable(variable), "NDFL_UNSTABLE", "REF_BOOK_REGION");
        addMetadata();
    }

    public QRefBookRegion(String variable, String schema, String table) {
        super(QRefBookRegion.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QRefBookRegion(Path<? extends QRefBookRegion> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "REF_BOOK_REGION");
        addMetadata();
    }

    public QRefBookRegion(PathMetadata metadata) {
        super(QRefBookRegion.class, metadata, "NDFL_UNSTABLE", "REF_BOOK_REGION");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(code, ColumnMetadata.named("CODE").withIndex(5).ofType(Types.VARCHAR).withSize(2).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(6).ofType(Types.VARCHAR).withSize(255).notNull());
        addMetadata(okatoDefinition, ColumnMetadata.named("OKATO_DEFINITION").withIndex(7).ofType(Types.VARCHAR).withSize(11));
        addMetadata(oktmo, ColumnMetadata.named("OKTMO").withIndex(8).ofType(Types.DECIMAL).withSize(18));
        addMetadata(oktmoDefinition, ColumnMetadata.named("OKTMO_DEFINITION").withIndex(9).ofType(Types.VARCHAR).withSize(11));
        addMetadata(recordId, ColumnMetadata.named("RECORD_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
        addMetadata(status, ColumnMetadata.named("STATUS").withIndex(4).ofType(Types.DECIMAL).withSize(1).notNull());
        addMetadata(version, ColumnMetadata.named("VERSION").withIndex(3).ofType(Types.TIMESTAMP).withSize(7).notNull());
    }

}

