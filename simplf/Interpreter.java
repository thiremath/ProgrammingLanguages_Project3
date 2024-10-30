package simplf; 

import java.util.List;

import simplf.Stmt.For;
import java.util.ArrayList;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Object> {
    public Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {

    }

    public void interpret(List<Stmt> stmts) {
        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Simplf.runtimeError(error);
        }
    }

    @Override
    public Object visitExprStmt(Stmt.Expression stmt) {
        return evaluate(stmt.expr);
    }

    @Override
    public Object visitPrintStmt(Stmt.Print stmt) {
        Object val = evaluate(stmt.expr);
        System.out.println(stringify(val));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object initData = null;
        if (stmt.initializer != null) {
            initData = evaluate(stmt.initializer);
        }
        environment = environment.define(stmt.name, stmt.name.lexeme, initData);
        return null;
    }

    @Override
    public Object visitBlockStmt(Stmt.Block stmt) {
        return runBlock(stmt.statements, new Environment(environment));
    }

    public Object runBlock(List<Stmt> statementsInBlock, Environment envBlock) {
        Environment prevEnv = this.environment;
        Object ret = null;
        try {
            this.environment = envBlock;
            for (int i=0;i < statementsInBlock.size();i++) {
                ret = execute(statementsInBlock.get(i));
            }
        } finally {
            this.environment = prevEnv;
        }
        return ret;
    }

    @Override
    public Object visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.cond))) {
            return execute(stmt.thenBranch);
        } 
        else if (stmt.elseBranch != null) {
            return execute(stmt.elseBranch);
        }
        return null;
    }

    @Override
    public Object visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.cond))) {
            execute(stmt.body);
        }
        return null;
    }

    @Override
    public Object visitForStmt(For stmt) {
        if (stmt.init != null) {
            evaluate(stmt.init);
        }
        while (stmt.cond == null || isTruthy(evaluate(stmt.cond))) {
            execute(stmt.body);
            if (stmt.incr != null) {
                evaluate(stmt.incr);
            }
        }
        return null;
    }

    @Override
    public Object visitFunctionStmt(Stmt.Function stmt) {
        SimplfFunction TempdefinedFunction = new SimplfFunction(stmt, environment);
        environment = environment.define(stmt.name, stmt.name.lexeme, TempdefinedFunction);
        return TempdefinedFunction;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.op.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitBinary(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.op.type) {
            case PLUS:
                if (left instanceof String || right instanceof String) {
                    return stringify(left) + stringify(right);
                }
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                throw new RuntimeError(expr.op, "Addition operation not supported for operands.");
            case LESS:
                checkNumbers(expr.op, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumbers(expr.op, left, right);
                return (double) left <= (double) right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case COMMA:
                return right;
            case MINUS:
                checkNumbers(expr.op, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumbers(expr.op, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumbers(expr.op, left, right);
                if ((double) right == 0) {
                    throw new RuntimeError(expr.op, "Cannot divide by zero.");
                }
                return (double) left / (double) right;
            case GREATER:
                checkNumbers(expr.op, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumbers(expr.op, left, right);
                return (double) left >= (double) right;
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitUnary(Expr.Unary expr) {
        Object right = evaluate(expr.right);
        switch (expr.op.type) {
            case MINUS:
                checkNumber(expr.op, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                break;
        }
        return null;
    }

    @Override
    public Object visitLiteral(Expr.Literal expr) {
        return expr.val;
    }

    @Override
    public Object visitGrouping(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitVarExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }
    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object calledFunc = evaluate(expr.callee);

        if ((calledFunc instanceof SimplfCallable)) {
            List<Object> paramsList = new ArrayList<>();

            for (int i=0;i < expr.args.size();i++) {
                paramsList.add(evaluate(expr.args.get(i)));
            }

            SimplfCallable callableObj = (SimplfCallable) calledFunc;
            if (paramsList.size() != ((SimplfFunction) callableObj).declaration.params.size()) {
                throw new RuntimeError(expr.paren, "Expected " + ((SimplfFunction) callableObj).declaration.params.size() + " arguments but got " + paramsList.size() + ".");
            }

            return callableObj.call(this, paramsList);
        }

        throw new RuntimeError(expr.paren, "Here, only functions can be called.");
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object GeneratedValue = evaluate(expr.value);
        environment.assign(expr.name, GeneratedValue);
        return GeneratedValue;
    }

    @Override
    public Object visitConditionalExpr(Expr.Conditional expr) {
        if (isTruthy(evaluate(expr.cond))) {
            return evaluate(expr.thenBranch);
        } else {
            return evaluate(expr.elseBranch);
        }
    }

    private Object execute(Stmt stmt) {
        return stmt.accept(this);
    }

    public Object evaluateExpression(Expr expression) {
        return evaluate(expression);
    }
    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean) object;
        }
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null)
            return b == null;
        return a.equals(b);
    }

    private void checkNumber(Token op, Object object) {
        if (object instanceof Double)
            return;
        throw new RuntimeError(op, "Operand must be a number");
    }

    private void checkNumbers(Token op, Object a, Object b) {
        if (a instanceof Double && b instanceof Double)
            return;
        throw new RuntimeError(op, "Operand must be numbers");
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";
        if (object instanceof Double) {
            String num = object.toString();
            if (num.endsWith(".0")) {
                num = num.substring(0, num.length() - 2);
            }
            return num;
        }
        return object.toString();
    }


} 