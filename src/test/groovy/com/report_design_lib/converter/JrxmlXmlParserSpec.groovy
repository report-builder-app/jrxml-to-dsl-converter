package com.report_design_lib.converter

import spock.lang.Specification

class JrxmlXmlParserSpec extends Specification {

    static final String SIMPLE_JRXML = '''<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
              name="CustomerReport" pageWidth="595" pageHeight="842" orientation="Portrait">
    <parameter name="StartDate" class="java.util.Date"/>
    <parameter name="Title" class="java.lang.String">
        <defaultValueExpression><![CDATA["Customers"]]></defaultValueExpression>
    </parameter>
    <field name="id"   class="java.lang.Long"/>
    <field name="name" class="java.lang.String"/>
    <queryString><![CDATA[SELECT id, name FROM customers]]></queryString>

    <style name="header" isBold="true" fontSize="12"/>

    <title>
        <band height="30">
            <staticText>
                <reportElement style="header" x="0" y="0" width="400" height="20"/>
                <text>Customers</text>
            </staticText>
        </band>
    </title>

    <columnHeader>
        <band height="20">
            <staticText>
                <reportElement x="0" y="0" width="100" height="15"/>
                <text>ID</text>
            </staticText>
        </band>
    </columnHeader>

    <detail>
        <band height="20">
            <textField>
                <reportElement x="0" y="0" width="100" height="15"/>
                <textFieldExpression><![CDATA[$F{id}]]></textFieldExpression>
            </textField>
        </band>
    </detail>
</jasperReport>'''

    def "парсит имя, параметры, поля и query"() {
        when:
        def m = JrxmlXmlParser.parse(SIMPLE_JRXML)

        then:
        m.name == 'CustomerReport'
        m.pageWidth == 595f
        m.orientation == 'portrait'
        m.query == 'SELECT id, name FROM customers'

        m.parameters.size() == 2
        m.parameters[0].name == 'StartDate'
        m.parameters[0].type == 'java.util.Date'
        m.parameters[1].defaultValue == '"Customers"'

        m.fields*.name == ['id', 'name']
        m.fields[0].type == 'java.lang.Long'
    }

    def "парсит стили и бэнды"() {
        when:
        def m = JrxmlXmlParser.parse(SIMPLE_JRXML)

        then:
        m.styles.size() == 1
        m.styles[0].name == 'header'
        m.styles[0].isBold
        m.styles[0].fontSize == 12

        m.bands.size() == 3
        m.bands.find { it.type == 'title' }.elements.size() == 1
        m.bands.find { it.type == 'detail' }.elements[0].kind == 'textField'
        m.bands.find { it.type == 'detail' }.elements[0].expression == '$F{id}'
    }
}
