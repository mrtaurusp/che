/*
 * *****************************************************************************
 *  Copyright (c) 2012-2016 Codenvy, S.A.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *    Codenvy, S.A. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.languageserver.server.registry;

import io.typefox.lsapi.InitializeParamsImpl;
import io.typefox.lsapi.InitializeResult;
import io.typefox.lsapi.InitializeResultImpl;
import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.LanguageDescriptionImpl;
import io.typefox.lsapi.ServerCapabilities;
import io.typefox.lsapi.services.LanguageServer;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.server.exception.LanguageServerException;
import org.eclipse.che.plugin.languageserver.server.launcher.LanguageServerLauncher;
import org.eclipse.che.plugin.languageserver.server.messager.PublishDiagnosticsParamsMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class ServerInitializerImpl implements ServerInitializer {
    private final static Logger LOG = LoggerFactory.getLogger(ServerInitializerImpl.class);

    private static final int    PROCESS_ID  = getProcessId();
    private static final String CLIENT_NAME = "EclipseChe";

    private final List<ServerInitializerObserver>   observers;
    private final PublishDiagnosticsParamsMessenger publishDiagnosticsParamsMessenger;

    private final ConcurrentHashMap<String, LanguageServer>           languageIdToServers;
    private final ConcurrentHashMap<LanguageServer, InitializeResult> serversToInitResult;

    @Inject
    public ServerInitializerImpl(PublishDiagnosticsParamsMessenger publishDiagnosticsParamsMessenger) {
        this.observers = new ArrayList<>();
        this.languageIdToServers = new ConcurrentHashMap<>();
        this.serversToInitResult = new ConcurrentHashMap<>();
        this.publishDiagnosticsParamsMessenger = publishDiagnosticsParamsMessenger;
    }

    @Override
    public void addObserver(ServerInitializerObserver observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(ServerInitializerObserver observer) {
        observers.remove(observer);
    }

    @Override
    public LanguageServer initialize(LanguageServerLauncher launcher, String projectPath) throws LanguageServerException {
        String languageId = launcher.getLanguageDescription().getLanguageId();

        synchronized (launcher) {
            LanguageServer server = languageIdToServers.get(languageId);
            if (server != null) {
                InitializeResult initializeResult = serversToInitResult.get(server);
//                if (!initializeResult.getCapabilities().isMultiplyProjectsProvider()) {
                    server = doInitialize(launcher, projectPath);
//                }
            } else {
                server = doInitialize(launcher, projectPath);
                languageIdToServers.put(languageId, server);
            }
            InitializeResult initializeResult = serversToInitResult.get(server);
            if(initializeResult instanceof InitializeResultImpl){
                ((InitializeResultImpl)initializeResult).setSupportedLanguages(Collections.singletonList((LanguageDescriptionImpl)launcher.getLanguageDescription()));
            }
            onServerInitialized(server, serversToInitResult.get(server).getCapabilities(), launcher.getLanguageDescription(), projectPath);
            return server;
        }
    }

    @Override
    public Map<LanguageServer, InitializeResult> getInitializedServers() {
        return Collections.unmodifiableMap(serversToInitResult);
    }

    protected LanguageServer doInitialize(LanguageServerLauncher factory, String projectPath) throws LanguageServerException {
        String languageId = factory.getLanguageDescription().getLanguageId();
        InitializeParamsImpl initializeParams = prepareInitializeParams(projectPath);

        LanguageServer server;
        try {
            server = factory.launch(projectPath);
        } catch (LanguageServerException e) {
            throw new LanguageServerException(
                    "Can't initialize Language Server " + languageId + " on " + projectPath + ". " + e.getMessage(), e);
        }
        registerCallbacks(server);

        CompletableFuture<InitializeResult> completableFuture = server.initialize(initializeParams);
        try {
            InitializeResult initializeResult = completableFuture.get();
            serversToInitResult.put(server, initializeResult);
        } catch (InterruptedException | ExecutionException e) {
            server.shutdown();
            server.exit();

            throw new LanguageServerException("Error fetching server capabilities " + languageId + ". " + e.getMessage(), e);
        }

        LOG.info("Initialized Language Server {} on project {}", languageId, projectPath);
        return server;
    }

    protected void registerCallbacks(LanguageServer server) {
        server.getTextDocumentService().onPublishDiagnostics(publishDiagnosticsParamsMessenger::onEvent);
        server.getWindowService().onLogMessage(messageParams -> LOG.error(messageParams.getType() + " " + messageParams.getMessage()));
    }

    protected InitializeParamsImpl prepareInitializeParams(String projectPath) {
        InitializeParamsImpl initializeParams = new InitializeParamsImpl();
        initializeParams.setProcessId(PROCESS_ID);
        initializeParams.setRootPath(projectPath);
        initializeParams.setClientName(CLIENT_NAME);
        return initializeParams;
    }

    protected void onServerInitialized(LanguageServer server,
                                       ServerCapabilities capabilities,
                                       LanguageDescription languageDescription,
                                       String projectPath) {
        observers.forEach(observer -> observer.onServerInitialized(server, capabilities, languageDescription, projectPath));
    }

    private static int getProcessId() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int prefixEnd = name.indexOf('@');
        if (prefixEnd != -1) {
            String prefix = name.substring(0, prefixEnd);
            try {
                return Integer.parseInt(prefix);
            } catch (NumberFormatException ignored) {
            }
        }

        LOG.error("Failed to recognize the pid of the process");
        return -1;
    }

    @PreDestroy
    protected void shutdown() {
        for (LanguageServer server : serversToInitResult.keySet()) {
            server.shutdown();
            server.exit();
        }
    }

}
