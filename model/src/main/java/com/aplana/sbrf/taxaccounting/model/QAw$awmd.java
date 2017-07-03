package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAw$awmd is a Querydsl query type for QAw$awmd
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAw$awmd extends com.querydsl.sql.RelationalPathBase<QAw$awmd> {

    private static final long serialVersionUID = 1276993598;

    public static final QAw$awmd aw$awmd = new QAw$awmd("AW$AWMD");

    public final SimplePath<java.sql.Blob> awlob = createSimple("awlob", java.sql.Blob.class);

    public final NumberPath<Integer> extnum = createNumber("extnum", Integer.class);

    public final NumberPath<Long> gen_ = createNumber("gen_", Long.class);

    public final StringPath objname = createString("objname");

    public final StringPath partname = createString("partname");

    public final NumberPath<Long> ps_ = createNumber("ps_", Long.class);

    public QAw$awmd(String variable) {
        super(QAw$awmd.class, forVariable(variable), "SYS", "AW$AWMD");
        addMetadata();
    }

    public QAw$awmd(String variable, String schema, String table) {
        super(QAw$awmd.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAw$awmd(Path<? extends QAw$awmd> path) {
        super(path.getType(), path.getMetadata(), "SYS", "AW$AWMD");
        addMetadata();
    }

    public QAw$awmd(PathMetadata metadata) {
        super(QAw$awmd.class, metadata, "SYS", "AW$AWMD");
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

