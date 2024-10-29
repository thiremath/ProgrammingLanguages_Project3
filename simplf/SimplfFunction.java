package simplf;

import java.util.List;

class SimplfFunction implements SimplfCallable {
    final Stmt.Function declaration;
    private Environment closureEnv;

    SimplfFunction(Stmt.Function functionDecl, Environment enclosingEnv) {
        this.declaration = functionDecl;
        this.closureEnv = enclosingEnv;
    }

    public void setClosure(Environment newEnvironmentIn) {
        this.closureEnv = newEnvironmentIn;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> callArgs) {
        Environment TempEnv = new Environment(closureEnv);

        for (int i=0; i < declaration.params.size(); i++) {
            TempEnv = TempEnv.define(declaration.params.get(i), declaration.params.get(i).lexeme, callArgs.get(i));
        }

        return interpreter.runBlock(declaration.body, TempEnv);
    }

    @Override
    public String toString() {
        return "<fn >";
    }

}