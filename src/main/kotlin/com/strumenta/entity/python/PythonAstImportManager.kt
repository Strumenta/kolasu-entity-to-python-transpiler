package com.strumenta.entity.python

import com.strumenta.kolasu.model.Node
import com.strumenta.kolasu.model.Origin
import com.strumenta.kolasu.transformation.ASTTransformer
import com.strumenta.kolasu.traversing.searchByType
import com.strumenta.kolasu.validation.Issue
import com.strumenta.python.codegen.PyAlias
import com.strumenta.python.codegen.PyImportFrom
import com.strumenta.python.codegen.PyModule
import com.strumenta.python.codegen.PyName
import kotlin.reflect.KClass

class PythonAstImportManager(issues: MutableList<Issue> = mutableListOf()) : ASTTransformer(issues) {

    init {
        this.registerNodeFactory(PyModule::class) { module ->
            module.apply {
                val imports = mutableListOf<PyImportFrom>()
                imports.add(PyImportFrom("__future__", names = listOf(PyAlias(name = "annotations"))))
                if (module.searchByType(PyName::class.java).any { it.id == "field" }) {
                    imports.add(PyImportFrom(module = "dataclasses", names = listOf(PyAlias(name = "field"))))
                }
                this.body = imports + this.body
            }
        }
    }

    override fun asOrigin(source: Any): Origin? {
        return (source as Node).origin
    }

    private fun <S : Node> registerIdentityMapping(kclass: KClass<S>) =
        this.registerNodeFactory(kclass) { source -> source }
}
