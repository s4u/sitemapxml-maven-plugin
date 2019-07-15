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

import org.apache.maven.model.Plugin;
import org.apache.maven.model.Reporting;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

public class ProjectMock extends MavenProjectStub {

    private Reporting reporting;

    public ProjectMock() {
        reporting = new Reporting();
        reporting.setOutputDirectory(getClass().getResource("/test-site").getFile());
    }

    @Override
    public Plugin getPlugin(String pluginKey) {
        return null;
    }

    @Override
    public Reporting getReporting() {
        return reporting;
    }

    @Override
    public String getUrl() {
        return "http://example.com/";
    }
}
