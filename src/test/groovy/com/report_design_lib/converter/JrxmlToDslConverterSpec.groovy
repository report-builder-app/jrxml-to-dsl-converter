package com.report_design_lib.converter

import spock.lang.Specification

class JrxmlToDslConverterSpec extends Specification {

    def "генерирует DSL с параметрами, полями, стилями и бэндами"() {
        given:
        def model = JrxmlXmlParser.parse(JrxmlXmlParserSpec.SIMPLE_JRXML)

        when:
        def code = new JrxmlToDslConverter().convert(model)

        then:
        code.contains("ReportDsl.report('CustomerReport')")
        code.contains("param('StartDate', Date)")
        code.contains("param('Title', String, \"Customers\")")
        code.contains("field('id', Long)")
        code.contains("field('name', String)")
        code.contains("queryString '''SELECT id, name FROM customers'''")
        code.contains("style('header')")
        code.contains("isBold = true")
        code.contains("size = 12")
        code.contains("titleBand {")
        code.contains("columnHeader {")
        code.contains("detail {")
        code.contains("label(text: 'Customers', style: 'header', width: 400.0f, height: 20.0f)")
        code.contains("textField(expression: '\$F{id}', width: 100.0f)")
    }

    def "генерирует валидный Groovy-код"() {
        given:
        def model = JrxmlXmlParser.parse(JrxmlXmlParserSpec.SIMPLE_JRXML)
        def code = new JrxmlToDslConverter().convert(model)

        when: "компилируем код"
        def shell = new GroovyShell()
        def script = shell.parse(code)

        then:
        noExceptionThrown()
        script != null
    }
}
