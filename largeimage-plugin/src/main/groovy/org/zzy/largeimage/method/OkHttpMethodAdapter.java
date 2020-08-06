package org.zzy.largeimage.method;

import com.android.ddmlib.Log;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.zzy.largeimage.Config;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 7:47
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class OkHttpMethodAdapter extends AdviceAdapter {

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor
     */
    public OkHttpMethodAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM5, mv, access, name, desc);
    }


    /**
     * 方法退出时插入
     * 这里不知道为什么在onMethodEnter方法插入会报空指针
     * interceptors.addAll(LargeImage.getInstance().getOkHttpInterceptors());
     * networkInterceptors.addAll(LargeImage.getInstance().getOkHttpNetworkInterceptors());
     * dns = LargeImage.getInstance().getDns();
     * 作者: ZhouZhengyi
     * 创建时间: 2020/4/5 9:39
     */
    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        //添加应用拦截器
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "okhttp3/OkHttpClient$Builder", "interceptors", "Ljava/util/List;");
        mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/LargeImage", "getInstance", "()Lorg/zzy/lib/largeimage/LargeImage;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/zzy/lib/largeimage/LargeImage", "getOkHttpInterceptors", "()Ljava/util/List;", false);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z", true);
        mv.visitInsn(POP);
        //添加网络拦截器
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "okhttp3/OkHttpClient$Builder", "networkInterceptors", "Ljava/util/List;");
        mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/LargeImage", "getInstance", "()Lorg/zzy/lib/largeimage/LargeImage;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/zzy/lib/largeimage/LargeImage", "getOkHttpNetworkInterceptors", "()Ljava/util/List;", false);
        mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "addAll", "(Ljava/util/Collection;)Z", true);
        mv.visitInsn(POP);
        //添加DNS
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/LargeImage", "getInstance", "()Lorg/zzy/lib/largeimage/LargeImage;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/zzy/lib/largeimage/LargeImage", "getDns", "()Lokhttp3/Dns;", false);
        mv.visitFieldInsn(PUTFIELD, "okhttp3/OkHttpClient$Builder", "dns", "Lokhttp3/Dns;");
    }
}
