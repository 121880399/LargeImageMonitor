package org.zzy.largeimage.adapter;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.zzy.largeimage.Config;
import org.zzy.largeimage.method.FrescoMethodAdapter;
import org.zzy.largeimage.method.GlideMethodAdapter;
import org.zzy.largeimage.method.ImageLoaderMethodAdapter;
import org.zzy.largeimage.method.PicassoMethodAdapter;


/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/3/31 21:27
 * 描    述：对Glide,Picasso,Fresco,ImageLoader框架中
 * 的方法进行字节码替换
 * 修订历史：
 * ================================================
 */
public class LargeImageClassAdapter extends ClassVisitor {
    private static final String IMAGELOADER_METHOD_NAME_DESC = "(Ljava/lang/String;Lcom/nostra13/universalimageloader/core/imageaware/ImageAware;Lcom/nostra13/universalimageloader/core/DisplayImageOptions;Lcom/nostra13/universalimageloader/core/assist/ImageSize;Lcom/nostra13/universalimageloader/core/listener/ImageLoadingListener;Lcom/nostra13/universalimageloader/core/listener/ImageLoadingProgressListener;)V";
    /**
     * 当前类名
     */
    private String className;

    public LargeImageClassAdapter(ClassVisitor classWriter) {
        super(Opcodes.ASM5, classWriter);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, methodName, desc, signature, exceptions);
        //如果插件开关关闭，则不插入字节码
        if(!Config.getInstance().largeImagePluginSwitch()) {
            return mv;
        }

        // TODO: 2020/4/2 这里考虑做版本兼容
        //对Glide4.11版本的SingleRequest类的构造方法进行字节码修改
        if(className.equals("com/bumptech/glide/request/SingleRequest") && methodName.equals("<init>") && desc!=null){
            return mv == null ? null : new GlideMethodAdapter(mv,access,methodName,desc);
        }

        //对picasso的Request类的构造方法进行字节码修改
        if(className.equals("com/squareup/picasso/Request") && methodName.equals("<init>") && desc!=null){
            return mv == null ? null : new PicassoMethodAdapter(mv,access,methodName,desc);
        }

        //对Fresco的ImageRequest类的构造方法进行字节码修改
        if(className.equals("com/facebook/imagepipeline/request/ImageRequest") && methodName.equals("<init>") && desc!=null){
            return mv == null ? null : new FrescoMethodAdapter(mv,access,methodName,desc);
        }

        //对ImageLoader的ImageLoader类的displayImage方法进行字节码修改
        if(className.equals("com/nostra13/universalimageloader/core/ImageLoader") && methodName.equals("displayImage") && desc.equals(IMAGELOADER_METHOD_NAME_DESC)){
            log(className,access,methodName,desc,signature);
            return mv == null ? null : new ImageLoaderMethodAdapter(mv,access,methodName,desc);
        }
        return mv;
    }

    private void log(String className, int access, String name, String desc, String signature) {
        System.out.println("LargeImageClassAdapter===matched====>" + "  className===" + className + "   access===" + access + "   methodName===" + name + "   desc===" + desc + "   signature===" + signature);
    }
}
