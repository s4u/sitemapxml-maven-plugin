Maven Sitemap generator plugin
==============================

This plugin generate sitemap.xml for project site.

Generated sitemap.xml is consistent with the description:

http://www.sitemaps.org/

Sitemaps are an easy way for webmasters to inform search engines about pages on their sites
that are available for crawling.

Usage
=====
To use it you should add to your project pom

    <project>
      ...
      <build>
        <!-- To define the plugin version in your parent POM -->
        <pluginManagement>
          <plugins>
            <plugin>
              <groupId>com.github.s4u.plugins</groupId>
              <artifactId>sitemapxml-maven-plugin</artifactId>
              <version>1.0.0</version>
            </plugin>
            ...
          </plugins>
        </pluginManagement>

        <!-- To use the plugin goals in your POM or parent POM -->
        <plugins>
          <plugin>
            <groupId>com.github.s4u.plugins</groupId>
            <artifactId>sitemapxml-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>gen</goal>
                    </goals>
                </execution>
             </executions>
          </plugin>
          ...
        </plugins>
      </build>
      ...
    </project>

Currently I don't know how prepare plugin to work without execution - goal configuration.
