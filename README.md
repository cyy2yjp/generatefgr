# generatefgr
自动生成flutter下resource R. 资源文件
插件基本功能已经完成，可以直接使用

效果如下
![image.png](https://upload-images.jianshu.io/upload_images/1030569-ac6a4036b8ac0076.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
#####一.开发环境
- Intellij:0.4.18
- kotlin:1.3.71
- Idea:2020.1
- mac：
     - 系统：10.15.3
     - 内存： 16G

前期准备环境搭建就不赘述了，参考了一下[环境搭建](https://www.jianshu.com/p/722841c6d0a9) ，中间遇到的一些坑这里描述下
#####二.踩坑小记
**java heap size**
现象：项目创建完成sync插件Sdk报 **java heap size**
解决步骤：gradle.properties  添加org.gradle.jvmargs=-Xmx2048M 
结果：7m32s693后同步成功

**代码无联想，异常 ，ClassNotFound**
现象：创建类代码无提示，gradle view 中 run **runIde**  一堆异常，代码跑不动提示创建的类异常，弄了好一会
解决步骤：重新创建了一个新项目一步步点下去自己好了，可能新项目原因
```
020-04-25 14:24:35,712 [  59735]   WARN - com.intellij.util.xmlb.Binding - no accessors for class org.jetbrains.kotlin.idea.highlighter.KotlinDefaultHighlightingSettingsProvider 
2020-04-25 14:24:36,178 [  60201]   WARN - Container.ComponentManagerImpl - Do not use constructor injection (requestorClass=com.android.tools.idea.lang.androidSql.room.RoomDependencyChecker) 

```
好了开始开发
**背景**：因为看到别人用 runner 实现了一个获取Flutter文件夹下图片资源生成R.xxx 实际指向图片Path，但是runner 每次需要跑一遍命令，我想是不能是能用IDEA插件开发实现，效果大致如下
![image.png](https://upload-images.jianshu.io/upload_images/1030569-81522d66d8fe3be7.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/700)

我对插件开发也不熟，打开官网看了下发现恰好有这个[Plugin Listener]([https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_listeners.html](https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_listeners.html)
)  我翻译也不好 打开google 搜索一看 大致能懂
这个能监听真个IDEA所有文件变动事件，创建，删除，移动，属性改变，删除，也可以不继承**BulkFileListener**单独继承，有兴趣的可以看看，官网还介绍了事件如何传递
```
public class MyVfsListener implements BulkFileListener {
    @Override
    public void after(@NotNull List<? extends VFileEvent> events) {
        // handle the events
    }
}

```
plugin.xml 下的配置
```
<applicationListeners>
  <listener class="myPlugin.MyVfsListener" topic="com.intellij.openapi.vfs.newvfs.BulkFileListener"/>
</applicationListeners>
```
点击右边的 **Gradle View**  跑runIde 也可以右键 选择debug模式，这是会启动一个的IDEA 运行在沙箱环境，我第一次跑 runIde 没有弹出新IDEA ，傻傻的等了好久，关掉重新启动就好了
![image.png](https://upload-images.jianshu.io/upload_images/1030569-bc09fd437b50bc34.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

测试后发现果然能获取到文件变动后的事件，网上搜了下如何创建dart文件，大致是说PSI，java文件，找了一个Flutter 的jsonToDart 插件用的是 VFturalFile就可以
``
 rDartFile.setBinaryContent(content.getBytes());
``
因为是要往指定目录下创建文件
```
//获取src 目录
   VirtualFile srcFile = LocalFileSystem.getInstance().findFileByPath(project.getBasePath() + File.separator + "src");
      File.separator + "Images");
                //判断目录下是有assert文件夹
                //没有就创建一个
                VirtualFile resourceFileDir = srcFile.findChild(getResourcesDirPath());
                if (resourceFileDir == null) {
                    resourceFileDir = srcFile.createChildDirectory(null, getResourcesDirPath());
                }
```
获取文件都需要使用project对象，这里点击看了下注释
```
/**
 * An object representing an IntelliJ project.
 *
 * <p>To get all of its modules, use {@code ModuleManager.getInstance(project).getModules()}.
 *
 * <p>To iterate over all project source files and directories,
 * use {@code ProjectFileIndex.SERVICE.getInstance(project).iterateContent(iterator)}.
 *
 * <p>To get the list of all open projects, use {@code ProjectManager.getInstance().getOpenProjects()}.
 */
百度翻译
表示IntelliJ项目的对象。
要获取其所有模块，请使用ModuleManager.getInstance（project.get modules（）。
要迭代所有项目源文件和目录，请使用ProjectFileIndex.SERVICE.getInstance（project.iterateContent（iterator）。
要获取所有打开项目的列表，请使用ProjectManager.getInstance（）.getOpenProjects（）
```
没有介绍怎么获取，网上大都是通过Anction获取，这是我直接到github上通过BulkListener 关键字搜索
![image.png](https://upload-images.jianshu.io/upload_images/1030569-e579391b5d2d7f6b.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

```
private static Project getProject(VFileEvent event) {
    Object requestor = event.getRequestor();
    if (requestor instanceof PsiManager) {
      PsiManager psiManager = (PsiManager) requestor;
      return psiManager.getProject();
    }
    return null;
  }
```
事后，我发现官网上还有另外一种方式
![image.png](https://upload-images.jianshu.io/upload_images/1030569-913e3f169f66b3c6.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
[如何不通过AnAction获取当前打开的Project]([https://www.jetbrains.org/intellij/sdk/docs/basics/faq.html](https://www.jetbrains.org/intellij/sdk/docs/basics/faq.html)
)
```
Project[] projects = ProjectManager.getInstance().getOpenProjects();
Project activeProject = null;
for (Project project : projects) {
    Window window = WindowManager.getInstance().suggestParentWindow(project);
    if (window != null && window.isActive()) {
        activeProject = project;
    }
}
```
好了，剩下的就是文件夹内的文件列表获取文件名并生成r.dart
最终效果如下，和 **flutter runner wach** 实现了一样的效果
![image.png](https://upload-images.jianshu.io/upload_images/1030569-ac6a4036b8ac0076.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

#####总结
整体还是挺简单的，主要是插件开发环境问题，第一次创建项目报错卡了好一会。通过对插件的功能的开发大致了解了插件相关知识，相信以后能做更多有趣的事情。
项目git地址[FlutterRGenerate](https://github.com/cyy2yjp/generatefgr)

简书地址
https://www.jianshu.com/p/a06cc2d8cd52
