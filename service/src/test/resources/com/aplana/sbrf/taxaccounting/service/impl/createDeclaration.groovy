def printRecord(xml, args) {
	xml.record(id: 'id' + args['index']);
}

xml.root(rootAttr: "корневой аттрибут") {
	for (int i = 0; i < 10; ++i) {
		if (i % 2 == 0) {
			printRecord(xml, [index: i])
		}
	}
}