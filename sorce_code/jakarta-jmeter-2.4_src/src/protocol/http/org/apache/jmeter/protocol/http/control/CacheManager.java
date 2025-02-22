/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// For unit tests @see TestCookieManager

package org.apache.jmeter.protocol.http.control;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Handles HTTP Caching
 */
public class CacheManager extends ConfigTestElement implements TestListener, Serializable {

    private static final long serialVersionUID = 234L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    //+ JMX attributes, do not change values
    public static final String CLEAR = "clearEachIteration"; // $NON-NLS-1$
    public static final String USE_EXPIRES = "useExpires"; // $NON-NLS-1$
    //-

    private transient ThreadLocal<Map<String, CacheEntry>> threadCache;

    private transient boolean useExpires; // Cached value

    public CacheManager() {
        setProperty(new BooleanProperty(CLEAR, false));
        setProperty(new BooleanProperty(USE_EXPIRES, false));
        clearCache();
        useExpires = false;
    }

    /*
     * Holder for storing cache details.
     * Perhaps add original response later?
     */
    // package-protected to allow access by unit-test cases
    static class CacheEntry{
        private final String lastModified;
        private final String etag;
        private final Date expires;
        public CacheEntry(String lastModified, Date expires, String etag){
           this.lastModified = lastModified;
           this.etag = etag;
           this.expires = expires;
       }
        public String getLastModified() {
            return lastModified;
        }
        public String getEtag() {
            return etag;
        }
        @Override
        public String toString(){
            return lastModified+" "+etag;
        }
        public Date getExpires() {
            return expires;
        }
    }

    /**
     * Save the Last-Modified, Etag, and Expires headers if the result is cacheable.
     *
     * @param conn connection
     * @param res result
     */
    public void saveDetails(URLConnection conn, SampleResult res){
        if (isCacheable(res)){
            String lastModified = conn.getHeaderField(HTTPConstantsInterface.LAST_MODIFIED);
            String expires = conn.getHeaderField(HTTPConstantsInterface.EXPIRES);
            String etag = conn.getHeaderField(HTTPConstantsInterface.ETAG);
            String url = conn.getURL().toString();
            String cacheControl = conn.getHeaderField(HTTPConstantsInterface.CACHE_CONTROL);
            setCache(lastModified, cacheControl, expires, etag, url);
        }
    }

    /**
     * Save the Last-Modified, Etag, and Expires headers if the result is cacheable.
     *
     * @param method
     * @param res result
     */
    public void saveDetails(HttpMethod method, SampleResult res) throws URIException{
        if (isCacheable(res)){
            String lastModified = getHeader(method ,HTTPConstantsInterface.LAST_MODIFIED);
            String expires = getHeader(method ,HTTPConstantsInterface.EXPIRES);
            String etag = getHeader(method ,HTTPConstantsInterface.ETAG);
            String url = method.getURI().toString();
            String cacheControl = getHeader(method, HTTPConstantsInterface.CACHE_CONTROL);
            setCache(lastModified, cacheControl, expires, etag, url);
        }
    }

    // helper method to save the cache entry
    private void setCache(String lastModified, String cacheControl, String expires, String etag, String url) {
        if (log.isDebugEnabled()){
            log.debug("SET(both) "+url + " " + cacheControl + " " + lastModified + " " + " " + expires + " " + etag);
        }
        Date expiresDate = null; // i.e. not using Expires
        if (useExpires) {// Check that we are processing Expires/CacheControl
            final String MAX_AGE = "max-age=";
            // TODO - check for other CacheControl attributes?
            if (cacheControl != null && cacheControl.contains("public") && cacheControl.contains(MAX_AGE)) {
                long maxAge = Long.parseLong(cacheControl.substring(cacheControl.indexOf(MAX_AGE)+MAX_AGE.length()))*1000;
                expiresDate=new Date(System.currentTimeMillis()+maxAge);
            } else if (expires != null) {
                try {
                    expiresDate = DateUtil.parseDate(expires);
                } catch (DateParseException e) {
                    if (log.isDebugEnabled()){
                        log.debug("Unable to parse Expires: '"+expires+"' "+e);
                    }
                    expiresDate = new Date(0L); // invalid dates must be treated as expired
                }
            }
        }
        getCache().put(url, new CacheEntry(lastModified, expiresDate, etag));
    }

    // Helper method to deal with missing headers
    private String getHeader(HttpMethod method, String name){
        Header hdr = method.getResponseHeader(name);
        return hdr != null ? hdr.getValue() : null;
    }

    /*
     * Is the sample result OK to cache?
     * i.e is it in the 2xx range?
     */
    private boolean isCacheable(SampleResult res){
        final String responseCode = res.getResponseCode();
        return "200".compareTo(responseCode) <= 0  // $NON-NLS-1$
            && "299".compareTo(responseCode) >= 0; // $NON-NLS-1$
    }

    /**
     * Check the cache, and if there is a match, set the headers:<br/>
     * If-Modified-Since<br/>
     * If-None-Match<br/>
     * @param url URL to look up in cache
     * @param method where to set the headers
     */
    public void setHeaders(URL url, HttpMethod method) {
        CacheEntry entry = getCache().get(url.toString());
        if (log.isDebugEnabled()){
            log.debug(method.getName()+"(OAHC) "+url.toString()+" "+entry);
        }
        if (entry != null){
            final String lastModified = entry.getLastModified();
            if (lastModified != null){
                method.setRequestHeader(HTTPConstantsInterface.IF_MODIFIED_SINCE, lastModified);
            }
            final String etag = entry.getEtag();
            if (etag != null){
                method.setRequestHeader(HTTPConstantsInterface.IF_NONE_MATCH, etag);
            }
        }
    }

    /**
     * Check the cache, and if there is a match, set the headers:<br/>
     * If-Modified-Since<br/>
     * If-None-Match<br/>
     * @param url URL to look up in cache
     * @param conn where to set the headers
     */
    public void setHeaders(HttpURLConnection conn, URL url) {
        CacheEntry entry = getCache().get(url.toString());
        if (log.isDebugEnabled()){
            log.debug(conn.getRequestMethod()+"(Java) "+url.toString()+" "+entry);
        }
        if (entry != null){
            final String lastModified = entry.getLastModified();
            if (lastModified != null){
                conn.addRequestProperty(HTTPConstantsInterface.IF_MODIFIED_SINCE, lastModified);
            }
            final String etag = entry.getEtag();
            if (etag != null){
                conn.addRequestProperty(HTTPConstantsInterface.IF_NONE_MATCH, etag);
            }
        }
    }

    /**
     * Check the cache, if the entry has an expires header and the entry has not expired, return true<br/>
     * @param url URL to look up in cache
     */
    public boolean inCache(URL url) {
        CacheEntry entry = getCache().get(url.toString());
        if (log.isDebugEnabled()){
            log.debug("inCache "+url.toString()+" "+entry);
        }
        if (entry != null){
            final Date expiresDate = entry.getExpires();
            if (expiresDate != null) {
                if (expiresDate.after(new Date())) {
                    if (log.isDebugEnabled()){
                        log.debug("Expires= " + expiresDate + " (Valid)");
                    }
                    return true;
                } else {
                    if (log.isDebugEnabled()){
                        log.debug("Expires= " + expiresDate + " (Expired)");
                    }
                }
            }
        }
        return false;
    }

    private Map<String, CacheEntry> getCache(){
        return threadCache.get();
    }

    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }

    public boolean getUseExpires() {
        return getPropertyAsBoolean(USE_EXPIRES);
    }

    public void setUseExpires(boolean expires) {
        setProperty(new BooleanProperty(USE_EXPIRES, expires));
    }

    @Override
    public void clear(){
        super.clear();
        clearCache();
    }

    private void clearCache() {
        log.debug("Clear cache");
        threadCache = new ThreadLocal<Map<String, CacheEntry>>(){
            @Override
            protected Map<String, CacheEntry> initialValue(){
                return new HashMap<String, CacheEntry>();
            }
        };
    }

    public void testStarted() {
    }

    public void testEnded() {
    }

    public void testStarted(String host) {
    }

    public void testEnded(String host) {
    }

    public void testIterationStart(LoopIterationEvent event) {
        if (getClearEachIteration()) {
            clearCache();
        }
        useExpires=getUseExpires(); // cache the value
    }
}