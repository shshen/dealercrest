package com.dealercrest.template;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for ExpressionCompiler — verifies that the correct CompiledExpression
 * subtype is produced and that evaluation is correct.
 */
public class ExpressionCompilerTest {

    private DataModel ctx;

    @Before
    public void setUp() {
        ctx = new DataModel();
    }

    // ------------------------------------------------------------------ compile type selection

    @Test
    public void testNoExpressionProducesLiteral() {
        CompiledExpression expr = ExpressionCompiler.compile("plain text");
        assertTrue("Expected LiteralExpression", expr instanceof LiteralExpression);
    }

    @Test
    public void testNullInputProducesLiteral() {
        CompiledExpression expr = ExpressionCompiler.compile(null);
        assertTrue("Expected LiteralExpression for null", expr instanceof LiteralExpression);
        assertEquals("", expr.evaluate(ctx));
    }

    @Test
    public void testSingleExpressionProducesVariable() {
        CompiledExpression expr = ExpressionCompiler.compile("${title}");
        assertTrue("Expected VariableExpression", expr instanceof VariableExpression);
    }

    @Test
    public void testMixedTextProducesComposite() {
        CompiledExpression expr = ExpressionCompiler.compile("Hello ${name}!");
        assertTrue("Expected CompositeExpression", expr instanceof CompositeExpression);
    }

    @Test
    public void testMultipleVariablesProducesComposite() {
        CompiledExpression expr = ExpressionCompiler.compile("${first} ${last}");
        assertTrue("Expected CompositeExpression", expr instanceof CompositeExpression);
    }

    // ------------------------------------------------------------------ evaluation

    @Test
    public void testLiteralEvaluatesToConstant() {
        CompiledExpression expr = ExpressionCompiler.compile("hello");
        assertEquals("hello", expr.evaluate(ctx));
    }

    @Test
    public void testVariableResolvesFromContext() {
        ctx.set("city", "Tokyo");
        CompiledExpression expr = ExpressionCompiler.compile("${city}");
        assertEquals("Tokyo", expr.evaluate(ctx));
    }

    @Test
    public void testVariableMissingReturnsNull() {
        CompiledExpression expr = ExpressionCompiler.compile("${notSet}");
        assertNotNull("compile should not return null", expr);
        Object val = expr.evaluate(ctx);
        // VariableExpression returns null for missing keys
        assertTrue("missing variable should evaluate to null", val == null);
    }

    @Test
    public void testCompositeResolvesAllParts() {
        ctx.set("first", "John");
        ctx.set("last",  "Doe");
        CompiledExpression expr = ExpressionCompiler.compile("${first} ${last}");
        assertEquals("John Doe", expr.evaluate(ctx));
    }

    @Test
    public void testMixedLiteralAndVariable() {
        ctx.set("name", "World");
        CompiledExpression expr = ExpressionCompiler.compile("Hello ${name}!");
        assertEquals("Hello World!", expr.evaluate(ctx));
    }

    @Test
    public void testUnclosedExpressionTreatedAsLiteral() {
        // "${unclosed" has no closing brace — should not throw
        CompiledExpression expr = ExpressionCompiler.compile("${unclosed");
        assertNotNull(expr);
        String result = (String) expr.evaluate(ctx);
        assertNotNull(result);
    }

    // ------------------------------------------------------------------ compileVariable

    @Test
    public void testCompileVariableStripsDelimiters() {
        VariableExpression expr = ExpressionCompiler.compileVariable("${title}");
        ctx.set("title", "Test");
        assertEquals("Test", expr.evaluate(ctx));
    }

    @Test
    public void testCompileVariableWithoutDelimiters() {
        VariableExpression expr = ExpressionCompiler.compileVariable("title");
        ctx.set("title", "Direct");
        assertEquals("Direct", expr.evaluate(ctx));
    }

    @Test
    public void testCompileVariableDotPath() {
        VariableExpression expr = ExpressionCompiler.compileVariable("user.name");
        assertEquals("user", expr.getRootName());
        assertEquals(2, expr.getPath().length);
    }
}
