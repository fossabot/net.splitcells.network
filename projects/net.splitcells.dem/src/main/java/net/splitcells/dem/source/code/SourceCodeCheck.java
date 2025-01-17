/*
 * Copyright (c) 2021 Mārtiņš Avots (Martins Avots) and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the MIT License,
 * which is available at https://spdx.org/licenses/MIT.html.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * or any later versions with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR MIT OR GPL-2.0-or-later WITH Classpath-exception-2.0
 */
package net.splitcells.dem.source.code;

import net.splitcells.dem.resource.host.Files;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import javax.xml.transform.Source;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static net.splitcells.dem.resource.host.Files.walk_recursively;
import static net.splitcells.dem.utils.NotImplementedYet.notImplementedYet;

public class SourceCodeCheck {
    public static void main(String... arg) {
        check_Java_source_code(Paths.get("src/main/java/net/splitcells/dem/Dem.java"));
        /*walk_recursively(Paths.get("src/main/java/"))
                .filter(Files::is_file)
                .forEach(SourceCodeCheck::check_Java_source_code);*/
    }

    private static void check_Java_source_code(Path file) {
        try {
            System.out.println("Checking file: " + file);
            final var lexer = new net.splitcells.dem.source.code.antlr.Java_11_lexer
                    (CharStreams.fromFileName(file.toString()));
            final var parser = new net.splitcells.dem.source.code.antlr.Java_11_parser
                    (new CommonTokenStream(lexer));
            // TODO REMOVE this, when this feature is implemented.
            // parser.addErrorListener(new DiagnosticErrorListener());
            //parser.setErrorHandler(new BailErrorStrategy());
            //parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
            parser.addErrorListener(new BaseErrorListener() {
                // Creates more easily understandable error message.
                @Override
                public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
                        throws ParseCancellationException {
                    System.out.println("line " + line + ":" + charPositionInLine + " " + msg);
                    throw new ParseCancellationException("line " + line + ":" + charPositionInLine + " " + msg);
                }
            });
            parser.source_unit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
