/*
 * Copyright (c) 2016, 2018 Gluon
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of Gluon, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.attach.util;

import java.util.stream.Stream;

public class Logger {

    private final String className;
    private volatile int levelValue;

    private static Logger instance;

    public static Logger getLogger(String className) {
        if (instance == null || ! className.equals(instance.className)) {
            instance = new Logger(className);
        }
        return instance;
    }

    private Logger(String className) {
        this.className = className;
        levelValue = Level.INFO.intValue();
    }

    public boolean isLoggable(Level level) {
        if (level.intValue() < levelValue) {
            return false;
        }
        return true;
    }

    public void log(Level level, String s) {
        if (!isLoggable(level)) {
            return;
        }
        System.err.println(className + " :: " + s);
    }

    public void log(Level level, String s, Object... params) {
        if (!isLoggable(level)) {
            return;
        }
        StringBuilder sb = new StringBuilder(s);
        Stream.of(params).forEach(p -> sb.append(", " + p));
        System.err.println(className + " :: " + sb.toString());

    }

    public void log(Level level, String s, Throwable ex) {
        if (!isLoggable(level)) {
            return;
        }
        System.err.println(className + " :: " + s);
        ex.printStackTrace();
    }

    public void severe(String s) {
        log(Level.SEVERE, s);
    }

    public void warning(String s) {
        log(Level.WARNING, s);
    }

    public void fine(String s) {
        log(Level.FINE, s);
    }

    public void info(String s) {
        log(Level.INFO, s);
    }

}
