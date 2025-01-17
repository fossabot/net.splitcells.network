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
package net.splitcells.website.server.renderer.extension.commonmark;

import net.splitcells.dem.lang.perspective.Perspective;
import net.splitcells.website.server.renderer.ProjectRenderer;
import net.splitcells.website.server.renderer.extension.ProjectRendererExtension;
import net.splitcells.website.server.renderer.RenderingResult;

import java.nio.file.Path;
import java.util.Optional;

import static io.vertx.core.http.HttpHeaders.TEXT_HTML;
import static net.splitcells.dem.resource.Paths.readString;
import static net.splitcells.dem.resource.host.Files.is_file;
import static net.splitcells.website.server.renderer.RenderingResult.renderingResult;
import static net.splitcells.website.server.renderer.extension.commonmark.CommonMarkRenderer.commonMarkRenderer;

/**
 * TODO Add support for header outline.
 */
public class CommonMarkReadmeExtension implements ProjectRendererExtension {

    public static CommonMarkReadmeExtension commonMarkReadmeExtension() {
        return new CommonMarkReadmeExtension();
    }

    private final CommonMarkRenderer renderer = commonMarkRenderer();

    private CommonMarkReadmeExtension() {
    }

    @Override
    public Optional<RenderingResult> renderFile(String path, ProjectRenderer projectRenderer) {
        if (path.endsWith("README.html") && is_file(projectRenderer.projectFolder().resolve("README.md"))) {
            final var pathContent = readString(projectRenderer.projectFolder().resolve("README.md"));
            return Optional.of(
                    renderingResult(renderer.render(pathContent, projectRenderer, path)
                            , TEXT_HTML.toString()));
        }
        return Optional.empty();
    }

    @Override
    public Perspective extendProjectLayout(Perspective layout, ProjectRenderer projectRenderer) {
        if (is_file(projectRenderer.projectFolder().resolve("README.md"))) {
            ProjectRenderer.extendPerspectiveWithPath(layout
                    , Path.of(projectRenderer.resourceRootPath().substring(1)).resolve("README.html"));
        }
        return layout;
    }
}
