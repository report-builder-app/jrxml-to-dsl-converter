package com.report_design_lib.converter

class ConverterCli {

    static void main(String[] args) {
        if (args.length == 0 || args[0] in ['-h', '--help']) {
            printUsage()
            return
        }

        File input  = new File(args[0])
        File output = args.length > 1
                ? new File(args[1])
                : new File(input.parentFile, input.name.replaceAll(/\.jrxml$/, '') + '.groovy')

        if (!input.exists()) {
            System.err.println "Input not found: $input"
            System.exit(2)
        }

        try {
            def model = JrxmlXmlParser.parse(input.text)
            DslWriter.write(model, output)
            println "Generated: ${output.absolutePath}"
        } catch (Exception e) {
            System.err.println "Conversion failed: ${e.message}"
            e.printStackTrace()
            System.exit(1)
        }
    }

    private static void printUsage() {
        println '''
Usage: jasper-to-dsl <input.jrxml> [output.groovy]

Converts a JasperReports JRXML template into Groovy DSL (ReportDsl).
'''
    }
}
