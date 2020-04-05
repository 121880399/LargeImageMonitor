package org.zzy.largeimage;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 21:50
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class Config {

    /**
     * 大图检测插件的开关
     */
    private boolean largeImagePluginSwitch = true;

    private Config(){}

    private static class Holder{
        private static Config INSTANCE = new Config();
    }

    public static Config getInstance(){
        return Holder.INSTANCE;
    }

    public boolean largeImagePluginSwitch() {
        return largeImagePluginSwitch;
    }

    public void init(LargeImageExtension extension){
        if(null != extension){
            this.largeImagePluginSwitch = extension.largeImagePluginSwitch;
        }
    }
}
