/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 27.10.19, 13:15
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

package org.mcnative.buildtool.maven.loader;

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import net.pretronic.libraries.utility.http.HttpClient;
import net.pretronic.libraries.utility.http.HttpResult;
import net.pretronic.libraries.utility.io.FileUtil;
import net.pretronic.libraries.utility.io.IORuntimeException;
import net.pretronic.libraries.utility.io.archive.ZipArchive;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;
import org.mcnative.buildtool.maven.McNativePluginManifest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

public class McNativeLoaderCreator {

    private static final String MCNATIVE_LOADER_URL = "https://repository.pretronic.net/repository/pretronic/org/mcnative/loader/McNativeResourceLoader/{version}/McNativeResourceLoader-{version}-sources.jar";
    private static final String MCNATIVE_LOADER_BASE_NAME = "mcnative-loader-{version}.jar";
    private static final String MCNATIVE_LOADER_BASE_PACKAGE = "org.mcnative.loader";
    private static final String MCNATIVE_LOADER_BASE_PATH = MCNATIVE_LOADER_BASE_PACKAGE.replace(".","/");

    private final Log log;
    private final String version;

    private final String basePackage;
    private final File baseFolder;
    private final File location;

    private final File sourceDirectory;
    private final File resourceDirectory;

    public McNativeLoaderCreator(Log log, String version, String basePackage, File location, File sourceDirectory, File resourceDirectory) {
        this.log = log;
        this.version = version;
        this.basePackage = basePackage;
        this.baseFolder = new File(sourceDirectory,basePackage.replace(".","/"));
        this.location = new File(location,MCNATIVE_LOADER_BASE_NAME.replace("{version}",version));
        this.sourceDirectory = sourceDirectory;
        this.resourceDirectory = resourceDirectory;
    }

    public void downloadSource() throws MojoFailureException{
        if(location.exists()) return;
        location.getParentFile().mkdirs();
        log.info("Downloading McNative loader v"+version+".");
        HttpClient client = new HttpClient();

        client.setUrl(MCNATIVE_LOADER_URL.replace("{version}",this.version));

        HttpResult result = client.connect();
        if(result.getCode() == 200) result.save(location);
        else{
            String message = String.format("Could not download McNative loader version %s (%s).",version,result.getCode());
            throw new MojoFailureException(result,message,message);
        }
        result.close();
        log.info("Successfully downloaded Mcnative loader.");
    }

    public void unpackLoader() throws MojoExecutionException {
        log.info("Unpacking McNative loader");
        ZipArchive archive = new ZipArchive(location);
        archive.extract(sourceDirectory);
        try {
            FileUtils.deleteDirectory(new File(sourceDirectory,"META-INF/"));
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new MojoExecutionException("Could not unpack McNative loader",exception);
        }
    }

    public void renamePackages() {
        try {
            FileUtils.rename(new File(sourceDirectory,MCNATIVE_LOADER_BASE_PATH),baseFolder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IORuntimeException(e);
        }
        FileUtil.processFilesHierarchically(sourceDirectory, file -> {
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(file.toPath()), charset);
            content = content.replaceAll(MCNATIVE_LOADER_BASE_PACKAGE,basePackage);
            Files.write(file.toPath(), content.getBytes(charset));
        });
    }

    public void createManifests(McNativePluginManifest manifest){
        Document document = Document.newDocument(manifest);
        createBungeeCordManifest(document.copy(null));
        createBukkitManifest(document.copy(null));
        createLoaderInfo(manifest);
    }

    private void createBungeeCordManifest(Document document){
        document.set("main",basePackage+".bootstrap.standalone.BungeeCordMcNativePluginBootstrap");
        document.set("version","McNative loader ("+version+")");
        DocumentFileType.YAML.getWriter().write(new File(resourceDirectory,"bungee.yml"),document);
    }

    private void createBukkitManifest(Document document){
        document.set("main",basePackage+".bootstrap.standalone.BukkitMcNativePluginBootstrap");
        document.set("version","McNative loader ("+version+")");
        document.rename("softDepends","softdepend");
        DocumentFileType.YAML.getWriter().write(new File(resourceDirectory,"plugin.yml"),document);
    }

    private void createLoaderInfo(McNativePluginManifest manifest){
        Properties info = new Properties();
        info.setProperty("installMcNative",String.valueOf(manifest.isInstallMcNative()));
        info.setProperty("plugin.name",manifest.getName());
        info.setProperty("plugin.id",manifest.getId());
        info.setProperty("plugin.website",manifest.getWebsite());
        info.setProperty("plugin.author",manifest.getAuthor());

        try {
            info.store(new FileOutputStream(new File(resourceDirectory,"mcnative-loader.properties")), null);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

}
