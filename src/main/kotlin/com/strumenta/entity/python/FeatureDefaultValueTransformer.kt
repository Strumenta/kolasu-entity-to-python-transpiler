package com.strumenta.entity.python

import com.strumenta.kolasu.model.Node
import com.strumenta.kolasu.model.Origin
import com.strumenta.kolasu.transformation.ASTTransformer
import com.strumenta.kolasu.traversing.searchByType
import com.strumenta.kolasu.validation.Issue
import com.strumenta.python.codegen.*
import kotlin.reflect.KClass

class FeatureDefaultValueTransformer(issues: MutableList<Issue> = mutableListOf()) : ASTTransformer(issues, false) {

    init {
        registerIdentityMapping(PyModule::class)
            .withChild(PyModule::body, PyModule::body)

        registerIdentityMapping(PyClassDef::class)
            .withChild(PyClassDef::body, PyClassDef::body)

        this.registerNodeFactory(PyAnnAssign::class) { annAssign ->
            annAssign.apply {
                if (annAssign.value == null) {
                    annAssign.value = PyCall(
                        func = PyName(id = "field", ctx = PyExprContext.Load),
                        keywords = listOf(PyKeyword(arg = "default", value = PyConstantExpr(value = "None"))))
                }
            }
        }
    }

    override fun asOrigin(source: Any): Origin? {
        return (source as Node).origin
    }

    private fun <S : Node> registerIdentityMapping(kclass: KClass<S>) =
        this.registerNodeFactory(kclass) { source -> source }
}
