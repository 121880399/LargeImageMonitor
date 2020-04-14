package org.zzy.largeimage.adapter;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.zzy.largeimage.Config;
import org.zzy.largeimage.method.OkHttpMethodAdapter;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 7:31
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class OkHttpClassAdapter extends ClassVisitor {

    private String className;

    public OkHttpClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, name, desc, signature, exceptions);
        //如果插件开关关闭，则不插入字节码
        if(!Config.getInstance().largeImagePluginSwitch()) {
            return methodVisitor;
        }
        if(className.equals("okhttp3/OkHttpClient$Builder") && name.equals("<init>") && desc.equals("()V")){
            return methodVisitor == null ? null : new OkHttpMethodAdapter(methodVisitor,access,name,desc);
        }
        return methodVisitor;
    }
}
