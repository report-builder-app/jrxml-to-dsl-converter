# report-design-jrxml-to-dsl

Утилита для конвертации шаблонов JasperReports (JRXML) в Groovy DSL (ReportDsl).
Работает как CLI и как библиотека.

## Содержание

- [Модули и структура](#модули-и-структура)
- [Требования](#требования)
- [Сборка](#сборка)
- [Использование](#использование)
- [Матрица конвертации JRXML → DSL](#матрица-конвертации-jrxml--dsl)
- [Ограничения](#ограничения)
- [Расширение](#расширение)
- [Тесты](#тесты)
- [Лицензия](#лицензия)

## Модули и структура

Проект состоит из одного Maven-модуля `report-design-jasper-to-dsl`.

```text
report-design-jasper-to-dsl/
├── pom.xml
└── src/
├── main/groovy/com/report_design_lib/converter/
│ ├── JasperXmlParser.groovy — парсинг JRXML в JrxmlModel
│ ├── JrxmlModel.groovy — нейтральная модель
│ ├── JrxmlToDslConverter.groovy — JrxmlModel → текст Groovy DSL
│ ├── DslWriter.groovy — запись в файл
│ └── ConverterCli.groovy — CLI
└── test/groovy/com/report_design_lib/converter/
├── JasperXmlParserSpec.groovy
└── JrxmlToDslConverterSpec.groovy
```

Поток данных:

JRXML → JasperXmlParser → JrxmlModel → JrxmlToDslConverter → Groovy DSL

## Требования

- JDK 17+
- Maven 3.9+
- Groovy 4.0.31 (подтягивается транзитивно)

## Сборка

```bash
mvn -U clean install -pl report-design-jasper-to-dsl -am
```

## Использование
### CLI

```shell
mvn -q -pl report-design-jasper-to-dsl exec:java \
-Dexec.args="src/test/resources/sample.jrxml out/sample.groovy"
```

Или после сборки fat-jar:

```shell
java -jar target/report-design-jasper-to-dsl-1.0-SNAPSHOT.jar sample.jrxml sample.groovy
```

Если второй аргумент не указан, выходной файл создаётся рядом с входным с расширением .groovy.

### Программно

```groovy
import com.report_design_lib.converter.*

def model = JrxmlXmlParser.parse(new File("report.jrxml").text)
DslWriter.write(model, new File("report.groovy"))

// или без записи в файл
def code = new JrxmlToDslConverter().convert(model)
println code
```

## Пример вывода
Входной JRXML:

```xml
<jasperReport name="CustomerReport" pageWidth="595" pageHeight="842" orientation="Portrait">
    <parameter name="StartDate" class="java.util.Date"/>
    <field name="id" class="java.lang.Long"/>
    <queryString><![CDATA[SELECT id FROM customers]]></queryString>
    <title><band height="30">
        <staticText>
            <reportElement x="0" y="0" width="400" height="20"/>
            <text>Customers</text>
        </staticText>
    </band></title>
    <detail><band height="20">
        <textField>
            <reportElement x="0" y="0" width="100" height="15"/>
            <textFieldExpression><![CDATA[$F{id}]]></textFieldExpression>
        </textField>
    </band></detail>
</jasperReport>
```

Результат:

```groovy
import com.report_design_lib.core.ReportDsl
import com.report_design_lib.core.elements.*

def report = ReportDsl.report('CustomerReport') {
    title 'Customers'
    orientation PageOrientation.PORTRAIT

    parameters {
        param('StartDate', Date)
    }

    fields {
        field('id', Long)
    }

    queryString '''SELECT id FROM customers'''

    bands {
        titleBand {
            label(text: 'Customers', width: 400.0f, height: 20.0f)
        }
        detail {
            textField(expression: '$F{id}', width: 100.0f)
        }
    }
}

report
```

## Матрица конвертации JRXML → DSL

```
+--------------------------------------------+------------------------------------------+
| JRXML                                       | DSL                                      |
+--------------------------------------------+------------------------------------------+
| <jasperReport name="X">                    | ReportDsl.report('X') { ... }            |
| <parameter name class>                     | param('name', Type[, default])           |
| <field name class>                         | field('name', Type)                      |
| <queryString>                              | queryString '''...'''                    |
| <style name isBold isItalic fontSize>      | style('name') { font { ... } }           |
| <title><band>                              | titleBand { ... }                        |
| <columnHeader><band>                       | columnHeader { ... }                     |
| <detail><band>                             | detail { ... }                           |
| <summary><band>                            | summary { ... }                          |
| <staticText>                               | label(text:, style:, width:, height:)    |
| <textField>                                | textField(expression:, style:, width:)   |
| <line>                                     | line(y:, width:)                         |
+--------------------------------------------+------------------------------------------+
```

Тип из атрибута class приводится к короткому имени:

```
+--------------------------------------+-----------------+
| FQN в JRXML                          | Тип в DSL       |
+--------------------------------------+-----------------+
| java.lang.String                     | String          |
| java.lang.Long                       | Long            |
| java.lang.Integer                    | Integer         |
| java.util.Date                       | Date            |
| java.math.BigDecimal                 | BigDecimal      |
+--------------------------------------+-----------------+
```

## Ограничения

```
+-------------------------+------------------------------------------------+
| Не поддерживается       | Поведение конвертера                           |
+-------------------------+------------------------------------------------+
| <group> (группировка)   | игнорируется, в код добавляется комментарий    |
| <subreport>             | игнорируется, добавляется комментарий          |
| <chart> / <image>       | игнорируется, добавляется комментарий          |
| <variable>              | не объявляется; $V{...} остаётся литералом    |
| <pageHeader>/<pageFooter>| не эмитятся (в Bands пока нет этих слотов)    |
| Сложные выражения       | сохраняются как есть (резолвер DSL решает)     |
+-------------------------+------------------------------------------------+
```

## Расширение
### Новый элемент JRXML
1. В JasperXmlParser.parseElement() добавить ветку case '<имя>'.
2. В JrxmlToDslConverter.writeElement() добавить case '<kind>'.
3. Добавить тест в JrxmlToDslConverterSpec.

### Новая секция JRXML
1. Добавить имя секции в extractBands() (список ['title', 'columnHeader', ...]).
2. В JrxmlToDslConverter.bandMethod() сопоставить имя секции с методом DSL.
3. При необходимости добавить слот в Bands модуля report-design-core.

## Тесты
```bash
mvn -pl report-design-jasper-to-dsl test
```

Покрыто:

```text
+---------------------------------------+------------------------------------------+
| Тест                                  | Что проверяет                            |
+---------------------------------------+------------------------------------------+
| JasperXmlParserSpec                   | разбор параметров, полей, стилей, бэндов |
| JrxmlToDslConverterSpec               | текст сгенерированного DSL               |
| JrxmlToDslConverterSpec (compile)     | сгенерированный код компилируется        |
+---------------------------------------+------------------------------------------+
```

## Лицензия
Apache License 2.0 — см. файл LICENSE.

JasperReports® and JRXML are trademarks or registered trademarks of Cloud Software Group, Inc.
or its subsidiaries (formerly TIBCO Software Inc. / Jaspersoft Corporation).
This project is an independent converter tool and is not affiliated with, endorsed by,
or sponsored by Cloud Software Group, Inc.