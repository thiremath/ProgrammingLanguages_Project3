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
        for (AssocList list = currAssocList; list != null; list = list.next) {
            if (list.name.equals(name.lexeme)) {
                list.value = value;
                return;
            }
        }
        if (EnclosingEnv != null) {
            EnclosingEnv.assign(name, value);
        } else {
            throw new RuntimeException("Undefined variable" + name.lexeme + ".");
        }
    }

    Object get(Token name) {
        for (AssocList list = currAssocList; list != null; list = list.next) {
            if (list.name.equals(name.lexeme)) {
                return list.value;
            }
        }
        if (EnclosingEnv != null) {
            return EnclosingEnv.get(name);
        }
        throw new RuntimeException("Undefined variable" + name.lexeme + ".");
    }
}

