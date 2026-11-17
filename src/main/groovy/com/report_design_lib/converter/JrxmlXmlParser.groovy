package com.report_design_lib.converter

import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult

class JrxmlXmlParser {

    static JrxmlModel parse(String xml) {
        def root = new XmlSlurper(false, false).parseText(xml)
        new JrxmlModel(
                name:        root.@name.text() ?: 'Report',
                title:       extractTitle(root),
                pageWidth:   root.@pageWidth.text()  as Float,
                pageHeight:  root.@pageHeight.text() as Float,
                orientation: root.@orientation.text()?.toLowerCase() ?: 'portrait',
                query:       root.queryString.text()?.trim(),
                parameters:  extractParameters(root),
                fields:      extractFields(root),
                styles:      extractStyles(root),
                bands:       extractBands(root)
        )
    }

    private static String extractTitle(GPathResult root) {
        root.band.find { it.@name.text() == 'title' }?.staticText?.text()
    }

    private static List<Map> extractParameters(GPathResult root) {
        root.parameter.collect { p ->
            [
                    name:         p.@name.text(),
                    type:         p.@class.text(),
                    defaultValue: p.defaultValueExpression.text() ?: null
            ]
        }
    }

    private static List<Map> extractFields(GPathResult root) {
        root.field.collect { f -> [name: f.@name.text(), type: f.@class.text()] }
    }

    private static List<Map> extractStyles(GPathResult root) {
        root.style.collect { s ->
            [
                    name:       s.@name.text(),
                    isBold:     s.@isBold.text() == 'true',
                    isItalic:   s.@isItalic.text() == 'true',
                    fontSize:   s.@fontSize.text() as Integer,
                    forecolor:  s.@forecolor.text()
            ]
        }
    }

    private static List<Map> extractBands(GPathResult root) {
        def result = []

        ['title', 'pageHeader', 'columnHeader', 'detail', 'summary', 'pageFooter'].each { section ->
            def node = root."$section"
            if (node.size() == 0) return
            node.each { sectionNode ->
                def band = sectionNode.band
                if (band.size() == 0) return
                result << [
                        type:     section,
                        elements: band.children().collect { parseElement(it) }.findAll { it != null }
                ]
            }
        }

        result
    }

    private static Map parseElement(GPathResult el) {
        switch (el.name()) {
            case 'staticText':
                def re = el.reportElement
                return [
                        kind:   'label',
                        text:   el.text.text() ?: '',     // <staticText><text>...</text></staticText>
                        style:  re.@style.text() ?: null,
                        x:      re.@x.text() as Float,
                        y:      re.@y.text() as Float,
                        width:  re.@width.text() as Float,
                        height: re.@height.text() as Float
                ]
            case 'textField':
                def re = el.reportElement
                return [
                        kind:       'textField',
                        expression: el.textFieldExpression.text(),
                        style:      re.@style.text() ?: null,
                        x:          re.@x.text() as Float,
                        y:          re.@y.text() as Float,
                        width:      re.@width.text() as Float,
                        height:     re.@height.text() as Float
                ]
            case 'line':
                def re = el.reportElement
                return [
                        kind:  'line',
                        x:     re.@x.text() as Float,
                        y:     re.@y.text() as Float,
                        width: re.@width.text() as Float
                ]
            default:
                return null
        }
    }
}
