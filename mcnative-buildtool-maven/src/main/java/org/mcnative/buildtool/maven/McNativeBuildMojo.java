/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 04.08.19 10:43
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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.mcnative.buildtool.maven.loader.McNativeLoaderCreator;
import org.mcnative.buildtool.maven.loader.ResourceLoaderInstaller;

import java.io.File;
import java.io.IOException;

@Mojo(name="McNative-Build", defaultPhase = LifecyclePhase.GENERATE_SOURCES,threadSafe = true)
public class McNativeBuildMojo extends AbstractMojo {

    private static final String MCNATIVE_LOADER_SOURCE_DIRECTORY_PATH = "/generated-sources/mcnative-loader/";
    private static final String MCNATIVE_LOADER_RESOURCE_DIRECTORY_PATH = "/generated-resources/mcnative-loader/";
    private static final String MCNATIVE_MANIFEST_FILE_PATH = MCNATIVE_LOADER_RESOURCE_DIRECTORY_PATH+"mcnative.json";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter( name = "mcnative-loader-location",defaultValue = "${project.basedir}/lib/")
    private String mcnativeLoaderLocation;

    @Parameter( name = "mcnative-loader-version",readonly = true)
    private String mcnativeLoaderVersion;

    @Parameter( name = "resource-loader-version",readonly = true)
    private String resourceLoaderVersion;

    @Parameter( name = "template-generator" ,readonly = true,defaultValue = "false")
    private boolean templateGenerator;

    @Parameter( property = "manifest",readonly = true,required = true)
    private McNativePluginManifest manifest;

    @Override
    public void execute() throws MojoFailureException, MojoExecutionException {
        File output = new File(project.getBuild().getOutputDirectory());
        File sourceDirectory = new File(output.getParent(),MCNATIVE_LOADER_SOURCE_DIRECTORY_PATH);
        File resourceDirectory = new File(output.getParent(),MCNATIVE_LOADER_RESOURCE_DIRECTORY_PATH);
        File manifestFile = new File(output.getParent(),MCNATIVE_MANIFEST_FILE_PATH);

        project.addCompileSourceRoot(sourceDirectory.getPath());

        if(manifestFile.exists()) return;

        sourceDirectory.mkdirs();
        resourceDirectory.mkdirs();

        this.manifest.createManifestFile(manifestFile);

        String basePackage = templateGenerator ? "org.mcnative.resource."+BuildUtil.transformUUIDtoStringId(manifest.getId()) : project.getGroupId()+".loader";

        ResourceLoaderInstaller installer = new ResourceLoaderInstaller(getLog(),resourceLoaderVersion
                ,new File(mcnativeLoaderLocation),sourceDirectory);

        McNativeLoaderCreator creator = new McNativeLoaderCreator(getLog(),mcnativeLoaderVersion,basePackage
                ,new File(mcnativeLoaderLocation),sourceDirectory,resourceDirectory);

        installer.downloadSource();
        creator.downloadSource();

        installer.unpackLoader();
        creator.unpackLoader();

        creator.renamePackages();

        creator.createManifests(manifest);

        try {
            FileUtils.copyDirectoryStructure(resourceDirectory,new File(project.getBuild().getOutputDirectory()));
        } catch (IOException e) {
            e.printStackTrace();
            throw new MojoFailureException(e.getMessage());
        }
    }
}
