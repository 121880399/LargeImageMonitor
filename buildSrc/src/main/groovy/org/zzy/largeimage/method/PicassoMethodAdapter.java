package org.zzy.largeimage.method;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/2 10:38
 * 描    述：对Request类的构造方法字节码进行修改
 * Request类构造方法中有一个List<Transformation> transformations
 * 参数，该参数会在piaccso得到bitmap以后进行遍历，对bitmap进行
 * 一些变换操作，我们可以插入自己的Transformation然后在里面
 * 得到我们想要的数据。
 * 修订历史：
 * ================================================
 */
public class PicassoMethodAdapter extends AdviceAdapter {

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags .
     * @param name   the method's name.
     * @param desc   the method's descriptor.
     */
    public PicassoMethodAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM5, mv, access, name, desc);
    }

    /**
    * 方法进入时
     * 1.拿到方法第一个参数Uri
     * 2.拿到方法第四个参数 List<Transformation> transformations
     * 3.把它们传入hook方法
     * 4.在方法中加入我们自己的Transformation
     * 5.将设置好以后的 List<Transformation> transformations返回给第四个参数
     * transformations = PicassoHook.process(uri,transformations);
    * 作者: ZhouZhengyi
    * 创建时间: 2020/4/2 11:26
    */
    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/aop/picasso/PicassoHook", "process", "(Landroid/net/Uri;Ljava/util/List;)Ljava/util/List;", false);
        mv.visitVarInsn(ASTORE, 4);
    }
}
