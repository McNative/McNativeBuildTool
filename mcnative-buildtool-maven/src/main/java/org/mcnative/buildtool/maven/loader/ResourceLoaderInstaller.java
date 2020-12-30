/*
 * (C) Copyright 2020 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 07.02.20, 21:41
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

import net.pretronic.libraries.utility.http.HttpClient;
import net.pretronic.libraries.utility.http.HttpResult;
import net.pretronic.libraries.utility.io.archive.ZipArchive;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

public class ResourceLoaderInstaller {

    private static final String RESOURCE_LOADER_URL = "https://repository.pretronic.net/repository/pretronic/net/pretronic/libraries/pretroniclibraries-resourceloader/{version}/pretroniclibraries-resourceloader-{version}-sources.jar";
    private static final String RESOURCE_LOADER_BASE_NAME = "resource-loader-${version}.jar";

    private final Log log;
    private final String version;
    private final File location;
    private final File sourceDirectory;

    public ResourceLoaderInstaller(Log log, String version, File location,File sourceDirectory) {
        this.log = log;
        this.version = version;
        this.location = new File(location,RESOURCE_LOADER_BASE_NAME.replace("${version}",version));
        this.sourceDirectory = sourceDirectory;
    }

    public void downloadSource() throws MojoFailureException {
        if(location.exists()) return;
        location.getParentFile().mkdirs();
        log.info("Downloading Resource loader v"+version+".");
        HttpClient client = new HttpClient();

        client.setUrl(RESOURCE_LOADER_URL.replace("{version}",this.version));

        HttpResult result = client.connect();
        if(result.getCode() == 200) result.save(location);
        else{
            String message = String.format("Could not download Resource loader version %s (%s).",version,result.getCode());
            throw new MojoFailureException(result,message,message);
        }
        result.close();
        log.info("Successfully downloaded Resource loader.");
    }

    public void unpackLoader() throws MojoExecutionException {
        log.info("Unpacking Resource loader");
        ZipArchive archive = new ZipArchive(location);
        archive.extract(sourceDirectory);
        try {
            FileUtils.deleteDirectory(new File(sourceDirectory,"META-INF/"));
        } catch (Exception exception) {
            exception.printStackTrace();
            throw new MojoExecutionException("Could not unpack Resource loader",exception);
        }
    }

}
