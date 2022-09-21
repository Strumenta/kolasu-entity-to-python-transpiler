package com.strumenta.entity.python

import com.strumenta.entity.parser.BooleanType
import com.strumenta.entity.parser.Entity
import com.strumenta.entity.parser.EntityRefType
import com.strumenta.entity.parser.Feature
import com.strumenta.entity.parser.IntegerType
import com.strumenta.entity.parser.Module
import com.strumenta.entity.parser.StringType
import com.strumenta.kolasu.transformation.ASTTransformer
import com.strumenta.kolasu.validation.Issue
import com.strumenta.python.codegen.PyAnnAssign
import com.strumenta.python.codegen.PyCall
import com.strumenta.python.codegen.PyClassDef
import com.strumenta.python.codegen.PyConstantExpr
import com.strumenta.python.codegen.PyExprContext
import com.strumenta.python.codegen.PyKeyword
import com.strumenta.python.codegen.PyModule
import com.strumenta.python.codegen.PyName

class EntityToPythonAstTransformer(issues: MutableList<Issue> = mutableListOf()) : ASTTransformer(issues) {

    init {
        registerModuleMapping()
        registerEntityMapping()
        registerFeatureMapping()
        registerTypeMappings()
    }

    private fun registerModuleMapping() {
        this.registerNodeFactory(Module::class, PyModule::class)
            .withChild(Module::entities, PyModule::body)
    }

    private fun registerEntityMapping() {
        this.registerNodeFactory(Entity::class) { entity -> PyClassDef(name = entity.name) }
            .withChild(Entity::features, PyClassDef::body)
    }

    private fun registerFeatureMapping() {
        this.registerNodeFactory(Feature::class) { feature ->
            PyAnnAssign(
                target = PyName(id = feature.name!!, ctx = PyExprContext.Load),
                value = PyCall(
                    func = PyName(id = "field", ctx = PyExprContext.Load),
                    keywords = listOf(PyKeyword(arg = "default", value = PyConstantExpr(value = "None")))
                ),
                simple = 0
            )
        }.withChild(Feature::type, PyAnnAssign::annotation)
    }

    private fun registerTypeMappings() {
        this.registerNodeFactory(StringType::class) { _ -> PyName(id = "str", ctx = PyExprContext.Load) }
        this.registerNodeFactory(BooleanType::class) { _ -> PyName(id = "bool", ctx = PyExprContext.Load) }
        this.registerNodeFactory(IntegerType::class) { _ -> PyName(id = "int", ctx = PyExprContext.Load) }
        this.registerNodeFactory(EntityRefType::class) { entityRefType ->
            PyName(
                id = entityRefType.target.name,
                ctx = PyExprContext.Load
            )
        }
    }

    // override fun asOrigin(source: Any): Origin? {
    //     return (source as Node).origin
    // }
}
