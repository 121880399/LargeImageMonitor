package org.zzy.largeimage.method;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/1 15:43
 * 描    述：对SingleRequest类中的构造方法进行字节码修改
 * 构造方法会对SingleRequest类进行初始化，其中有一个
 * requestListeners参数，它是一个list类型，
 * 在图片准备完毕时会对该list进行遍历回调，我们只需要在
 * 该list中添加我们自定义的listener，遍历时就会回调我们
 * 的方法，从而拿到图片数据。
 * 修订历史：
 * ================================================
 */
public class GlideMethodAdapter extends AdviceAdapter {

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor ).
     */
    public GlideMethodAdapter( MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM5, mv, access, name, desc);
    }

    /**
    * 方法退出时
     * 1.先拿到requestListeners
     * 2.然后对其进行修改
     * 3.将修改后的requestListeners设置回去
     * requestListeners=GlideHook.process(requestListeners);
    * 作者: ZhouZhengyi
    * 创建时间: 2020/4/1 15:51
    */
    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "com/bumptech/glide/request/SingleRequest", "requestListeners", "Ljava/util/List;");
        mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/aop/glide/GlideHook", "process", "(Ljava/util/List;)Ljava/util/List;", false);
        mv.visitFieldInsn(PUTFIELD, "com/bumptech/glide/request/SingleRequest", "requestListeners", "Ljava/util/List;");
    }
}
