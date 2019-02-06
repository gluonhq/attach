/*
 * Copyright (c) 2018, 2019 Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
package com.gluonhq.attach.util.impl;

import javafx.application.Platform;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NestedEventLoopInvoker {

    private static final Logger LOG = Logger.getLogger(NestedEventLoopInvoker.class.getName());

    public static void enterNestedEventLoop(Object key) {
        if (enterNestedEventLoopMethod != null) {
            try {
                enterNestedEventLoopMethod.invoke(invokeObj, key);
            } catch (IllegalAccessException e) {
                LOG.log(Level.SEVERE, "Could not enter nested event loop.", e);
            } catch (InvocationTargetException e) {
                LOG.log(Level.SEVERE, "Could not enter nested event loop.", e);
            }
        }
    }

    public static void exitNestedEventLoop(Object key, Object rval) {
        if (exitNestedEventLoopMethod != null) {
            try {
                exitNestedEventLoopMethod.invoke(invokeObj, key, rval);
            } catch (IllegalAccessException e) {
                LOG.log(Level.SEVERE, "Could not exit nested event loop.", e);
            } catch (InvocationTargetException e) {
                LOG.log(Level.SEVERE, "Could not exit nested event loop.", e);
            }
        }
    }

    private static Method enterNestedEventLoopMethod = null;
    private static Method exitNestedEventLoopMethod = null;
    private static Object invokeObj;
    static {
        try {
            enterNestedEventLoopMethod = Platform.class.getMethod("enterNestedEventLoop", Object.class);
            exitNestedEventLoopMethod = Platform.class.getMethod("exitNestedEventLoop", Object.class, Object.class);
        } catch (NoSuchMethodException e) {
            log("Could not find method enter/exitNestedEventLoop on javafx.application.Platform", e);
        } catch (SecurityException e) {
            log("Could not find method enter/exitNestedEventLoop on javafx.application.Platform", e);
        }

        if (enterNestedEventLoopMethod == null || exitNestedEventLoopMethod == null) {
            Class clazz = null;
            try {
                clazz = Class.forName("com.sun.javafx.tk.Toolkit");
            } catch (ClassNotFoundException e) {
                log("Could not find class com.sun.javafx.tk.Toolkit", e);
            }

            if (clazz != null) {
                try {
                    enterNestedEventLoopMethod = clazz.getMethod("enterNestedEventLoop", Object.class);
                    exitNestedEventLoopMethod = clazz.getMethod("exitNestedEventLoop", Object.class, Object.class);
                } catch (NoSuchMethodException e) {
                    log("Could not find method enter/exitNestedEventLoop on com.sun.javafx.tk.Toolkit", e);
                } catch (SecurityException e) {
                    log("Could not find method enter/exitNestedEventLoop on com.sun.javafx.tk.Toolkit", e);
                }

                if (enterNestedEventLoopMethod != null && exitNestedEventLoopMethod != null) {
                    try {
                        Method getToolkitMethod = clazz.getMethod("getToolkit");
                        invokeObj = getToolkitMethod.invoke(null);
                    } catch (NoSuchMethodException e) {
                        log("Could not get toolkit from com.sun.javafx.fx.Toolkit", e);
                    } catch (IllegalAccessException e) {
                        log("Could not get toolkit from com.sun.javafx.fx.Toolkit", e);
                    } catch (InvocationTargetException e) {
                        log("Could not get toolkit from com.sun.javafx.fx.Toolkit", e);
                    }
                }
            }
        }

        if (enterNestedEventLoopMethod == null) {
            LOG.log(Level.SEVERE, "Failed to detect valid enterNestedEventLoop method. Set log level for this logger to FINE for more details.");
        }
        if (exitNestedEventLoopMethod == null) {
            LOG.log(Level.SEVERE, "Failed to detect valid exitNestedEventLoop method. Set log level for this logger to FINE for more details.");
        }
    }

    private static void log(String message, Throwable cause) {
        LOG.log(Level.FINE, message);
        if (LOG.isLoggable(Level.FINE)) {
            cause.printStackTrace();
        }
    }
}
