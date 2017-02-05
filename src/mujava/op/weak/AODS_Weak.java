/**
 * Copyright (C) 2017 the original author or authors.
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
 * <p>Generate AODS (Arithmetic Operator Deletion (Short-cut)) mutants --
 *    delete each occurrence of an increment operator (++) or a decrement
 *    operator (--)
 * </p>
 * @author Haoyuan Sun
 * @version 0.1a
 */

public class AODS_Weak extends InstrumentationParser {
    public AODS_Weak(FileEnvironment file_env, ClassDeclaration cdecl, CompilationUnit comp_unit) {
        super(file_env, comp_unit);
    }

    public void visit(UnaryExpression p) throws ParseTreeException {
        if (mutExpression == null) mutExpression = p;

        int op = p.getOperator();
        if (op < 4) {
            typeStack.add(getType(p));
            exprStack.add(p); // +0
            typeStack.add(getType(p));
            exprStack.add(p.getExpression()); // +1

            counter += 2;

            boolean kill = true;
            if((p.getOperator() == UnaryExpression.POST_DECREMENT ||
                    p.getOperator() == UnaryExpression.PRE_DECREMENT) &&
                    mutStatement instanceof ReturnStatement)
                kill = false;


            outputToFile(kill);

            pop(2);
        }

        if(mutExpression.getObjectID() == p.getObjectID()) mutExpression = null;
    }

    /**
     * Output AODS mutants to files
     */
    public void outputToFile(boolean kill) {
        if (comp_unit == null)
            return;

        String f_name;
        num++;
        f_name = getSourceName("AODS");
        String mutant_dir = getMuantID("AODS");

        try {
            PrintWriter out = getPrintWriter(f_name);
            //PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
            InstrumentationCodeWriter writer = new InstrumentationCodeWriter(mutant_dir, out);

            writer.setPreKill(kill ? 1 : 2);
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