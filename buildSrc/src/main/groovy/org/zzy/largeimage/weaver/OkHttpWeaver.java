package org.zzy.largeimage.weaver;

import com.quinn.hunter.transform.asm.BaseWeaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.zzy.largeimage.adapter.OkHttpClassAdapter;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/4/5 7:29
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class OkHttpWeaver extends BaseWeaver {

    @Override
    protected ClassVisitor wrapClassWriter(ClassWriter classWriter) {
        return new OkHttpClassAdapter(classWriter);
    }
}
