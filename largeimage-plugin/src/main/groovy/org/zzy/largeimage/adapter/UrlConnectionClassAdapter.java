package org.zzy.largeimage.adapter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.zzy.largeimage.Config;
import org.zzy.largeimage.method.UrlConnectionMethodAdapter;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 16:20
 * 描    述：Hook UrlConnection的目的是为了将
 * UrlConnection代理到OKhttp上面，这样通过网络下载图片就可以
 * 通过okhttp统一的处理。
 * 修订历史：
 * ================================================
 */
public class UrlConnectionClassAdapter extends ClassVisitor {

    /**
     * 类名
     */
    private String className;

    public UrlConnectionClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    /**
     * 这个方法跟其他几个methodAdapter不一样
     * 其他的methodAdapter是根据类名和方法名来进行hook
     * 也就是说当访问到某个类的某个方法时进行
     * 而这个方法是，所有的类和方法都有可能存在hook，
     * 所以这里不做类和方法的判断
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/5 17:25
     */
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        //如果插件开关关闭，则不插入字节码
        if (!Config.getInstance().largeImagePluginSwitch()) {
            return methodVisitor;
        }
        return methodVisitor == null ? null : new UrlConnectionMethodAdapter(className, methodVisitor, access, name, desc);
    }
}
