package com.strumenta.entity.python

import com.strumenta.entity.parser.EntityParser
import com.strumenta.python.codegen.PyNode
import com.strumenta.python.codegen.PythonCodeGenerator

class EntityToPythonTranspiler {

    fun transpile(code: String): String =
        EntityParser().parse(code)
            .let { EntityToPythonAstTransformer().transform(it.root!!) }
            .let { PythonAstImportManager().transform(it) }
            .let { PythonCodeGenerator().generateToString(it as PyNode) }
}

fun main() {
    val entityCode = """
        module School {
            
            entity Professor {
                first_name: string;
                last_name: string;
                age: integer;
                funny: boolean;
            }
            
            entity Course {
                name: string;
                professor: Professor;
            }
            
        }
    """.trimIndent()
    val pythonCode = EntityToPythonTranspiler().transpile(entityCode)

    println("********************")
    println(entityCode)
    println("********************")
    println(pythonCode)
    println("********************")
}
