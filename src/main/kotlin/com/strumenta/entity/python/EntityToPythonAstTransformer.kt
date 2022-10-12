package com.strumenta.entity.python

import com.strumenta.entity.parser.*
import com.strumenta.kolasu.transformation.ASTTransformer
import com.strumenta.kolasu.validation.Issue
import com.strumenta.kolasu.validation.IssueSeverity
import com.strumenta.python.codegen.*

class EntityToPythonAstTransformer(issues: MutableList<Issue> = mutableListOf()) : ASTTransformer(issues, false) {

    init {
        registerModuleMapping()
        registerEntityMapping()
        registerFeatureMapping()
        registerTypeMappings()
        registerExpressionMappings()
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
                simple = 0
            )
        }
            .withChild(Feature::type, PyAnnAssign::annotation)
            .withChild(Feature::value, PyAnnAssign::value)
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

    private fun registerExpressionMappings() {
        this.registerNodeFactory(LiteralExpression::class) { expression -> PyConstantExpr(expression.value!!) }
        this.registerNodeFactory(BinaryExpression::class) { expression ->
            PyBinOp(
                left = PyConstantExpr(""),
                right = PyConstantExpr(""),
                op = when (expression.operator) {
                    BinaryOperator.SUM -> PyOperator.Add
                    BinaryOperator.SUB -> PyOperator.Sub
                    BinaryOperator.MUL -> PyOperator.Mult
                    BinaryOperator.DIV -> PyOperator.Div
                    else -> {
                        issues.add(Issue.translation(
                            "Unable to translate binary operator: ${expression.operator}",
                            IssueSeverity.ERROR,
                            expression.position
                        ))
                        PyOperator.Add
                    }
                })
        }
            .withChild(BinaryExpression::left, PyBinOp::left)
            .withChild(BinaryExpression::right, PyBinOp::right)
        this.registerNodeFactory(FqnExpression::class) { expression ->
            var fqnName = expression.target.name
            var context = expression.context
            while (context != null) {
                fqnName = "${context.target.name}.${fqnName}"
                context = context.context
            }
            PyName(
                id = fqnName,
                ctx = PyExprContext.Load
            )
        }
    }
}
