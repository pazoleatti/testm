package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QHelp is a Querydsl query type for QHelp
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QHelp extends com.querydsl.sql.RelationalPathBase<QHelp> {

    private static final long serialVersionUID = -1627298338;

    public static final QHelp help = new QHelp("HELP");

    public final StringPath info = createString("info");

    public final NumberPath<java.math.BigInteger> seq = createNumber("seq", java.math.BigInteger.class);

    public final StringPath topic = createString("topic");

    public final com.querydsl.sql.PrimaryKey<QHelp> helpTopicSeq = createPrimaryKey(seq, topic);

    public QHelp(String variable) {
        super(QHelp.class, forVariable(variable), "SYSTEM", "HELP");
        addMetadata();
    }

    public QHelp(String variable, String schema, String table) {
        super(QHelp.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QHelp(Path<? extends QHelp> path) {
        super(path.getType(), path.getMetadata(), "SYSTEM", "HELP");
        addMetadata();
    }

    public QHelp(PathMetadata metadata) {
        super(QHelp.class, metadata, "SYSTEM", "HELP");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(info, ColumnMetadata.named("INFO").withIndex(3).ofType(Types.VARCHAR).withSize(80));
        addMetadata(seq, ColumnMetadata.named("SEQ").withIndex(2).ofType(Types.DECIMAL).withSize(22).notNull());
        addMetadata(topic, ColumnMetadata.named("TOPIC").withIndex(1).ofType(Types.VARCHAR).withSize(50).notNull());
    }

}

