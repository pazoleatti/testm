def fields = ["a":"1", "b":"2", "c":"3"]
	File.createTempFile("temp",".ini").withWriter { out ->
    fields.each() { key, value ->
        out.writeLine("${key}=${value}")
    }
}