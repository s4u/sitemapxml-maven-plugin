/*
 * Copyright 2013 Slawomir Jaranowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.s4u.plugins.sitemapxml;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generate sitemap.xml for project site.
 *
 * @author Slawomir Jaranowski.
 */
@Mojo(name = "gen", defaultPhase = LifecyclePhase.SITE, requiresProject = true, threadSafe = true)
public class SiteMapXmlMojo extends AbstractMojo {


    /**
     * Directory where the project sites and report distributions was generated.
     */
    @Parameter(property = "siteOutputDirectory", defaultValue = "${project.reporting.outputDirectory}", required = true)
    private File siteOutputDirectory;

    /**
     * URL prefix which will be used to build url in sitemap.xml
     */
    @Parameter(property = "sitemapxml.siteurl", defaultValue = "${project.url}", required = true)
    private String siteUrl;

    /**
     * Files mask which be used to include its to sitemap.xml<br/>
     *
     * Default is <code>*.html</code>
     */
    @Parameter
    private String[] includes;

    /**
     * Maximum depth for looking for items for sitemap.xml
     */
    @Parameter(property = "sitemapxml.maxdept", defaultValue = "1")
    private int maxDepth;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        checkParameters();

        getLog().info("Generate sitemap.xml - Start");

        if (getLog().isDebugEnabled()) {
            getLog().debug("Site local directory: " + siteOutputDirectory);
            getLog().debug("Site url: " + siteUrl);
            getLog().debug("Includes: " + Arrays.toString(includes));
        }

        List<String> urls = new ArrayList<>();
        listDirectory(1, siteOutputDirectory, urls);

        try {
            generateXML(urls);
        } catch (ParserConfigurationException | TransformerException e) {
            getLog().error("", e);
            throw new MojoFailureException("Generate sitemap.xml error: " + e.getMessage(), e);
        }
        getLog().info("Generate sitemap.xml - OK");
    }

    /**
     * Check parameters, set default value.
     */
    private void checkParameters() {

        if (includes == null || includes.length == 0) {
            includes = new String[]{".*\\.html"};
        }

        if (siteUrl.endsWith("/")) {
            siteUrl.replaceFirst("/+$", "");
        }
    }

    /**
     * Generate output sitemap.xml
     *
     * @param urls urls to include
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    private void generateXML(List<String> urls) throws ParserConfigurationException, TransformerException {

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element urlset = document.createElement("urlset");
        urlset.setAttribute("xmlns", "http://www.sitemaps.org/schemas/sitemap/0.9");
        document.appendChild(urlset);

        for (String file : urls) {
            Element url = document.createElement("url");
            Element loc = document.createElement("loc");
            loc.appendChild(document.createTextNode(file));
            url.appendChild(loc);
            urlset.appendChild(url);
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(siteOutputDirectory, "sitemap.xml"));

        transformer.transform(source, streamResult);
    }

    /**
     * Implementation of FilenameFilter.
     */
    class OurFilenameFilter implements FilenameFilter {

        @Override
        public boolean accept(File dir, String name) {

            File f = new File(dir, name);

            if (f.isDirectory()) {
                return true;
            }

            for (String s : includes) {
                if (name.matches(s)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Prepare file list for xml.
     *
     * @param depth               max directory depth
     * @param siteOutputDirectory current directory
     * @param urls               output list
     */
    private void listDirectory(int depth, File siteOutputDirectory, List<String> urls) {

        List<File> nextDirs = new ArrayList<>();

        for (File file : siteOutputDirectory.listFiles(new OurFilenameFilter())) {

            if (file.isDirectory()) {
                nextDirs.add(file);
            } else {
                getLog().debug("add file to list: " + file);
                appendFile(urls, file);
            }
        }

        if (depth < maxDepth) {
            for (File dir : nextDirs) {
                listDirectory(depth + 1, dir, urls);
            }
        }
    }

    /**
     * Append one file to list, transform its name to url.
     *
     * @param urls output list
     * @param file file to add to list
     */
    private void appendFile(List<String> urls, File file) {

        String fileAbs = file.getAbsolutePath();
        String fileToAdd = fileAbs.replace(siteOutputDirectory.getAbsolutePath(), "");
        fileToAdd = fileToAdd.replaceAll("\\\\", "/");
        urls.add(siteUrl + fileToAdd);
    }
}