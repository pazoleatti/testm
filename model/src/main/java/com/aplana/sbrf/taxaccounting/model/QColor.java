package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QColor is a Querydsl query type for QColor
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QColor extends com.querydsl.sql.RelationalPathBase<QColor> {

    private static final long serialVersionUID = 1089039462;

    public static final QColor color = new QColor("COLOR");

    public final NumberPath<Short> b = createNumber("b", Short.class);

    public final NumberPath<Short> g = createNumber("g", Short.class);

    public final StringPath hex = createString("hex");

    public final NumberPath<Short> id = createNumber("id", Short.class);

    public final StringPath name = createString("name");

    public final NumberPath<Short> r = createNumber("r", Short.class);

    public final com.querydsl.sql.PrimaryKey<QColor> colorPk = createPrimaryKey(id);

    public final com.querydsl.sql.ForeignKey<QFormStyle> _formStyleFkBackColor = createInvForeignKey(id, "BACK_COLOR");

    public final com.querydsl.sql.ForeignKey<QFormStyle> _formStyleFkFontColor = createInvForeignKey(id, "FONT_COLOR");

    public QColor(String variable) {
        super(QColor.class, forVariable(variable), "NDFL_UNSTABLE", "COLOR");
        addMetadata();
    }

    public QColor(String variable, String schema, String table) {
        super(QColor.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QColor(Path<? extends QColor> path) {
        super(path.getType(), path.getMetadata(), "NDFL_UNSTABLE", "COLOR");
        addMetadata();
    }

    public QColor(PathMetadata metadata) {
        super(QColor.class, metadata, "NDFL_UNSTABLE", "COLOR");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(b, ColumnMetadata.named("B").withIndex(5).ofType(Types.DECIMAL).withSize(3).notNull());
        addMetadata(g, ColumnMetadata.named("G").withIndex(4).ofType(Types.DECIMAL).withSize(3).notNull());
        addMetadata(hex, ColumnMetadata.named("HEX").withIndex(6).ofType(Types.VARCHAR).withSize(7).notNull());
        addMetadata(id, ColumnMetadata.named("ID").withIndex(1).ofType(Types.DECIMAL).withSize(3).notNull());
        addMetadata(name, ColumnMetadata.named("NAME").withIndex(2).ofType(Types.VARCHAR).withSize(100).notNull());
        addMetadata(r, ColumnMetadata.named("R").withIndex(3).ofType(Types.DECIMAL).withSize(3).notNull());
    }

}

