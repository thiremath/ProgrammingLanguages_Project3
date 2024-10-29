package simplf; 

class Environment {
    Environment EnclosingEnv;
    AssocList currAssocList;

    Environment() {
        this.EnclosingEnv = null;
        this.currAssocList = null;
    }

    Environment(Environment enclosing) {
        this.EnclosingEnv = enclosing;
        this.currAssocList = null;
    }

    Environment(AssocList assocList, Environment enclosing) {
        this.EnclosingEnv = enclosing;
        this.currAssocList = assocList;
    }

    // Return a new version of the environment that defines the variable "name"
    // and sets its initial value to "value". Take care to ensure the proper aliasing
    // relationship. There is an association list implementation in Assoclist.java.
    // If your "define" function adds a new entry to the association list without
    // modifying the previous list, this should yield the correct aliasing
    // relationsip.
    //
    // For example, if the original environment has the association list
    // [{name: "x", value: 1}, {name: "y", value: 2}]
    // the new environment after calling define(..., "z", 3) should have the following
    //  association list:
    // [{name: "z", value: 3}, {name: "x", value: 1}, {name: "y", value: 2}]
    // This should be constructed by building a new class of type AssocList whose "next"
    // reference is the previous AssocList.
    Environment define(Token varToken, String name, Object value) {
        currAssocList = new AssocList(name, value, currAssocList);
        return this;        
    }

    void assign(Token name, Object value) {
        Environment it = this;
        
        while (it != null) {
            boolean flag = false;
            for (AssocList currit = it.currAssocList; currit != null; currit = currit.next) {
                if (currit.name.equals(name.lexeme)) {
                    currit.value = value;
                    flag = true;
                    break;
                }
            }
            
            if (flag) {
                return;
            }
            it = it.EnclosingEnv;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'");
    }

    public Object get(Token identifier) {
        Environment envIterator = this;
        do {
            AssocList listit = envIterator.currAssocList;
            while (listit != null) {
                if (listit.name.equals(identifier.lexeme))  return listit.value;
                listit = listit.next;
            }
            envIterator = envIterator.EnclosingEnv;
        } 
        while (envIterator != null); 

        throw new RuntimeException("Undefined variable '" + identifier.lexeme + "'");
    }
}

