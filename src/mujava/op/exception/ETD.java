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
package mujava.op.exception;

import java.io.*;

import openjava.mop.*;
import openjava.ptree.*;

/**
 * <p>Description: </p>
 * @author Yu-Seung Ma
 * @version 1.0
 */

public class ETD extends mujava.op.util.Mutator {
    public ETD(FileEnvironment file_env, ClassDeclaration cdecl,
               CompilationUnit comp_unit) {
        super(file_env, comp_unit);
    }

    public void visit(ThrowStatement p) throws ParseTreeException {
        outputToFile(p);
    }

    public void outputToFile(ThrowStatement original) {
        if (comp_unit == null) return;

        String f_name;
        num++;
        f_name = getSourceName(this);
        String mutant_dir = getMuantID();

        try {
            PrintWriter out = getPrintWriter(f_name);
            ETD_Writer writer = new ETD_Writer(mutant_dir, out);
            writer.setMutant(original);
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
