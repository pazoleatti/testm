package com.aplana.sbrf.taxaccounting.dao.impl.refbook.filter;

import com.aplana.sbrf.taxaccounting.dao.refbook.filter.FilterTreeListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Объект хелпер - фильтр, содержит единственный
 * метод для получения безопасного фрагмента sql кода
 * для использования его в запросе для фильтрации справочника.
 *
 * @author auldanov
 */
public class Filter
{
    public static void getFilterQuery(String query, FilterTreeListener listener)
    {
        if (query == null){
            return;
        }
        // Создаем InputStream из query
        ANTLRInputStream input = new ANTLRInputStream(query);

        // Создаем лексер которому скормим InputStream
        FilterTreeLexer lexer = new FilterTreeLexer(input);

        // TokenStream  из токенов вытянутых из лексера
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Создадим парсер и отправим ему буфер токенов
        FilterTreeParser parser = new FilterTreeParser(tokens);

        ParseTree tree = parser.query(); // начинаем разбор с первого правила
        //System.out.println(tree.toStringTree(parser)); // выыести LISP-подобное дерево

        // Создадим стандартный ходунок  для разбора дерева методом вызова коллбэков
        ParseTreeWalker walker = new ParseTreeWalker();
        // Передадим листнера для прохода по дереву, лисенер содержит методы которые будут вызваны по мере обхода дерева
        walker.walk(listener, tree);
    }
}
