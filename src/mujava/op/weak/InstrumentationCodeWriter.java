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
import java.util.ArrayList;

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

    // indicate which block was mutated
    private Statement mutBlock;

    // indicate which statement was mutated
    private Statement mutStatement;

    // indicate which expression was mutated
    private Expression mutExpression;

    // instrumentation code
    private Instrument inst;

    // rename variables for for loops
    private ArrayList<String> itName;

    public InstrumentationCodeWriter(PrintWriter out) {
        super(out);
    }

    public InstrumentationCodeWriter(String mutant_dir, PrintWriter out) {
        super(mutant_dir, out);
    }

    public void setPreKill(int p) { preKill = p; }

    public void setBlock(Statement s) { mutBlock = s; }

    public void setExpression(Expression e) { mutExpression = e; }

    public void setStatement(Statement s) { mutStatement = s; }

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
        for (int i = 0; i < islst.length; ++i) {
            out.println("import " + islst[i] + ";");
            line_num++;
        }

        // weak mutation kill and live
        out.println("import static mujava.op.weak.Instrument.*;");
        line_num++;

        out.println();
        line_num++;
        
        /* type declaration list */
        ClassDeclarationList tdlst = p.getClassDeclarations();
        tdlst.accept(this);
    }

    public void visit(ExpressionStatement p) throws ParseTreeException {
        if(isSameObject(mutStatement, p)) {
            super.visit(inst.init);
            for (String str : inst.assertion) writeString(str);
            super.visit(inst.post);
            if(mutBlock == null) writeString(Instrument.exit);
        }
        else super.visit(p);
    }

    public void visit(ForStatement p) throws ParseTreeException {
        if (!isSameObject(mutBlock, p)) {
            super.visit(p);
            return;
        } else if (!isSameObject(mutStatement, p)){
            super.visit(p);
            writeString(Instrument.exit);
            return;
        }

        ExpressionList init = p.getInit();
        TypeName tspec = p.getInitDeclType();
        VariableDeclarator[] vdecls = p.getInitDecls();

        out.println();
        line_num++;

        if (init != null && (!init.isEmpty())) {
            for (int i = 0; i < init.size(); ++i) {
                writeTab();
                init.get(i).accept(this);
                out.println(";");
                line_num++;
            }
        } else if (tspec != null && vdecls != null && vdecls.length != 0) {
            itName = new ArrayList<String>();

            for (int i = 0; i < vdecls.length; ++i) {
                writeTab();
                tspec.accept(this);
                out.print(" ");
                itName.add(vdecls[i].getVariable());
                writeNewName(vdecls[i], i);
                out.println(";");
                line_num++;
            }
        }

        writeTab();
        out.println("while (true) {");
        line_num++;
        pushNest();

        Expression expr = p.getCondition();
        if (expr != null) {
            writeTab();
            out.print("if (!(");
            expr.accept(this);
            out.print(")) break");
        }

        out.println(";");
        line_num++;
        out.println();
        line_num++;

        StatementList stmts = p.getStatements();
        if (stmts.isEmpty()) {
            writeTab();
            out.println(";");
            line_num++;
        } else {
            stmts.accept(this);
        }
        out.println();
        line_num++;

        ExpressionList incr = p.getIncrement();
        if (incr != null && (!incr.isEmpty())) {
            for (int i = 0; i < incr.size(); ++i) {
                if(isSameObject(incr.get(i), mutExpression)){
                    super.visit(inst.init);
                    for (String str : inst.assertion) writeString(str);
                    super.visit(inst.post);
                } else {
                    writeTab();
                    incr.get(i).accept(this);
                    out.println(";");
                    line_num++;
                }
            }
        }

        popNest();
        writeTab();
        out.println("}");
        line_num++;

        writeString(Instrument.exit);

        itName = null;
    }

    public void visit(Variable p) throws ParseTreeException {
        String name = p.toString();

        if (itName != null){
            for(int i = 0; i < itName.size(); ++i)
                if (itName.get(i).equals(name)) {
                    out.print(weakConfig.varPrefix + "FOR_" + i);
                    return;
                }
        }
        out.print(name);
    }

    private void writeString(String str) {
        writeTab();

        out.println(str);
        line_num++;
    }

    private void writeNewName(VariableDeclarator p, int index)
            throws ParseTreeException{
        out.print(weakConfig.varPrefix + "FOR_" + index);

        for (int i = 0; i < p.getDimension(); ++i) {
            out.print("[]");
        }

        VariableInitializer varinit = p.getInitializer();
        if (varinit != null) {
            out.print(" = ");
            varinit.accept(this);
        }
    }
}
