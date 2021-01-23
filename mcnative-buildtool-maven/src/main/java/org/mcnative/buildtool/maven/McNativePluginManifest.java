/*
 * (C) Copyright 2019 The McNative Project (Davide Wietlisbach & Philipp Elvin Friedhoff)
 *
 * @author Davide Wietlisbach
 * @since 31.08.19, 16:04
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

import net.pretronic.libraries.document.Document;
import net.pretronic.libraries.document.type.DocumentFileType;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class McNativePluginManifest {

    @Parameter(required =true)
    private String name;

    private String id;

    @Parameter(required = true)
    private String version;

    @Parameter(required = true)
    private String main;

    @Parameter(required = true)
    private String installMcNative;

    private String messageModule;
    private String description;
    private String website;
    private String author;
    private Set<String> depends;
    private Set<String> softDepends;

    public String getName() {
        return name;
    }

    public String getInstallMcNative() {
        return installMcNative;
    }

    public String getId() {
        return id;
    }

    public String getVersion() {
        return version;
    }

    public String getMain() {
        return main;
    }

    public String getMessageModule() {
        return messageModule;
    }

    public String getDescription() {
        return description;
    }

    public String getWebsite() {
        return website;
    }

    public String getAuthor() {
        return author;
    }

    public Set<String> getDepends() {
        if(depends == null) depends = new HashSet<>();
        return depends;
    }

    public Set<String> getSoftdepends() {
        if(softDepends == null) softDepends = new HashSet<>();
        return softDepends;
    }

    public void createManifestFile(File location){
        if(depends == null) this.depends = new HashSet<>();
        getSoftdepends().addAll(Arrays.asList("McNative","PlaceHolderApi","Vault","CloudNetAPI","CloudNet-Bridge","ViaVersion","ProtocolLib"));
        if(messageModule == null) messageModule = getName().replace(" ","_").toLowerCase();
        location.getParentFile().mkdirs();
        try {
            location.createNewFile();
        } catch (IOException ignored) {}
        DocumentFileType.JSON.getWriter().write(location, Document.newDocument(this));
    }
}
