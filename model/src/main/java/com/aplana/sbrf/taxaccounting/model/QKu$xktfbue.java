package com.aplana.sbrf.taxaccounting.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;




/**
 * QKu$xktfbue is a Querydsl query type for QKu$xktfbue
 */
@Generated("com.querydsl.sql.codegen.MetaDataSerializer")
public class QKu$xktfbue extends com.querydsl.sql.RelationalPathBase<QKu$xktfbue> {

    private static final long serialVersionUID = -1653435568;

    public static final QKu$xktfbue ku$xktfbue = new QKu$xktfbue("KU$XKTFBUE");

    public final NumberPath<java.math.BigInteger> ktfbueblks = createNumber("ktfbueblks", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ktfbuesegbno = createNumber("ktfbuesegbno", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ktfbuesegfno = createNumber("ktfbuesegfno", java.math.BigInteger.class);

    public final NumberPath<java.math.BigInteger> ktfbuesegtsn = createNumber("ktfbuesegtsn", java.math.BigInteger.class);

    public QKu$xktfbue(String variable) {
        super(QKu$xktfbue.class, forVariable(variable), "SYS", "KU$XKTFBUE");
        addMetadata();
    }

    public QKu$xktfbue(String variable, String schema, String table) {
        super(QKu$xktfbue.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QKu$xktfbue(Path<? extends QKu$xktfbue> path) {
        super(path.getType(), path.getMetadata(), "SYS", "KU$XKTFBUE");
        addMetadata();
    }

    public QKu$xktfbue(PathMetadata metadata) {
        super(QKu$xktfbue.class, metadata, "SYS", "KU$XKTFBUE");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(ktfbueblks, ColumnMetadata.named("KTFBUEBLKS").withIndex(4).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ktfbuesegbno, ColumnMetadata.named("KTFBUESEGBNO").withIndex(3).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ktfbuesegfno, ColumnMetadata.named("KTFBUESEGFNO").withIndex(2).ofType(Types.DECIMAL).withSize(22));
        addMetadata(ktfbuesegtsn, ColumnMetadata.named("KTFBUESEGTSN").withIndex(1).ofType(Types.DECIMAL).withSize(22));
    }

}

