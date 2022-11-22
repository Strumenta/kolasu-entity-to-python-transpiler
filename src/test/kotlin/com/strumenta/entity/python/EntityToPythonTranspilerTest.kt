package com.strumenta.entity.python

import org.junit.Test

internal class EntityToPythonTranspilerTest {

    @Test
    fun testTranspiler() {
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
}
