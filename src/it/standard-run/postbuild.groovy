/*
 * Copyright 2020 Slawomir Jaranowski
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
def buildLog = new File( basedir, 'build.log' )

assert buildLog.text.contains('[INFO] Generate sitemap.xml - Start')
assert buildLog.text.contains('[INFO] Generate sitemap.xml - OK')
assert buildLog.text.contains('[INFO] BUILD SUCCESS')

def sitemap = new File( basedir, 'target/site/sitemap.xml' )

assert sitemap.text.contains('<?xml version="1.0" encoding="UTF-8" standalone="no"?>')
assert sitemap.text.contains('<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">')
assert sitemap.text.contains('<loc>https://example.com/index.html</loc>')
