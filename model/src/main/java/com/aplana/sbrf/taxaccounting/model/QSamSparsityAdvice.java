package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QSamSparsityAdvice is a Querydsl query type for QSamSparsityAdvice
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QSamSparsityAdvice extends com.querydsl.sql.RelationalPathBase<QSamSparsityAdvice> {

    private static final long serialVersionUID = 1145531459;

    public static final QSamSparsityAdvice samSparsityAdvice = new QSamSparsityAdvice("SAM_SPARSITY_ADVICE");

    public final StringPath advice = createString("advice");

    public final StringPath cubename = createString("cubename");

    public final NumberPath<java.math.BigDecimal> density = createNumber("density", java.math.BigDecimal.class);

    public final StringPath dimcolumn = createString("dimcolumn");

    public final StringPath dimension = createString("dimension");

    public final StringPath dimsource = createString("dimsource");

    public final StringPath fact = createString("fact");

    public final NumberPath<Long> leafcount = createNumber("leafcount", Long.class);

    public final NumberPath<Long> membercount = createNumber("membercount", Long.class);

    public final StringPath partby = createString("partby");

    public final StringPath partlevel = createString("partlevel");

    public final NumberPath<Integer> partnum = createNumber("partnum", Integer.class);

    public final StringPath parttops = createString("parttops");

    public final NumberPath<Short> position = createNumber("position", Short.class);

    public QSamSparsityAdvice(String variable) {
        super(QSamSparsityAdvice.class, forVariable(variable), "SYS", "SAM_SPARSITY_ADVICE");
        addMetadata();
    }

    public QSamSparsityAdvice(String variable, String schema, String table) {
        super(QSamSparsityAdvice.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QSamSparsityAdvice(Path<? extends QSamSparsityAdvice> path) {
        super(path.getType(), path.getMetadata(), "SYS", "SAM_SPARSITY_ADVICE");
        addMetadata();
    }

    public QSamSparsityAdvice(PathMetadata metadata) {
        super(QSamSparsityAdvice.class, metadata, "SYS", "SAM_SPARSITY_ADVICE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(advice, ColumnMetadata.named("ADVICE").withIndex(8).ofType(Types.VARCHAR).withSize(10).notNull());
        addMetadata(cubename, ColumnMetadata.named("CUBENAME").withIndex(1).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(density, ColumnMetadata.named("DENSITY").withIndex(10).ofType(Types.DECIMAL).withSize(11).withDigits(8));
        addMetadata(dimcolumn, ColumnMetadata.named("DIMCOLUMN").withIndex(4).ofType(Types.VARCHAR).withSize(100));
        addMetadata(dimension, ColumnMetadata.named("DIMENSION").withIndex(3).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(dimsource, ColumnMetadata.named("DIMSOURCE").withIndex(5).ofType(Types.VARCHAR).withSize(4000));
        addMetadata(fact, ColumnMetadata.named("FACT").withIndex(2).ofType(Types.VARCHAR).withSize(4000).notNull());
        addMetadata(leafcount, ColumnMetadata.named("LEAFCOUNT").withIndex(7).ofType(Types.DECIMAL).withSize(12));
        addMetadata(membercount, ColumnMetadata.named("MEMBERCOUNT").withIndex(6).ofType(Types.DECIMAL).withSize(12));
        addMetadata(partby, ColumnMetadata.named("PARTBY").withIndex(12).ofType(Types.CLOB).withSize(4000));
        addMetadata(partlevel, ColumnMetadata.named("PARTLEVEL").withIndex(14).ofType(Types.VARCHAR).withSize(200));
        addMetadata(partnum, ColumnMetadata.named("PARTNUM").withIndex(11).ofType(Types.DECIMAL).withSize(6).notNull());
        addMetadata(parttops, ColumnMetadata.named("PARTTOPS").withIndex(13).ofType(Types.CLOB).withSize(4000));
        addMetadata(position, ColumnMetadata.named("POSITION").withIndex(9).ofType(Types.DECIMAL).withSize(4).notNull());
    }

}

