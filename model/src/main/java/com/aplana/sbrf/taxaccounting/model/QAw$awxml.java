package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QAw$awxml is a Querydsl query type for QAw$awxml
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QAw$awxml extends com.querydsl.sql.RelationalPathBase<QAw$awxml> {

    private static final long serialVersionUID = 932106832;

    public static final QAw$awxml aw$awxml = new QAw$awxml("AW$AWXML");

    public final SimplePath<java.sql.Blob> awlob = createSimple("awlob", java.sql.Blob.class);

    public final NumberPath<Integer> extnum = createNumber("extnum", Integer.class);

    public final NumberPath<Long> gen_ = createNumber("gen_", Long.class);

    public final StringPath objname = createString("objname");

    public final StringPath partname = createString("partname");

    public final NumberPath<Long> ps_ = createNumber("ps_", Long.class);

    public QAw$awxml(String variable) {
        super(QAw$awxml.class, forVariable(variable), "SYS", "AW$AWXML");
        addMetadata();
    }

    public QAw$awxml(String variable, String schema, String table) {
        super(QAw$awxml.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QAw$awxml(Path<? extends QAw$awxml> path) {
        super(path.getType(), path.getMetadata(), "SYS", "AW$AWXML");
        addMetadata();
    }

    public QAw$awxml(PathMetadata metadata) {
        super(QAw$awxml.class, metadata, "SYS", "AW$AWXML");
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

