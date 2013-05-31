import groovy.xml.MarkupBuilder;

def printRecord(xmlbuilder, args) {
	xmlbuilder.record(id: 'id' + args['index']);
}

def xmlbuilder = new MarkupBuilder(xml)


xmlbuilder.root(rootAttr: "корневой аттрибут") {
	for (int i = 0; i < 10; ++i) {
		if (i % 2 == 0) {
			printRecord(xmlbuilder, [index: i])
		}
	}
}

logger.info("Успех")