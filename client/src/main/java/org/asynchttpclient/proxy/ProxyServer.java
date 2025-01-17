/*
 * Copyright 2010 Ning, Inc.
 *
 * This program is licensed to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package org.asynchttpclient.proxy;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.Realm;
import org.asynchttpclient.Request;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.asynchttpclient.util.Assertions.assertNotNull;
import static org.asynchttpclient.util.MiscUtils.isNonEmpty;

/**
 * Represents a proxy server.
 */
public class ProxyServer {

    private final String host;
    private final int port;
    private final int securedPort;
    private final @Nullable Realm realm;
    private final List<String> nonProxyHosts;
    private final ProxyType proxyType;
    private final @Nullable Function<Request, HttpHeaders> customHeaders;

    public ProxyServer(String host, int port, int securedPort, @Nullable Realm realm, List<String> nonProxyHosts, ProxyType proxyType,
                       @Nullable Function<Request, HttpHeaders> customHeaders) {
        this.host = host;
        this.port = port;
        this.securedPort = securedPort;
        this.realm = realm;
        this.nonProxyHosts = nonProxyHosts;
        this.proxyType = proxyType;
        this.customHeaders = customHeaders;
    }

    public ProxyServer(String host, int port, int securedPort, Realm realm, List<String> nonProxyHosts, ProxyType proxyType) {
        this(host, port, securedPort, realm, nonProxyHosts, proxyType, null);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public int getSecuredPort() {
        return securedPort;
    }

    public List<String> getNonProxyHosts() {
        return nonProxyHosts;
    }

    public @Nullable Realm getRealm() {
        return realm;
    }

    public ProxyType getProxyType() {
        return proxyType;
    }

    public @Nullable Function<Request, HttpHeaders> getCustomHeaders() {
        return customHeaders;
    }

    /**
     * Checks whether proxy should be used according to nonProxyHosts settings of
     * it, or we want to go directly to target host. If {@code null} proxy is
     * passed in, this method returns true -- since there is NO proxy, we should
     * avoid to use it. Simple hostname pattern matching using "*" are supported,
     * but only as prefixes.
     *
     * @param hostname the hostname
     * @return true if we have to ignore proxy use (obeying non-proxy hosts
     * settings), false otherwise.
     * @see <a href=
     * "https://docs.oracle.com/javase/8/docs/api/java/net/doc-files/net-properties.html">Networking
     * Properties</a>
     */
    public boolean isIgnoredForHost(String hostname) {
        assertNotNull(hostname, "hostname");
        if (isNonEmpty(nonProxyHosts)) {
            for (String nonProxyHost : nonProxyHosts) {
                if (matchNonProxyHost(hostname, nonProxyHost)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean matchNonProxyHost(String targetHost, String nonProxyHost) {

        if (nonProxyHost.length() > 1) {
            if (nonProxyHost.charAt(0) == '*') {
                return targetHost.regionMatches(true, targetHost.length() - nonProxyHost.length() + 1, nonProxyHost, 1,
                        nonProxyHost.length() - 1);
            } else if (nonProxyHost.charAt(nonProxyHost.length() - 1) == '*') {
                return targetHost.regionMatches(true, 0, nonProxyHost, 0, nonProxyHost.length() - 1);
            }
        }

        return nonProxyHost.equalsIgnoreCase(targetHost);
    }

    public static class Builder {

        private final String host;
        private final int port;
        private int securedPort;
        private @Nullable Realm realm;
        private @Nullable List<String> nonProxyHosts;
        private @Nullable ProxyType proxyType;
        private @Nullable Function<Request, HttpHeaders> customHeaders;

        public Builder(String host, int port) {
            this.host = host;
            this.port = port;
            securedPort = port;
        }

        public Builder setSecuredPort(int securedPort) {
            this.securedPort = securedPort;
            return this;
        }

        public Builder setRealm(@Nullable Realm realm) {
            this.realm = realm;
            return this;
        }

        public Builder setRealm(Realm.Builder realm) {
            this.realm = realm.build();
            return this;
        }

        public Builder setNonProxyHost(String nonProxyHost) {
            if (nonProxyHosts == null) {
                nonProxyHosts = new ArrayList<>(1);
            }
            nonProxyHosts.add(nonProxyHost);
            return this;
        }

        public Builder setNonProxyHosts(List<String> nonProxyHosts) {
            this.nonProxyHosts = nonProxyHosts;
            return this;
        }

        public Builder setProxyType(ProxyType proxyType) {
            this.proxyType = proxyType;
            return this;
        }

        public Builder setCustomHeaders(Function<Request, HttpHeaders> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        public ProxyServer build() {
            List<String> nonProxyHosts = this.nonProxyHosts != null ? Collections.unmodifiableList(this.nonProxyHosts) : Collections.emptyList();
            ProxyType proxyType = this.proxyType != null ? this.proxyType : ProxyType.HTTP;
            return new ProxyServer(host, port, securedPort, realm, nonProxyHosts, proxyType, customHeaders);
        }
    }
}
