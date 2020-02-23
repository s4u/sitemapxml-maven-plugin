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

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class SiteMapXmlMojoTest {


    private static final String EXPECTED_SITE_MAP = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
            "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n" +
            "    <url>\n" +
            "        <loc>http://example.com/index.html</loc>\n" +
            "    </url>\n" +
            "    <url>\n" +
            "        <loc>http://example.com/index2.html</loc>\n" +
            "    </url>\n" +
            "</urlset>\n";

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void pluginShouldGenerateCorrectSitemap() throws Exception {

        ProjectMock project = new ProjectMock();

        Mojo gen = rule.lookupConfiguredMojo(project, "gen");
        gen.execute();

        String generatedSiteMap = new String(Files.readAllBytes(
                new File( project.getReporting().getOutputDirectory(), "sitemap.xml").toPath()),
                StandardCharsets.UTF_8);


        assertEquals("Generated map has not expected content",
                EXPECTED_SITE_MAP, generatedSiteMap);
    }
}
