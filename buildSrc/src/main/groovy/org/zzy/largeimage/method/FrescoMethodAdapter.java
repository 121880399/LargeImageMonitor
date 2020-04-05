package org.zzy.largeimage.method;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/2 22:00
 * 描    述：对ImageRequest类的构造方法进行字节码修改
 * ImageRequest构造方法中会对mPostprocessor字段赋值，
 * 改字段是一个后处理器，我们通过hook改字段，可以得到
 * bitmap对象。
 * 修订历史：
 * ================================================
 */
public class FrescoMethodAdapter extends AdviceAdapter {

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     *
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags
     * @param name   the method's name.
     * @param desc   the method's descriptor
     */
    public FrescoMethodAdapter( MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM5, mv, access, name, desc);
    }

    /**
     * 方法进入时
     * 1.调用ImageRequestBuilder的getSourceUri()
     * 2.调用getPostprocessor()
     * 3.设置进FrescoHook的process方法
     * 4.将返回的Postprocessor再设置进ImageRequestBuilder
    * builder.setPostprocessor(FrescoHook.process(builder.getSourceUri()，builder.getPostprocessor));
    * 作者: ZhouZhengyi
    * 创建时间: 2020/4/2 23:53
    */
    @Override
    protected void onMethodEnter() {
        super.onMethodEnter();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "com/facebook/imagepipeline/request/ImageRequestBuilder", "getSourceUri", "()Landroid/net/Uri;", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "com/facebook/imagepipeline/request/ImageRequestBuilder", "getPostprocessor", "()Lcom/facebook/imagepipeline/request/Postprocessor;", false);
        mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/aop/fresco/FrescoHook", "process", "(Landroid/net/Uri;Lcom/facebook/imagepipeline/request/Postprocessor;)Lcom/facebook/imagepipeline/request/Postprocessor;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "com/facebook/imagepipeline/request/ImageRequestBuilder", "setPostprocessor", "(Lcom/facebook/imagepipeline/request/Postprocessor;)Lcom/facebook/imagepipeline/request/ImageRequestBuilder;", false);
    }
}
