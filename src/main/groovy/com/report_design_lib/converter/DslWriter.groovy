package com.report_design_lib.converter

class DslWriter {
    static File write(JrxmlModel model, File output) {
        def code = new JrxmlToDslConverter().convert(model)
        output.parentFile?.mkdirs()
        output.text = code
        output
    }
}
