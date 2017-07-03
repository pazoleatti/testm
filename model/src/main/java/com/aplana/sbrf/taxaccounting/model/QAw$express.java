package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAw$express is a Querydsl query type for QAw$express
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAw$express extends com.querydsl.sql.RelationalPathBase<QAw$express> {

    private static final long serialVersionUID = 1677902303;

    public static final QAw$express aw$express = new QAw$express("AW$EXPRESS");

    public final SimplePath<java.sql.Blob> awlob = createSimple("awlob", java.sql.Blob.class);

    public final NumberPath<Integer> extnum = createNumber("extnum", Integer.class);

    public final NumberPath<Long> gen_ = createNumber("gen_", Long.class);

    public final StringPath objname = createString("objname");

    public final StringPath partname = createString("partname");

    public final NumberPath<Long> ps_ = createNumber("ps_", Long.class);

    public QAw$express(String variable) {
        super(QAw$express.class, forVariable(variable), "SYS", "AW$EXPRESS");
        addMetadata();
    }

    public QAw$express(String variable, String schema, String table) {
        super(QAw$express.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAw$express(Path<? extends QAw$express> path) {
        super(path.getType(), path.getMetadata(), "SYS", "AW$EXPRESS");
        addMetadata();
    }

    public QAw$express(PathMetadata metadata) {
        super(QAw$express.class, metadata, "SYS", "AW$EXPRESS");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(awlob, ColumnMetadata.named("AWLOB").withIndex(4).ofType(Types.BLOB).withSize(372));
        addMetadata(extnum, ColumnMetadata.named("EXTNUM").withIndex(3).ofType(Types.DECIMAL).withSize(8));
        addMetadata(gen_, ColumnMetadata.named("GEN#").withIndex(2).ofType(Types.DECIMAL).withSize(10));
        addMetadata(objname, ColumnMetadata.named("OBJNAME").withIndex(5).ofType(Types.VARCHAR).withSize(256));
        addMetadata(partname, ColumnMetadata.named("PARTNAME").withIndex(6).ofType(Types.VARCHAR).withSize(256));
        addMetadata(ps_, ColumnMetadata.named("PS#").withIndex(1).ofType(Types.DECIMAL).withSize(10));
    }

}

