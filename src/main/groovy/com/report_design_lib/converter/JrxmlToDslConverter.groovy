package com.report_design_lib.converter

class JrxmlToDslConverter {

    String convert(JrxmlModel m) {
        def out = new StringBuilder()
        out << 'import com.report_design_lib.core.ReportDsl\n'
        out << 'import com.report_design_lib.core.elements.*\n'
        out << 'import com.report_design_lib.core.elements.PageFormat\n'
        out << 'import com.report_design_lib.core.elements.PageOrientation\n'
        out << '\n'
        out << "def report = ReportDsl.report('${m.name}') {\n"

        if (m.title) out << "    title '${escape(m.title)}'\n"
        if (m.pageWidth && m.pageHeight) {
            out << "    // page size: ${m.pageWidth} x ${m.pageHeight}\n"
        }
        out << "    orientation PageOrientation.${m.orientation.toUpperCase()}\n\n"

        if (m.parameters) {
            out << '    parameters {\n'
            m.parameters.each { p ->
                def type = shortType(p.type)
                def defVal = p.defaultValue ? ", ${p.defaultValue}" : ''
                out << "        param('${p.name}', ${type}${defVal})\n"
            }
            out << '    }\n\n'
        }

        if (m.fields) {
            out << '    fields {\n'
            m.fields.each { f ->
                out << "        field('${f.name}', ${shortType(f.type)})\n"
            }
            out << '    }\n\n'
        }

        if (m.query) {
            out << "    queryString '''${m.query}'''\n\n"
        }

        if (m.styles) {
            out << '    styles {\n'
            m.styles.each { s ->
                out << "        style('${s.name}') {\n"
                out << '            font {\n'
                if (s.fontSize)  out << "                size = ${s.fontSize}\n"
                if (s.isBold)    out << '                isBold = true\n'
                if (s.isItalic)  out << '                isItalic = true\n'
                out << '            }\n'
                out << '        }\n'
            }
            out << '    }\n\n'
        }

        out << '    bands {\n'
        m.bands.each { band ->
            def method = bandMethod(band.type)
            out << "        ${method} {\n"
            (band.elements as List<Map>).each { el -> writeElement(out, el) }
            out << '        }\n'
        }
        out << '    }\n'

        out << '}\n\n'
        out << 'report\n'
        out.toString()
    }

    private void writeElement(StringBuilder out, Map el) {
        switch (el.kind) {
            case 'label':
                out << "            label(text: '${escape(el.text)}'"
                if (el.style) out << ", style: '${el.style}'"
                if (el.width) out << ", width: ${el.width}f"
                if (el.height) out << ", height: ${el.height}f"
                out << ')\n'; break
            case 'textField':
                out << "            textField(expression: '${escape(el.expression)}'"
                if (el.style) out << ", style: '${el.style}'"
                if (el.width) out << ", width: ${el.width}f"
                out << ')\n'; break
            case 'line':
                out << "            line(y: ${el.y ?: 0}f"
                if (el.width) out << ", width: ${el.width}f"
                out << ')\n'; break
        }
    }

    private static String bandMethod(String type) {
        switch (type?.toLowerCase()) {
            case 'title':         return 'titleBand'
            case 'columnheader':  return 'columnHeader'
            case 'detail':        return 'detail'
            case 'summary':       return 'summary'
            default:              return 'detail'
        }
    }

    private static String shortType(String fqn) {
        if (!fqn) return 'String'
        fqn.replace('java.lang.', '')
                .replace('java.util.', '')
                .replace('java.math.', '')
    }

    private static String escape(String s) {
        s?.replace("\\", "\\\\")?.replace("'", "\\'") ?: ''
    }
}
