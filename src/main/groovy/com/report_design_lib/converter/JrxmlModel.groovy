package com.report_design_lib.converter

class JrxmlModel {
    String name
    String title
    Float pageWidth
    Float pageHeight
    String orientation
    String query
    List<Map> parameters = []
    List<Map> fields = []
    List<Map> styles = []
    List<Map> bands = []
}

