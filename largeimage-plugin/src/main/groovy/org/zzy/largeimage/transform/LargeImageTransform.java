package org.zzy.largeimage.transform;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.quinn.hunter.transform.HunterTransform;
import com.quinn.hunter.transform.RunVariant;

import org.gradle.api.Project;
import org.zzy.largeimage.weaver.LargeImageWeaver;

import java.io.IOException;
import java.util.Collection;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2020/3/31 21:05
 * 描    述：使用Hunter框架，不需用传统方式写Transform
 * 修订历史：
 * ================================================
 */
public class LargeImageTransform extends HunterTransform {

    public LargeImageTransform(Project project) {
        super(project);
        this.bytecodeWeaver = new LargeImageWeaver();
    }

    @Override
    public void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental);
    }

    @Override
    protected RunVariant getRunVariant() {
        return super.getRunVariant();
    }

}
