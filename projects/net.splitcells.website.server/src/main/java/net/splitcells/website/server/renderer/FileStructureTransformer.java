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
package net.splitcells.website.server.renderer;

import net.splitcells.dem.data.set.map.Map;
import net.splitcells.dem.lang.namespace.NameSpaces;
import net.splitcells.dem.lang.perspective.PerspectiveI;
import net.splitcells.dem.resource.Paths;
import net.splitcells.dem.resource.host.interaction.LogLevel;
import net.splitcells.dem.utils.CommonFunctions;
import net.splitcells.website.Validator;
import net.splitcells.website.ValidatorViaSchema;
import net.splitcells.website.server.translation.to.html.PathBasedUriResolver;
import net.splitcells.website.server.translation.to.html.XslTransformer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.nio.file.Files.newInputStream;
import static net.splitcells.dem.lang.namespace.NameSpaces.STRING;
import static net.splitcells.dem.lang.perspective.PerspectiveI.perspective;
import static net.splitcells.dem.resource.Paths.generateFolderPath;
import static net.splitcells.dem.resource.host.interaction.Domsole.domsole;
import static net.splitcells.dem.utils.CommonFunctions.appendToFile;

public class FileStructureTransformer {

    private final Path fileStructureRoot;
    private final XslTransformer transformer;
    private final Path loggingProject = Paths.path(System.getProperty("user.home")
            + "/connections/tmp.storage/net.splitcells.dem");
    private final Validator validator;

    /* TODO REMOVE by 2022
    @Deprecated
    public static void main(String... args) throws IOException {
        final var articles = java.nio.file.Paths.get(System.getProperty("user.home")
                + "/Documents/projects/net.splitcells.martins.avots.support.system/private/net.splitcells.martins.avots.website/src/main/xml/net/splitcells/martins/avots/website/articles");
        Files.list(articles).forEach(a -> {
            final var folder = articles.resolve(a.getFileName().toString().split("-")[0])
                    .resolve(a.getFileName().toString().split("-")[1])
                    .resolve(a.getFileName().toString().split("-")[2]);
            generateFolderPath(folder);
            Paths.copyFileFrom(a, folder.resolve(a.getFileName().toString().substring(11)));
        });
    }*/

    public FileStructureTransformer(Path fileStructureRoot, Path xslLibs, String transformerXsl, Validator validator) {
        this.fileStructureRoot = fileStructureRoot;
        try {
            transformer = new XslTransformer
                    (newInputStream(xslLibs.resolve(transformerXsl)), new PathBasedUriResolver(xslLibs));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        this.validator = validator;
    }

    public String transform(List<String> path) {
        return transform(Paths.path(fileStructureRoot, path));
    }

    public String transform(Path file) {
        validator.validate(file).ifPresent(error -> {
            final var loggingFolder = loggingProject.resolve("src/main/txt")
                    .resolve(fileStructureRoot.relativize(file).getParent());
            generateFolderPath(loggingFolder);
            appendToFile(loggingFolder.resolve(file.getFileName() + ".errors.txt"), error);
            domsole().append(perspective(error, STRING), LogLevel.ERROR);
        });
        return new String(transformer.transform(file));
    }

    public String transform(String content) {
        return transformer.transform(content);
    }
}
