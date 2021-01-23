/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 26.10.19, 11:49
 *
 * The McNative Project is under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.mcnative.buildtool.maven;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;
import java.util.Map;

@Mojo(name="McNative-Package", defaultPhase = LifecyclePhase.PACKAGE,threadSafe = true, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class McNativePackageMojo extends AbstractMojo {

    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;

    @Parameter( name = "template-generator" ,readonly = true,defaultValue = "false")
    private boolean templateGenerator;

    @Parameter( property = "manifest",readonly = true,required = true)
    private McNativePluginManifest manifest;

    @Component
    private Map<String, Archiver> archivers;

    @Parameter(defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    @Override
    public void execute() throws MojoExecutionException {
        try{
            String basePackage = templateGenerator ? "org.mcnative.resource."+BuildUtil.transformUUIDtoStringId(manifest.getId()) : project.getGroupId()+".loader";
            String basePath = basePackage.replace(".","/");
            String name = templateGenerator ? "loader.jar" : project.getArtifact().getArtifactId()+"-"+project.getArtifact().getVersion()+"-loader.jar";

            MavenArchiver archiver = new MavenArchiver();
            archiver.setArchiver((JarArchiver) archivers.get("jar"));
            archiver.setOutputFile(new File(project.getBuild().getDirectory(),name));
            archiver.getArchiver().addDirectory(new File(project.getBuild().getOutputDirectory())
                    ,new String[]{
                            "**/net/pretronic/libraries/resourceloader/**"
                            ,"**/"+basePath+"/**"
                            ,"**/mcnative-loader.properties"
                            ,"**/plugin.yml"
                            ,"**/bungee.yml"
                    },new String[]{"META-INF/maven/**"});

            archiver.createArchive(session,project,archive);
        }catch (Exception exception){
            exception.printStackTrace();
            throw new MojoExecutionException("Error assembling JAR",exception);
        }
    }
}
