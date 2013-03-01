def fields = ["a":"1", "b":"2", "c":"3"]
	File file = File.createTempFile("temp",".ini");
	file.withWriter{ out ->
    	fields.each { key, value ->
        	out.writeLine("${key}=${value}")
    	}
	}
	file.delete();