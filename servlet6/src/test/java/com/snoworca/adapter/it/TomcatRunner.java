package com.snoworca.adapter.it;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.Servlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.stream.Stream;

/**
 * Tomcat embed runner for servlet6 module integration tests.
 *
 * <p>Boots a Tomcat 10.1.x instance on a random free port with a temporary base directory.
 * Provides servlet/filter registration API and implements {@link AutoCloseable} for
 * deterministic resource cleanup (Tomcat stop + temp dir delete).
 *
 * @req FR-TEST-003
 */
public final class TomcatRunner implements AutoCloseable {

    private final Tomcat tomcat;
    private final Context context;
    private final Path baseDir;
    private final int port;
    private boolean started;

    /**
     * @req FR-TEST-003
     */
    public TomcatRunner() throws IOException {
        this.baseDir = Files.createTempDirectory("tomcat-embed-servlet6-");
        this.port = findFreePort();
        this.tomcat = new Tomcat();
        this.tomcat.setBaseDir(this.baseDir.toString());
        this.tomcat.setPort(this.port);
        Connector connector = this.tomcat.getConnector();
        connector.setPort(this.port);
        Path docBase = this.baseDir.resolve("webapp");
        Files.createDirectories(docBase);
        this.context = this.tomcat.addContext("", docBase.toAbsolutePath().toString());
    }

    /**
     * @req FR-TEST-003
     */
    public TomcatRunner addServlet(String name, String urlPattern, Servlet servlet) {
        Tomcat.addServlet(this.context, name, servlet);
        this.context.addServletMappingDecoded(urlPattern, name);
        return this;
    }

    /**
     * @req FR-TEST-003
     */
    public TomcatRunner addFilter(String name, String urlPattern, Filter filter) {
        FilterDef def = new FilterDef();
        def.setFilterName(name);
        def.setFilter(filter);
        this.context.addFilterDef(def);
        FilterMap map = new FilterMap();
        map.setFilterName(name);
        map.addURLPattern(urlPattern);
        for (DispatcherType dt : EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE)) {
            map.setDispatcher(dt.name());
        }
        this.context.addFilterMap(map);
        return this;
    }

    /**
     * @req FR-TEST-003
     */
    public TomcatRunner start() throws LifecycleException {
        this.tomcat.start();
        this.started = true;
        return this;
    }

    /**
     * @req FR-TEST-003
     */
    public int port() {
        return this.port;
    }

    /**
     * @req FR-TEST-003
     */
    public String baseUrl() {
        return "http://localhost:" + this.port;
    }

    @Override
    public void close() {
        try {
            if (this.started) {
                this.tomcat.stop();
                this.tomcat.destroy();
            }
        } catch (LifecycleException ignored) {
            // best-effort shutdown
        } finally {
            deleteRecursively(this.baseDir);
        }
    }

    private static int findFreePort() throws IOException {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        }
    }

    private static void deleteRecursively(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // best-effort
                }
            });
        } catch (IOException ignored) {
            // best-effort
        }
    }
}
