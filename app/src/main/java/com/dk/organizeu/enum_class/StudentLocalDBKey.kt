package com.dk.organizeu.enum_class

enum class StudentLocalDBKey(val displayName: String) {
    ID("studentId"),
    NAME("studentName"),
    ACADEMIC_YEAR("studentAcademicYear"),
    ACADEMIC_TYPE("studentAcademicType"),
    SEMESTER("studentSemester"),
    CLASS("studentClass"),
    BATCH("studentBatch"),
    REMEMBER("rememberStudentId")

}