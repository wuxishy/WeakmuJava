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

import java.util.ArrayList;

import openjava.mop.*;
import openjava.ptree.*;

/**
 * <p>Storage</p>
 *
 * @author Haoyuan Sun
 * @version 0.1a
 */

public class Instrument {
    public StatementList init;
    public ArrayList<String> assertion;
    public StatementList post;

    public Instrument() {
        init = new StatementList();
        assertion = new ArrayList<String>();
        post = new StatementList();
    }

    public void addAssertion(String a, String b) {
        String if_t = (assertion.isEmpty()) ? "if (" : "else if (";
        assertion.add(if_t + a + " != " + b + " ) throw new WeakKillException();");
    }

    public void addFloatAssertion(String a, String b) {
        String if_t = (assertion.isEmpty()) ? "if (" : "else if (";
        assertion.add(if_t + "Math.abs(" + a + " - " + b + ") > " + weakConfig.eps +
            ") throw new WeakKillException();");
    }

    // Hack: because java thinks "unreachable code" is a compile error.
    // Tested up to Java 8, not guaranteed to work in future versions of Java.
    public static String exit = "if (true) throw new WeakLiveException();";

    public static class WeakKillException extends RuntimeException{
        public WeakKillException(){
            super("This mutant is weakly killed");
        }
    }

    public static class WeakLiveException extends RuntimeException{
        public WeakLiveException(){
            super("This mutant is not weakly killed");
        }
    }
}
