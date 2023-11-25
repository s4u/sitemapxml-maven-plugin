/*
 * Copyright 2019 Slawomir Jaranowski
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

package org.simplify4u.plugins.sitemapxml;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Generate <a href="https://www.sitemaps.org/">sitemap.xml</a> for project site.
 *
 * @author Slawomir Jaranowski.
 */
@Mojo(name = "gen", defaultPhase = LifecyclePhase.SITE, threadSafe = true)
public class SiteMapXmlMojo extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SiteMapXmlMojo.class);

    private static final Pattern PATTERN_END_SLASH = Pattern.compile("/+$");

    private String siteUrl;

    private List<Pattern> patternIncludes;

    /**
     * Directory where the project sites and report distributions was generated.
     *
     * @since 1.0.0
     */
    @Parameter(property = "siteOutputDirectory", defaultValue = "${project.reporting.outputDirectory}",
            required = true)
    private File siteOutputDirectory;

    /**
     * Skip sitemap.xml generation.
     *
     * @since 2.1.0
     */
    @Parameter(property = "maven.site.skip", defaultValue = "false")
    private boolean skip;

    /**
     * Maximum depth for looking for items for sitemap.xml
     *
     * @since 1.0.0
     */
    @Parameter(property = "sitemapxml.maxdept", defaultValue = "1")
    private int maxDepth;

    /**
     * List of index page names. Those names will be removed from the end of generated urls.
     * <p>
     * So for default configuration we will have: {@code https://www.example.com/} instead of
     * {@code https://www.example.com/index.html}
     *
     * @since 2.2.0
     */
    @Parameter(defaultValue = "index.html", required = true)
    private List<String> indexPages;

    /**
     * URL prefix which will be used to build url in sitemap.xml
     *
     * @param siteUrl a site url prefix
     *
     * @since 1.0.0
     */
    @Parameter(property = "sitemapxml.siteurl", defaultValue = "${project.url}", required = true)
    public void setSiteUrl(String siteUrl) {
        if (siteUrl.endsWith("/")) {
            siteUrl = PATTERN_END_SLASH.matcher(siteUrl).replaceFirst("");
        }

        this.siteUrl = siteUrl;
    }

    /**
     * Files mask which be used to include its to sitemap.xml
     *
     * @param includes a file mask for include
     *
     * @since 1.0.0
     */
    @Parameter(defaultValue = "*.html", required = true)
    public void setIncludes(List<String> includes) {
        this.patternIncludes = includes.stream()
                .map(s -> s.replace(".", "\\."))
                .map(s -> s.replace("*", ".*"))
                .map(Pattern::compile)
                .collect(Collectors.toList());
    }

    // setters used in test

    /**
     * Set maxDepth param.
     *
     * @param maxDepth a max depth
     */
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    /**
     * Set indexPages param.
     *
     * @param indexPages a list
     */
    public void setIndexPages(List<String> indexPages) {
        this.indexPages = indexPages;
    }


    @Override
    public void execute() throws MojoFailureException {

        if (skip) {
            LOGGER.info("Skipping sitemap.xml generation");
            return;
        }

        LOGGER.info("Generate sitemap.xml - Start");

        LOGGER.debug("Site local directory: {}", siteOutputDirectory);
        LOGGER.debug("Site url: {}", siteUrl);
        LOGGER.debug("Includes: {}", patternIncludes);

        if (!siteOutputDirectory.exists()) {
            throw new MojoFailureException(
                    String.format("site directory %s not exist - please run with site phase", siteOutputDirectory));
        }

        try {
            List<String> urls = new ArrayList<>();
            listDirectory(1, siteOutputDirectory, urls);
            generateXML(urls);
        } catch (ParserConfigurationException | TransformerException e) {
            throw new MojoFailureException("Generate sitemap.xml error: " + e.getMessage(), e);
        }

        LOGGER.info("Generate sitemap.xml - OK");
    }


    /**
     * Generate output sitemap.xml
     *
     * @param urls urls to include
     */
    private void generateXML(List<String> urls) throws ParserConfigurationException, TransformerException {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        documentBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
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
        transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

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

            return patternIncludes.stream().anyMatch(p -> p.matcher(name).find());
        }
    }

    /**
     * Prepare file list for xml.
     *
     * @param depth               max directory depth
     * @param siteOutputDirectory current directory
     */
    private void listDirectory(int depth, File siteOutputDirectory, List<String> urls) {

        List<File> nextDirs = new ArrayList<>();

        File[] listFiles = Optional.ofNullable(siteOutputDirectory)
                .map(dir -> dir.listFiles(new OurFilenameFilter()))
                .orElseGet(() -> new File[]{});

        Arrays.sort(listFiles);

        for (File file : listFiles) {

            if (file.isDirectory()) {
                nextDirs.add(file);
            } else {
                LOGGER.debug("add file to list: {}", file);
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

        File absoluteFile = file.getAbsoluteFile();
        String absolutePath;
        if (indexPages.contains(absoluteFile.getName())) {
            absolutePath = absoluteFile.getParent() + "/";
        } else {
            absolutePath = absoluteFile.getPath();
        }

        String fileToAdd = absolutePath.replace(siteOutputDirectory.getAbsolutePath(), "");
        fileToAdd = fileToAdd.replace("\\", "/");
        urls.add(siteUrl + fileToAdd);
    }
}
