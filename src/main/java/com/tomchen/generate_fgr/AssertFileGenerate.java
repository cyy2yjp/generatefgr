package com.tomchen.generate_fgr;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import org.apache.log4j.helpers.LogLog;

import java.io.File;
import java.io.IOException;

public class AssertFileGenerate {

    VirtualFile virtualFile;
    Project project;

    public void doCreateAssertFile(Project project, VFileEvent event) {
        virtualFile = event.getFile();
        this.project = project;
        writeClass();
    }

    private void writeClass() {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                VirtualFile srcFile = LocalFileSystem.getInstance().findFileByPath(project.getBasePath() + File.separator + "src");
                VirtualFile assertDir = LocalFileSystem.getInstance().findFileByPath(project.getBasePath() + File.separator + "Images");
                //判断目录下是有assert文件夹
                //没有就创建一个
                VirtualFile resourceFileDir = srcFile.findChild(getResourcesDirPath());
                if (resourceFileDir == null) {
                    resourceFileDir = srcFile.createChildDirectory(null, getResourcesDirPath());
                }

                if (resourceFileDir == null) {
                    return;
                }

                if (assertDir == null || assertDir.getChildren() == null || assertDir.getChildren().length == 0) {
                    return;
                }

                VirtualFile rDartFile = resourceFileDir.findOrCreateChildData(null, "r.dart");
                LogLog.debug("Start generate R file...");
                String content = "import 'package:flutter/material.dart';\n class R {\n";

                VirtualFile[] assets = assertDir.getChildren();
                for (VirtualFile fileEntity : assets) {
                    final String path = fileEntity.getPath().replace(project.getBasePath() + File.separator, "");

                    final String variableName = fileEntity.getNameWithoutExtension();
                    LogLog.debug("variableName" + variableName);
                    if (!variableName.startsWith(".")) {
                        String vari = "\tstatic const String " + variableName + " ='" + path + "';\n";
                        content += vari;
                    }
                }

                content += "}\n";
                content += "class Assets {\n";

                for (VirtualFile fileEntity : assets) {
                    final String variName = fileEntity.getNameWithoutExtension();
                    LogLog.debug("variableName" + variName);
                    if (!variName.startsWith(".")) {
                        String assetVari = "\tstatic const AssetImage " + variName + " = const AssetImage(R." + variName + ");\n";
                        content += assetVari;
                    }
                }
                content += "}";

                LogLog.debug("Generate R file complete");
                rDartFile.setBinaryContent(content.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private String getResourcesDirPath() {
        return "Resources";
    }
}
