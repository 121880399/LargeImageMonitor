package org.zzy.largeimage;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.api.ApplicationVariant;
import com.android.build.gradle.internal.api.BaseVariantImpl;
import com.android.build.gradle.tasks.MergeResources;

import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.zzy.largeimage.transform.LargeImageTransform;
import org.zzy.largeimage.transform.OkHttpTransform;
import org.zzy.largeimage.transform.UrlConnectionTransform;
import org.zzy.largeimage.utils.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/3/31 20:49
 * 描    述：调试前执行gradle assembleDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true命令，然后按debug按钮，接着执行task debug便会生效
 * 修订历史：2020/4/13 v1.0.0
 * github:https://github.com/121880399/LargeImageMonitor
 * ================================================
 */
public class LargeImageMonitorPlugin implements Plugin<Project> {

    private List<String> largeImageList = new ArrayList();

    @Override
    public void apply(Project project) {
        List<String> taskNames = project.getGradle().getStartParameter().getTaskNames();
        //如果是Release版本，则不进行字节码替换
        //暂时注释掉，这个版本支持线上自动压缩
//        for(String taskName : taskNames){
//            if(taskName.contains("Release")){
//                return;
//            }
//        }
        AppExtension appExtension = (AppExtension)project.getProperties().get("android");
        //创建自定义扩展
        project.getExtensions().create("largeImageMonitor",LargeImageExtension.class);
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                LargeImageExtension extension = project.getExtensions().getByType(LargeImageExtension.class);
                Config.getInstance().init(extension);
                //判断是否开启自动压缩
                if(extension.openAutoCompress) {
                    DomainObjectSet<ApplicationVariant> applicationVariants = appExtension.getApplicationVariants();
                    applicationVariants.all(new Action<ApplicationVariant>() {
                        @Override
                        public void execute(ApplicationVariant applicationVariant) {
                            //得到资源合并Task
                            MergeResources mergeResources = applicationVariant.getMergeResourcesProvider().get();
                            //创建压缩任务
                            Task largeImageCompressTask = project.task("largeImageCompressTask");
                            largeImageCompressTask.doLast(new Action<Task>() {
                                @Override
                                public void execute(Task task) {
                                    Set<File> resFiles =
                                            ((BaseVariantImpl) applicationVariant).getAllRawAndroidResources().getFiles();
                                    List<String> cacheList = new ArrayList();
                                    List<File> imageList = new ArrayList();

                                    for(File dir : resFiles){
                                        traverseResDir(dir,imageList,cacheList);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
        //将自定义Transform添加到编译流程中
        appExtension.registerTransform(new LargeImageTransform(project), Collections.EMPTY_LIST);
        //添加OkHttp
        appExtension.registerTransform(new OkHttpTransform(project),Collections.EMPTY_LIST);
        //添加UrlConnection
        appExtension.registerTransform(new UrlConnectionTransform(project),Collections.EMPTY_LIST);
    }

    /**
    * 遍历资源目录
    * 作者: ZhouZhengyi
    * 创建时间: 2020/8/6 16:31
    */
    private void traverseResDir(File file,List<File> imageList,List<String> cacheList){
        //判断缓存列表中是否存在该地址
        if(cacheList.contains(file.getAbsolutePath())){
            return;
        }else{
            cacheList.add(file.getAbsolutePath());
        }
        //判断是否是目录
        if(file.isDirectory()){
            File[] files = file.listFiles();
            if(files != null) {
                for (File subFile : files) {
                    if(subFile.isDirectory()){
                        traverseResDir(subFile,imageList,cacheList);
                    }else{
                        filterImage(subFile,imageList);
                    }
                }
            }
        }else{
            filterImage(file,imageList);
        }
    }

    /**
    * 过滤图片文件
    * 作者: ZhouZhengyi
    * 创建时间: 2020/8/6 16:45
    */
    private void filterImage(File file,List<File> imageList){
        if(Config.getInstance().getWhiteList().contains(file.getName()) || !ImageUtil.isImageFile(file)){
            return;
        }

        if(Config.getInstance().isOpenAutoCompress() && ImageUtil.isLargeImageFile(file,Config.getInstance().getFileSizeThreshold())){
            largeImageList.add(file.getAbsolutePath());
        }
        imageList.add(file);
    }


}
