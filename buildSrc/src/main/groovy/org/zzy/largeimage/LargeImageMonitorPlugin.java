package org.zzy.largeimage;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.zzy.largeimage.transform.LargeImageTransform;
import org.zzy.largeimage.transform.OkHttpTransform;
import org.zzy.largeimage.transform.UrlConnectionTransform;

import java.util.Collections;
import java.util.List;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/3/31 20:49
 * 描    述：调试前执行gradle assembleDebug -Dorg.gradle.daemon=false -Dorg.gradle.debug=true命令，然后按debug按钮，接着执行task debug便会生效
 * 修订历史：
 * ================================================
 */
public class LargeImageMonitorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        List<String> taskNames = project.getGradle().getStartParameter().getTaskNames();
        //如果是Release版本，则不进行字节码替换
        for(String taskName : taskNames){
            if(taskName.contains("Release")){
                return;
            }
        }

        AppExtension appExtension = (AppExtension)project.getProperties().get("android");
        //创建自定义扩展
        project.getExtensions().create("largeImageMonitor",LargeImageExtension.class);
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                LargeImageExtension extension = project.getExtensions().getByType(LargeImageExtension.class);
                Config.getInstance().init(extension);
            }
        });
        //将自定义Transform添加到编译流程中
        appExtension.registerTransform(new LargeImageTransform(project), Collections.EMPTY_LIST);
        //添加OkHttp
        appExtension.registerTransform(new OkHttpTransform(project),Collections.EMPTY_LIST);
        //添加UrlConnection
        appExtension.registerTransform(new UrlConnectionTransform(project),Collections.EMPTY_LIST);
    }
}
