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
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoParameter;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@MojoTest
@MojoParameter(name = "siteOutputDirectory", value = "${project.build.testOutputDirectory}/test-site")
@MojoParameter(name = "siteUrl", value = "http://example.com/")
class SiteMapXmlMojoTest {

    private static final URL RESOURCE_ROOT = SiteMapXmlMojo.class.getResource("/");

    private static final File SITEMAP_FILE = new File(RESOURCE_ROOT.getFile(), "test-site/sitemap.xml");

    @BeforeEach
    void setup() throws IOException, URISyntaxException {
        Files.deleteIfExists(Paths.get(RESOURCE_ROOT.toURI()).resolve("test-site").resolve("sitemap.xml"));
    }

    static Stream<Arguments> sitemapCases() {
        return Stream.of(
                Arguments.of(1, "index.html", "sitemap-depth-1.xml"),
                Arguments.of(2, "index.html", "sitemap-depth-2.xml"),
                Arguments.of(2, "foo.html", "sitemap-index-file-foo.xml"),
                Arguments.of(2, "index2.html", "sitemap-index-file-index2.xml"));
    }

    @ParameterizedTest
    @MethodSource("sitemapCases")
    @InjectMojo(goal = "gen")
    void pluginShouldGenerateCorrectSitemap(int maxDepth, String indexPage, String expectedSitemap, SiteMapXmlMojo mojo)
            throws Exception {

        mojo.setMaxDepth(maxDepth);
        mojo.setIndexPages(Collections.singletonList(indexPage));
        mojo.execute();

        assertThat(SITEMAP_FILE)
                .hasSameTextualContentAs(new File(RESOURCE_ROOT.getFile(), expectedSitemap), StandardCharsets.UTF_8);
    }

    @Test
    @InjectMojo(goal = "gen")
    @MojoParameter(name = "siteOutputDirectory", value = "/no-exist-test-site")
    void pluginShouldReturnInfoAboutMissSite(SiteMapXmlMojo mojo) {

        assertThatThrownBy(mojo::execute)
                .isExactlyInstanceOf(MojoFailureException.class)
                .hasMessageMatching("site directory (/|[A-Z]:\\\\)no-exist-test-site not exist - please run with site phase");
    }

    @Test
    @InjectMojo(goal = "gen")
    @MojoParameter(name = "skip", value = "true")
    void pluginShouldSkipSiteMapGeneration(SiteMapXmlMojo mojo) throws Exception {

        mojo.execute();

        assertThat(SITEMAP_FILE).doesNotExist();
    }
}
