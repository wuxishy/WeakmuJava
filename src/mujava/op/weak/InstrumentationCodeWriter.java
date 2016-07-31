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

import java.io.*;

import mujava.op.util.TraditionalMutantCodeWriter;
import openjava.ptree.*;

/**
 * <p>Description: Writes the mutated statement and its associated instrumentation 
 *    for weak mutation testing 
 * </p>
 * @author Haoyuan Sun
 * @version 0.1a
 */


public class InstrumentationCodeWriter extends TraditionalMutantCodeWriter {
    // some mutants are always killed or always live
    // 1 -- always killed, 2 -- always live, 0 -- run test
    private int preKill = 0;

    // indicate if the mutant is in a block (if, while, switch-case, for)
    private Statement curBlock;
    // 0 -- in block body or not in a block
    // 1 -- for loop instantiation, 2 -- condition, 3 -- for loop increment
    private int blockType;

    // indicate which statement was mutated
    private Statement curStatement;

    // instrumentation code
    private Instrument inst;

    public InstrumentationCodeWriter(PrintWriter out) {
        super(out);
    }

    public InstrumentationCodeWriter(String mutant_dir, PrintWriter out) {
        super(mutant_dir, out);
    }

    public void setPreKill(int p) { preKill = p; }

    public void setBlock(Statement b, int t) { curBlock = b; blockType = t; }

    public void setStatement(Statement s) { curStatement = s; }

    public void setInstrument(Instrument i) { inst = i; }

    // set a preKill flag in the first line of code
    public void visit(CompilationUnit p)
            throws ParseTreeException {
        out.println("// PREKILL: " + preKill);
        line_num++;
        out.println("// This is an instrumented mutant program.");
        line_num++;
        out.println();
        line_num++;


        /* package statement */
        String qn = p.getPackage();
        if (qn != null) {
            out.print("package " + qn + ";");
            out.println();
            line_num++;

            out.println();
            line_num++;
            out.println();
            line_num++;
        }

        /* import statement list */
        String[] islst = p.getDeclaredImports();
        if (islst.length != 0) {
            for (int i = 0; i < islst.length; ++i) {
                out.println("import " + islst[i] + ";");
                line_num++;
            }
            out.println();
            line_num++;
            out.println();
            line_num++;
        }
        
        /* type declaration list */
        ClassDeclarationList tdlst = p.getClassDeclarations();
        tdlst.accept(this);
    }

    // exception hack xD
    public void visit(MethodDeclaration p)
            throws ParseTreeException {

        writeTab();

        /*ModifierList*/
        ModifierList modifs = p.getModifiers();
        if (modifs != null) {
            modifs.accept(this);
            if (!modifs.isEmptyAsRegular()) out.print(" ");
        }

        //print generics type parameters
        TypeParameterList tpl = p.getTypeParameterList();
        if (tpl != null)
            tpl.accept(this);
        out.print(" ");

        TypeName ts = p.getReturnType();
        ts.accept(this);

        out.print(" ");

        String name = p.getName();
        out.print(name);

        ParameterList params = p.getParameters();
        out.print("(");
        if (!params.isEmpty()) {
            out.print(" ");
            params.accept(this);
            out.print(" ");
        } else {
            params.accept(this);
        }
        out.print(")");

        out.println();
        line_num++;
        writeTab();
        writeTab();
        out.print("throws Exception");


        StatementList bl = p.getBody();
        if (bl == null) {
            out.print(";");
        } else {
            out.println();
            line_num++;
            writeTab();
            out.print("{");
            out.println();
            line_num++;
            pushNest();
            bl.accept(this);
            popNest();
            writeTab();
            out.print("}");
        }

        out.println();
        line_num++;
    }

    public void visit(ExpressionStatement p) throws ParseTreeException {
        if(isSameObject(curStatement, p)) {
            //System.out.println("DEBUG_1");
            //System.out.println(inst.init.toString());
            //System.out.println(inst.post.toString());
            super.visit(inst.init);
            writeString(inst.assertion);
            super.visit(inst.post);
        }
        else super.visit(p);
    }

    public void writeString(String str) {
        writeTab();

        out.println(str);
        line_num++;
    }
}
