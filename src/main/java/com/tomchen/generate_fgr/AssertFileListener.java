package com.tomchen.generate_fgr;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.psi.PsiManager;
import com.tomchen.generate_fgr.config.FGRConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AssertFileListener implements BulkFileListener {

    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        for (VFileEvent event : events) {
//            VFileEvent fileCreateEvent;
//            //删除或者添加都掉用这个方法
//            if (event instanceof VFileCreateEvent
//                    || event instanceof VFileDeleteEvent
//                    || event instanceof VFileCopyEvent
//            || event instanceof VFileMoveEvent) {
//                fileCreateEvent = event;
//            } else {
//                continue;
//            }
            if (event.getFile().getParent() == null) {
                continue;
            }
            Project project = getProject(event);
            if (project == null) {
                continue;//this is not a file create event we need to do any fixup on
            }

            if (FGRConfig.getInstance().isTargetDir(project, event.getFile())) {
                new AssertFileGenerate().doCreateAssertFile(project, event);
                return;
            }
        }
    }

    private static Project getProject(VFileEvent event) {
        Object requestor = event.getRequestor();
        if (requestor instanceof PsiManager) {
            PsiManager psiManager = (PsiManager) requestor;
            return psiManager.getProject();
        }
        return null;
    }
}
