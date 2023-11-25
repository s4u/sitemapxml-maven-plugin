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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SiteMapXmlMojoTest {

    private static final URL RESOURCE_ROOT = SiteMapXmlMojo.class.getResource("/");

    @Rule
    public MojoRule rule = new MojoRule();

    @Before
    public void setup() throws IOException, URISyntaxException {
        Files.deleteIfExists(Paths.get(RESOURCE_ROOT.toURI()).resolve("test-site").resolve("sitemap.xml"));
    }

    @Test
    public void pluginShouldGenerateCorrectSitemap() throws Exception {

        ProjectMock project = new ProjectMock("/test-site");

        SiteMapXmlMojo mojo = rule.lookupConfiguredMojo(project, "gen");
        mojo.execute();

        assertThat(new File(project.getReporting().getOutputDirectory(), "sitemap.xml"))
                .hasSameTextualContentAs(new File(RESOURCE_ROOT.getFile(), "sitemap-depth-1.xml"), StandardCharsets.UTF_8);
    }

    @Test
    public void pluginShouldGenerateCorrectSitemapWithDepth2() throws Exception {

        ProjectMock project = new ProjectMock("/test-site");

        SiteMapXmlMojo mojo = rule.lookupConfiguredMojo(project, "gen");
        mojo.setMaxDepth(2);
        mojo.execute();

        assertThat(new File(project.getReporting().getOutputDirectory(), "sitemap.xml"))
                .hasSameTextualContentAs(new File(RESOURCE_ROOT.getFile(), "sitemap-depth-2.xml"), StandardCharsets.UTF_8);
    }

    @Test
    public void pluginShouldGenerateCorrectSitemapWithNotExistingIndexPages() throws Exception {

        ProjectMock project = new ProjectMock("/test-site");

        SiteMapXmlMojo mojo = rule.lookupConfiguredMojo(project, "gen");
        mojo.setMaxDepth(2);
        mojo.setIndexPages(Collections.singletonList("foo.html"));
        mojo.execute();

        assertThat(new File(project.getReporting().getOutputDirectory(), "sitemap.xml"))
                .hasSameTextualContentAs(new File(RESOURCE_ROOT.getFile(), "sitemap-index-file-foo.xml"), StandardCharsets.UTF_8);
    }

    @Test
    public void pluginShouldGenerateCorrectSitemapWithExistingIndexPages() throws Exception {

        ProjectMock project = new ProjectMock("/test-site");

        SiteMapXmlMojo mojo = rule.lookupConfiguredMojo(project, "gen");
        mojo.setMaxDepth(2);
        mojo.setIndexPages(Collections.singletonList("index2.html"));
        mojo.execute();

        assertThat(new File(project.getReporting().getOutputDirectory(), "sitemap.xml"))
                .hasSameTextualContentAs(new File(RESOURCE_ROOT.getFile(), "sitemap-index-file-index2.xml"), StandardCharsets.UTF_8);
    }

    @Test
    public void pluginShouldReturnInfoAboutMissSite() throws Exception {

        ProjectMock project = new ProjectMock("/no-exist-test-site");

        Mojo gen = rule.lookupConfiguredMojo(project, "gen");

        assertThatThrownBy(gen::execute)
                .isExactlyInstanceOf(MojoFailureException.class)
                .hasMessageMatching("site directory (/|[A-Z]:\\\\)no-exist-test-site not exist - please run with site phase");
    }

    @Test
    public void pluginShouldSkipSiteMapGeneration() throws Exception {

        ProjectMock project = new ProjectMock("/test-site");

        Mojo gen = rule.lookupConfiguredMojo(project, "gen");
        rule.setVariableValueToObject(gen, "skip", true);

        gen.execute();

        assertThat(new File(project.getReporting().getOutputDirectory(), "sitemap.xml")).doesNotExist();
    }
}
