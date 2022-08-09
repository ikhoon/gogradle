/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.github.blindpirate.gogradle.util.logging;

import com.github.blindpirate.gogradle.GogradleGlobal;

import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;
import org.gradle.internal.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

import static com.github.blindpirate.gogradle.util.IOUtils.byteCountToDisplaySize;


public class ProgressMonitorInputStream extends InputStream {
    private static final Logger slf4jLogger =
            LoggerFactory.getLogger(ProgressMonitorInputStream.class.getName());

    private static final int BUF_SIZE = 4096;
    private InputStream delegate;
    private ProgressLogger progressLogger;
    private int totalBytes;
    private int bufferedBytes;
    private boolean completed;
    private String description;

    public ProgressMonitorInputStream(String url, InputStream delegate) {
        this.delegate = delegate;
        final ServiceRegistry serviceRegistry = GogradleGlobal.getInstance(ServiceRegistry.class);
        if (serviceRegistry != null) {
            ProgressLoggerFactory progressLoggerFactory = serviceRegistry.get(ProgressLoggerFactory.class);
            progressLogger = progressLoggerFactory.newOperation(this.getClass());

            description = "Start downloading from " + url;
            logStart();
        }
    }

    @Override
    public int read() throws IOException {
        int ret = delegate.read();
        if (completed) {
            return ret;
        }
        if (ret == -1) {
            progress();
            progressLogger.completed();
            completed = true;
        } else {
            bufferedBytes += 1;
            if (bufferedBytes >= BUF_SIZE) {
                progress();
            }
        }
        return ret;
    }

    private void progress() {
        totalBytes += bufferedBytes;
        bufferedBytes = 0;
        logProgress(byteCountToDisplaySize(totalBytes) + " downloaded");
    }

    private void logStart() {
        if (progressLogger != null) {
            progressLogger.start(description, description);
        } else {
            slf4jLogger.info(description);
        }
    }

    private void logProgress(String message) {
        if (progressLogger != null) {
            progressLogger.progress(message);
        } else {
            slf4jLogger.debug(description + " ... " + message);
        }
    }

}
