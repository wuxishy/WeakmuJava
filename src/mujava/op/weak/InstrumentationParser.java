/**
 * Copyright (C) 2016 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mujava.op.weak;

import openjava.mop.*;
import openjava.ptree.*;

import java.io.*;
import java.util.Stack;

import mujava.MutationSystem;

/**
 * <p>Recursively evaluate an expression</p>
 * <p>
 * Refer to <a href=https://www.sharelatex.com/project/577c5c8abfe51f234ad0c329>the planning document</a>
 * for exact details on the recursive parser
 * </p>
 * @author Haoyuan Sun
 * @version 0.1a
 */

public abstract class InstrumentationParser extends InstrumentationMutator{
    public InstrumentationParser(FileEnvironment file_env, CompilationUnit comp_unit) {
        super(file_env, comp_unit);

        typeStack = new Stack<OJClass>();
        exprStack = new Stack<Expression>();
        post = new StatementList();
    }

    // count the number of extra variables being used
    protected int counter = 0;

    // keep the operands of the expression in a stack
    protected Stack<OJClass> typeStack;
    protected Stack<Expression> exprStack;

    protected StatementList post;

    public void visit(BinaryExpression p) throws ParseTreeException {
        if (mutExpression == null) mutExpression = p;

        typeStack.add(getType(p));
        exprStack.add(new BinaryExpression(new Variable(InstConfig.varPrefix+(counter+3)), p.getOperator(),
                new Variable(InstConfig.varPrefix+(counter+2)))); // +0
        typeStack.add(getType(p));
        exprStack.add(new BinaryExpression(new Variable(InstConfig.varPrefix+(counter+4)), p.getOperator(),
                new Variable(InstConfig.varPrefix+(counter+2)))); // +1
        typeStack.add(getType(p.getRight()));
        exprStack.add(p.getRight()); // +2

        counter += 3;
        p.getLeft().accept(this);

        pop(3);

        typeStack.add(getType(p));
        exprStack.add(new BinaryExpression(new Variable(InstConfig.varPrefix+(counter+2)), p.getOperator(),
                new Variable(InstConfig.varPrefix+(counter+3)))); // +0
        typeStack.add(getType(p));
        exprStack.add(new BinaryExpression(new Variable(InstConfig.varPrefix+(counter+2)), p.getOperator(),
                new Variable(InstConfig.varPrefix+(counter+4)))); // +1
        typeStack.add(getType(p.getLeft()));
        exprStack.add(p.getLeft()); // +2

        counter += 3;
        p.getRight().accept(this);

        pop(3);

        if (mutExpression == p) mutExpression = null;
    }

    // combine typeStack and exprStack into a list of statements ready to be printed
    public Instrument genInstrument(){
        Instrument inst = new Instrument();
        for(int i = counter-1; i >= 0; --i){
            inst.init.add(new VariableDeclaration(TypeName.forOJClass(typeStack.get(i)),
                    InstConfig.varPrefix+i, exprStack.get(i)));
        }
        inst.addAssertion(InstConfig.varPrefix+0, InstConfig.varPrefix+1);
        inst.post = post;
        inst.varName = InstConfig.varPrefix+0;

        return inst;
    }

    // pop the top k elements in the expression stack
    protected void pop(int k){
        for(int i = 0; i < k; ++i){
            typeStack.pop();
            exprStack.pop();
            --counter;
        }
    }
}
