package org.zzy.largeimage.method;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 16:50
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class UrlConnectionMethodAdapter extends AdviceAdapter {

    private String className;

    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags).
     * @param name   the method's name.
     * @param desc   the method's descriptor
     */
    public UrlConnectionMethodAdapter(String className, MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM5, mv, access, name, desc);
        this.className = className;
    }

    /**
    * 这里复写的方法与其他的methodAdapter也不同
     * 其他的methodAdapter是在方法进入或者退出时操作
     * 而这个methodAdapter是根据指令比较的
     * 这个方法的意思是当方法被访问时调用
     * @param opcode 指令
     * @param owner 操作的类
     * @param name 方法名称
     * @param desc 方法描述  （参数）返回值类型
    * 作者: ZhouZhengyi
    * 创建时间: 2020/4/5 17:29
    */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        //所有的类和方法，只要存在调用openConnection方法的指令，就进行hook
        if(opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/net/URL")
            && name.equals("openConnection")&& desc.equals("()Ljava/net/URLConnection;")){
            mv.visitMethodInsn(INVOKEVIRTUAL,"java/net/URL", "openConnection", "()Ljava/net/URLConnection;", false);
            super.visitMethodInsn(INVOKESTATIC,"org/zzy/lib/largeimage/aop/urlconnection/UrlConnectionHook","process","(Ljava/net/URLConnection;)Ljava/net/URLConnection;",false);
        }else if(opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/net/URL")
                && name.equals("openConnection")&& desc.equals("(Ljava/net/Proxy;)Ljava/net/URLConnection;")){
            //public URLConnection openConnection(Proxy proxy)
            mv.visitMethodInsn(INVOKEVIRTUAL,"java/net/URL", "openConnection", "(Ljava/net/Proxy;)Ljava/net/URLConnection;", false);
            super.visitMethodInsn(INVOKESTATIC,"org/zzy/lib/largeimage/aop/urlconnection/UrlConnectionHook","process","(Ljava/net/URLConnection;)Ljava/net/URLConnection;",false);
        }else{
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

    }
}
