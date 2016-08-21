/**
 * Copyright (C) 2015  the original author or authors.
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

/**
 * <p>Generate LOD (Logical Operator Deletion) mutants --
 *    delete each occurrence of bitwise logical operators 
 *    (bitwise and-&, bitwise or-|, exclusive or-^)
 * </p>
 * @author Yu-Seung Ma
 * @version 1.0
 */

public class LOD extends InstrumentationParser {
    public LOD(FileEnvironment file_env, ClassDeclaration cdecl, CompilationUnit comp_unit) {
        super(file_env, comp_unit);
    }

    public void visit(UnaryExpression p) throws ParseTreeException {
        if (mutExpression == null) mutExpression = p;

        int op = p.getOperator();
        if (op == UnaryExpression.BIT_NOT) {
            // original
            typeStack.add(getType(p));
            exprStack.add(new UnaryExpression(genVar(counter+2), UnaryExpression.BIT_NOT)); // +0
            // mutant
            typeStack.add(getType(p));
            exprStack.add(genVar(counter+2)); // +1
            // expression
            typeStack.add(getType(p));
            exprStack.add(p.getExpression()); // +2
            counter += 3;

            outputToFile(p);

            pop(3);
        }

        if(mutExpression.getObjectID() == p.getObjectID()) mutExpression = null;
    }

    /**
     * Output LOD mutants to files
     * @param original
     */
    public void outputToFile(UnaryExpression original) {
        if (comp_unit == null)
            return;

        String f_name;
        num++;
        f_name = getSourceName("LOD");
        String mutant_dir = getMuantID("LOD");

        try {
            PrintWriter out = getPrintWriter(f_name);
            //PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
            InstrumentationCodeWriter writer = new InstrumentationCodeWriter(mutant_dir, out);

            writer.setEnclose(encBlock);
            writer.setBlock(mutBlock);
            writer.setStatement(mutStatement);
            writer.setExpression(mutExpression);
            writer.setInstrument(genInstrument());
            writer.setMethodSignature(currentMethodSignature);

            comp_unit.accept(writer);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.err.println("fails to create " + f_name);
        } catch (ParseTreeException e) {
            System.err.println("errors during printing " + f_name);
            e.printStackTrace();
        }
    }
}
