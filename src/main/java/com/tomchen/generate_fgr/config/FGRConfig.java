package com.tomchen.generate_fgr.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

public class FGRConfig {

    private FGRConfig() {
    }

    private static FGRConfig fgrConfig = new FGRConfig();

    public static FGRConfig getInstance() {
        return fgrConfig;
    }

    private final String TARGET_DIR_NAME = "images";

    public boolean isTargetDir(Project project, VirtualFile file) {
        System.out.println("isTargetDir " + file.getPath() +"+++"+getTargetDirFilePath(project));
        return getTargetDirFilePath(project).equalsIgnoreCase(file.getParent().getPath());
    }

    public String getTargetDirFilePath(Project project){
        return (project.getBasePath() + File.separator + TARGET_DIR_NAME);
    }
}
