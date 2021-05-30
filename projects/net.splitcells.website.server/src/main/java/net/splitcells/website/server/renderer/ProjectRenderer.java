package net.splitcells.website.server.renderer;

import net.splitcells.dem.data.set.Set;
import net.splitcells.dem.data.set.Sets;
import net.splitcells.dem.data.set.list.Lists;
import net.splitcells.dem.lang.Xml;
import net.splitcells.dem.lang.namespace.NameSpaces;
import net.splitcells.dem.lang.perspective.Perspective;
import net.splitcells.dem.resource.Paths;
import net.splitcells.dem.resource.host.Files;
import net.splitcells.website.Validator;
import net.splitcells.website.ValidatorViaSchema;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;

import static io.vertx.core.http.HttpHeaders.TEXT_HTML;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.stream.Stream.concat;
import static net.splitcells.dem.data.set.Sets.toSetOfUniques;
import static net.splitcells.dem.data.set.list.Lists.list;
import static net.splitcells.dem.data.set.list.Lists.toList;
import static net.splitcells.dem.lang.namespace.NameSpaces.STRING;
import static net.splitcells.dem.lang.perspective.PerspectiveI.perspective;
import static net.splitcells.dem.resource.Paths.readString;
import static net.splitcells.dem.resource.host.Files.isDirectory;
import static net.splitcells.dem.resource.host.Files.is_file;
import static net.splitcells.website.server.renderer.RenderingResult.renderingResult;

/**
 * TODO Use resource folder for xml, txt and etc.
 * <p>
 * TODO Merge website rendering and support system rendering. Make distinction between online and offline rendering.
 */
public class ProjectRenderer {
    private static final String MARKER = "198032jrf013jf09j13f13f4290fj2394fj24";

    public Path project() {
        return project;
    }

    private final Path project;
    private final Path xslLibs;
    private final Path resources;
    private final String resourceRootPath;
    private final boolean flatRepository;
    private final String profile;
    private final boolean typedFolder;
    private final Validator validator;

    public ProjectRenderer(String renderer, Path project, Path xslLibs, Path resources, String resourceRootPath, Validator validator) {
        this(renderer, project, xslLibs, resources, resourceRootPath, true, false, validator);
    }

    public ProjectRenderer(String renderer, Path project, Path xslLibs, Path resources, String resourceRootPath, boolean typedFolder, boolean flatRepository, Validator validator) {
        this.typedFolder = typedFolder;
        this.profile = renderer;
        this.project = project;
        this.xslLibs = xslLibs;
        this.resources = resources;
        this.resourceRootPath = resourceRootPath;
        this.flatRepository = flatRepository;
        this.validator = validator;
    }

    /**
     * A new renderer is created each time, in order to use updated XSL transformations.
     * <p>
     * IDEA Create mode where the renderer ist cached.
     */
    private FileStructureTransformer renderer() {
        return new FileStructureTransformer(project.resolve("xml"), xslLibs, "main." + profile + ".xsl", validator);
    }

    /**
     * TODO Create root for each file type, that needs its one processing method.
     */
    public Optional<RenderingResult> render(String path) {
        try {
            if (path.endsWith("README.md") && Files.is_file(project.resolve("../..").resolve("README.md"))) {
                // TODO HACK
                Parser parser = Parser.builder().build();
                Node document = parser.parse(readString(project.resolve("../..").resolve("README.md")));
                HtmlRenderer renderer = HtmlRenderer.builder().build();
                return renderHtmlBodyContent(renderer.render(document))
                        .map(result -> renderingResult
                                (result
                                        , TEXT_HTML.toString()));
            }
            if (path.length() > 0 && path.charAt(0) == '/') {
                path = path.substring(1);
            }
            // System.out.println(path);
            // TODO Devide rendering function into routing and content type determination.
        /* REMOVE final var path = Lists.<String>list
                (routingContext.request().path().split("/")
                ).stream().filter(e -> !e.isBlank()).collect(toList());*/
            if (path.endsWith(".txt")) {
                return renderTextFile(path).map(r -> renderingResult(r, TEXT_HTML.toString()));
            } else if (path.endsWith(".png")) {
                return readArtifact(path).map(r -> renderingResult(r, "image/png"));
            } else if (path.endsWith(".jpg")) {
                return readArtifact(path).map(r -> renderingResult(r, "image/jpg"));
            } else if (path.endsWith(".css")) {
                return readArtifact(path).map(r -> renderingResult(r, "image/css"));
            } else if (path.endsWith(".js")) {
                return readArtifact(path).map(r -> renderingResult(r, "text/javascript"));
            } else if (path.endsWith(".html")) {
                return renderFile(path).map(r -> renderingResult(r, TEXT_HTML.toString()));
            } else if (path.endsWith(".xml") || path.endsWith(".rss")) {
                return renderFile(path).map(r -> renderingResult(r, TEXT_HTML.toString()));
            } else if (path.endsWith(".svg")) {
                return readArtifact(path).map(r -> renderingResult(r, "image/svg+xml"));
            } else {
                return readArtifact(path).map(r -> renderingResult(r, TEXT_HTML.toString()));
            }
        } catch (Exception e) {
            throw new RuntimeException(resourceRootPath, e);
        }
    }

    private Optional<byte[]> renderFile(String path) {
        // TODO REMOVE Split by dot.
        final var splitByDot = path.split("\\.");
        final var suffix = splitByDot[splitByDot.length - 1];
        try {

            final var sourcePath = path.substring(0, path.length() - suffix.length() - 1) + ".xml";
            final var absolutePath = resolveSourceFolder(sourcePath, "xml");
            // System.out.println("Rendering: " + path);
            // System.out.println("Rendering Relative Resource: " + sourcePath);
            // System.out.println("Rendering Absolute Resource: " + absolutePath);
            // TODO HACK Use optional instead of manual file checking.
            if (java.nio.file.Files.isRegularFile(absolutePath)) {
                // System.out.println("Rendering: " + path);
                return Optional.of(renderer()
                        .transform(absolutePath)
                        .getBytes(Charset.forName("UTF-8")));
            }
            // System.out.println("Reading artifact: " + path);
            return readArtifact(path);
        } catch (Exception e) {
            // System.out.println(path);
            throw new RuntimeException(e);
        }
    }

    private Path resolveSourceFolder(String projectPath, String type) {
        if (flatRepository) {
            projectPath = projectPath.replace(resourceRootPath.substring(1), "");
        }
        if (typedFolder) {
            try {
                return project.resolve(type).resolve(URLDecoder.decode(projectPath, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return project.resolve(URLDecoder.decode(projectPath, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Optional<byte[]> renderHtmlBodyContent(String bodyContent) {
        try {
            final var content = Xml.rElement(NameSpaces.NATURAL, "text");
            content.appendChild
                    (Xml.textNode(MARKER));
            return Optional.of(renderer()
                    .transform(Xml.toPrettyString(content))
                    .replace(MARKER, bodyContent)
                    .getBytes(UTF_8));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<byte[]> renderTextFile(String path) {
        try {
            final var absolutePath = resolveSourceFolder(path, "txt");
            if (java.nio.file.Files.isRegularFile(absolutePath)) {
                final var content = Xml.rElement(NameSpaces.NATURAL, "text");
                content.appendChild
                        (Xml.textNode
                                (new String
                                        (readAllBytes
                                                (absolutePath))));
                return Optional.of(renderer()
                        .transform(Xml.toPrettyString(content))
                        .getBytes(UTF_8));
            }
            return readArtifact(path);
        } catch (Exception e) {
            return readArtifact(path);
        }
    }

    private Optional<byte[]> readArtifact(String path) {
        final var resourcePath = resources.resolve(path);
        if (!is_file(resourcePath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(readAllBytes(resourcePath));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String resourceRootPath() {
        return resourceRootPath;
    }

    public Perspective projectLayout() {
        final var layout = perspective(NameSpaces.VAL, NameSpaces.NATURAL);
        final Path folder;
        if (typedFolder) {
            folder = project.resolve("xml");
        } else {
            folder = project;
        }
        try {
            if (isDirectory(folder)) {
                java.nio.file.Files.walk(folder)
                        .filter(java.nio.file.Files::isRegularFile)
                        .forEach(file -> extendPerspectiveWithPath(layout, folder.relativize(file)));
            }
        } catch (IOException e) {
            throw new RuntimeException(folder.toAbsolutePath().toString(), e);
        }
        return layout;
    }

    public Set<Path> projectPaths() {
        final var projectPaths = Sets.<String>setOfUniques();
        return list(project.resolve("xml"), project.resolve("txt"), resources)
                .stream()
                .filter(folder -> Files.isDirectory(folder))
                .map(folder -> {
                    try {
                        return java.nio.file.Files.walk(folder)
                                .filter(java.nio.file.Files::isRegularFile)
                                .map(file -> folder.relativize(file));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).reduce((a, b) -> concat(a, b))
                .get()
                .collect(toSetOfUniques());
    }

    /**
     * @param current
     * @param relativeProjectPath Path relative to the project folders src/xml folder. This path also represent absolute path in projects namespace.
     */
    private void extendPerspectiveWithPath(Perspective current, Path relativeProjectPath) {
        for (final var element : list(relativeProjectPath.toString().split("/")).stream().filter(e -> !"".contentEquals(e)).collect(toList())) {
            final var children = current.children().stream()
                    .filter(child -> child.nameIs(NameSpaces.VAL, NameSpaces.NATURAL))
                    .filter(child -> child.propertyInstances(NameSpaces.NAME, NameSpaces.NATURAL).stream()
                            .anyMatch(property -> property.value().get().name().equals(element)))
                    .collect(Lists.toList());
            final Perspective child;
            if (children.isEmpty()) {
                child = perspective(NameSpaces.VAL, NameSpaces.NATURAL)
                        .withProperty(NameSpaces.NAME, NameSpaces.NATURAL, element);
                current.withChild(child);
            } else {
                child = children.get(0);
            }
            current = child;
        }
        current.withChild(
                perspective(NameSpaces.LINK, NameSpaces.DEN)
                        .withChild(perspective(NameSpaces.URL, NameSpaces.DEN)
                                .withChild(perspective("/" + relativeProjectPath.toString(), STRING))));
    }
}
