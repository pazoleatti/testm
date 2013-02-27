def fields = ["a":"1", "b":"2", "c":"3"]
new File("/tmp/foo.ini").withWriter { out ->
    fields.each() { key, value ->
        out.writeLine("${key}=${value}")
    }
}