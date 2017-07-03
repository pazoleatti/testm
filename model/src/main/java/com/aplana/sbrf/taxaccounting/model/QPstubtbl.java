package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QPstubtbl is a Querydsl query type for QPstubtbl
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QPstubtbl extends com.querydsl.sql.RelationalPathBase<QPstubtbl> {

    private static final long serialVersionUID = 62616061;

    public static final QPstubtbl pstubtbl = new QPstubtbl("PSTUBTBL");

    public final StringPath dbname = createString("dbname");

    public final StringPath line = createString("line");

    public final NumberPath<java.math.BigInteger> lineno = createNumber("lineno", java.math.BigInteger.class);

    public final StringPath lun = createString("lun");

    public final StringPath lutype = createString("lutype");

    public final StringPath username = createString("username");

    public QPstubtbl(String variable) {
        super(QPstubtbl.class, forVariable(variable), "SYS", "PSTUBTBL");
        addMetadata();
    }

    public QPstubtbl(String variable, String schema, String table) {
        super(QPstubtbl.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QPstubtbl(Path<? extends QPstubtbl> path) {
        super(path.getType(), path.getMetadata(), "SYS", "PSTUBTBL");
        addMetadata();
    }

    public QPstubtbl(PathMetadata metadata) {
        super(QPstubtbl.class, metadata, "SYS", "PSTUBTBL");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(dbname, ColumnMetadata.named("DBNAME").withIndex(2).ofType(Types.VARCHAR).withSize(128));
        addMetadata(line, ColumnMetadata.named("LINE").withIndex(6).ofType(Types.VARCHAR).withSize(1800));
        addMetadata(lineno, ColumnMetadata.named("LINENO").withIndex(5).ofType(Types.DECIMAL).withSize(22));
        addMetadata(lun, ColumnMetadata.named("LUN").withIndex(3).ofType(Types.VARCHAR).withSize(30));
        addMetadata(lutype, ColumnMetadata.named("LUTYPE").withIndex(4).ofType(Types.VARCHAR).withSize(3));
        addMetadata(username, ColumnMetadata.named("USERNAME").withIndex(1).ofType(Types.VARCHAR).withSize(30));
    }

}

