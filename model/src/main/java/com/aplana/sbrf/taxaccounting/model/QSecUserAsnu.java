package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSecUserAsnu is a Querydsl query type for QSecUserAsnu
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSecUserAsnu extends com.querydsl.sql.RelationalPathBase<QSecUserAsnu> {

    private static final long serialVersionUID = 1253033496;

    public static final QSecUserAsnu secUserAsnu = new QSecUserAsnu("SEC_USER_ASNU");

    public final NumberPath<Long> asnuId = createNumber("asnuId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> userId = createNumber("userId", Integer.class);

    public final com.querydsl.sql.PrimaryKey<QSecUserAsnu> secUserAsnuPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QSecUser> secUserAsnuUserFk = createForeignKey(userId, "ID");

    public final com.querydsl.sql.ForeignKey<QRefBookAsnu> secUserAsnuAsnuFk = createForeignKey(asnuId, "ID");

    public QSecUserAsnu(String variable) {
        super(QSecUserAsnu.class, forVariable(variable), "NDFL_1_0", "SEC_USER_ASNU");
        addMetadata();
    }

    public QSecUserAsnu(String variable, String schema, String table) {
        super(QSecUserAsnu.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSecUserAsnu(Path<? extends QSecUserAsnu> path) {
        super(path.getType(), path.getMetadata(), "NDFL_1_0", "SEC_USER_ASNU");
        addMetadata();
    }

    public QSecUserAsnu(PathMetadata metadata) {
        super(QSecUserAsnu.class, metadata, "NDFL_1_0", "SEC_USER_ASNU");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(asnuId, ColumnMetadata.named("ASNU_ID").withIndex(3).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(18).notNull());
        addMetadata(userId, ColumnMetadata.named("USER_ID").withIndex(2).ofType(Types.DECIMAL).withSize(9).notNull());
    }

}

