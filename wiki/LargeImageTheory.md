# 通过ASM实现大图监控

### 1.背景

最近看滴滴开源的Dokit框架中有一个大图监控的功能，可以对图片的文件大小和所占用的内存大小设置一个阈值，当图片超过该值的时候进行提示。这个功能对于我们在做APK体积压缩，内存管理的时候还是很有用的，比如当我们要从后台返回的连接中加载一张图片，这张图片的大小我们是不知道的，虽然现在大家都使用Glide等三方 图片加载框架，框架会自动对图片进行压缩，但是依然会出现压缩后所占内存超过预期的情况。这时候我们可以在开发、测试和预生产阶段使用大图监控来识别出那些超标的图片。



### 2.需求

在讨论如何做之前，我们必须明确我们要做什么。该大图监控框架我觉得应该实现以下功能：

1. 能对图片的文件大小和所占用的内存大小设置阈值，超过其中之一则报警。
2. 能够得到超标图片的详细信息，包括当前文件大小，所占用内存，图片分辨率，图片的略缩图，图片的加载地址，view的尺寸。
3. 能够通过弹窗或者列表的方式查看当前超标的图片信息。
4. 不论是本地加载图片还是网络加载图片都能够进行监控。



### 3.实现思路

​	要实现对图片文件大小和所占内存的监控，那么我们就得先知道图片的文件大小和加载该图片所耗费的内存。目前加载图片一般都使用第三方框架，所以可以对常用的图片加载框架进行Hook,这里主要对主流的四种图片加载框架进行Hook操作。

1. Glide

2. Picasso

3. Fresco

4. Image Loader

​    以从网络加载一张图片举例，当使用图片框架加载一张网络图片时，会使用OkHttp或者是HttpUrlconnection去下载该图片,这时候我们就能得到图片文件的大小。当图片框架将图片文件构造成Bitmap对象以后，我们又能得到其所占用的内存，这样我们就同时的得到了图片的文件大小和所占用的内存。那么这里我们也必须对OkHttp和HttpUrlconnection进行Hook。

​	既然要对三方框架进行Hook操作，那么我们如何进行Hook呢？在选择Hook的实现方案时，我对以下几种方案进行了调研。

1. 反射+动态代理

2. ASM

3. AspectJ

4. ByteBuddy

​    首先反射+动态代理 只能在程序运行时进行，这样会影响效率，所以暂不考虑。其他三种方案都能够在编译期进行字节码插桩，ASM直接操纵字节码，阅读起来不那么友好。AspectJ以前用过，经常出一些莫名其妙的问题，体验不是很好。ByteBuddy 封装了ASM，据说效率很高，而且使用JAVA编写，代码可读性好，只是网上的资料太少了，大部分都是那么几篇文章再转发。所以这里最后选择了ASM实现。

有了ASM进行字节码插入，那什么时候将我们编写好的字节码插入到第三方框架中呢？

![](https://raw.githubusercontent.com/121880399/PictureManager/master/20200418112226.png)

​	我们从Apk打包流程图中可以看到，在生成dex文件之前，我们可以获取到本项目和第三方库的class文件，那么我们是否可以在此处将我们编写的字节码插入呢？答案是肯定的，我们在谷歌官网上找到这么一个界面-[Transform Api](http://tools.android.com/tech-docs/new-build-system/transform-api)。

![](https://raw.githubusercontent.com/121880399/PictureManager/master/20200418112915.png)



​	网页上讲从Android Gralde插件1.5.0版本开始，添加了Transform API，来允许第三方插件在经过编译的class文件转换为dex文件之前对其进行操作。Gradle会按照以下顺序执行转换：JaCoCo->第三方插件->ProGuard。其中第三方插件的执行顺序与第三方插件添加顺序一致，并且第三方插件无法通过Api控制转换的执行顺序。

![](https://raw.githubusercontent.com/121880399/PictureManager/master/20200418152815.png)

​	有了Transform API +ASM我们就能够将我们自己编写的字节码插入到第三方框架的class文件中，从而在编译器完成插桩。

### 4.具体实现

​	现在我们已经决定了用ASM在编译期通过Transform API进行插桩。那么具体该怎么实现呢？我们回想一下我们需要实现的功能，我们要对图片进行监控，为了监控我们要获取图片的数据，得到数据后发现超标图片我们要给与提示。这意味着有两部分功能，一部分负责通过插桩获取数据，另外一部分负责显示超标数据。于是整个大图监控项目我们采用Gradle自定义插件+Android Library的形式。

![](https://raw.githubusercontent.com/121880399/PictureManager/master/1587195721(1).jpg)

1. largeimage-plugin:自定义Gradle插件，主要负责将我们编写的字节码插入到class文件。
2. largeimage:Andriod Library，主要负责将获取到的图片数据进行过滤，保存超标图片并且以弹窗或者列表的形式呈现给用户。

​    如何创建Gralde插件项目在这里就不多说了，网上有很多教程。网上的大多数教程会告诉你把插件项目名称改为buildSrc，这样做有很多好处，尤其是在代码编写阶段，可以采用以下这种形式进行测试

```groovy
apply plugin:org.zzy.largeimage.LargeImageMonitorPlugin
```

不需要每次编写完成以后发布到maven仓库，插件项目修改以后，会直接在使用模块体现出来。

​	在这里笔者自建了本地maven库，并且为了名称上的统一，并没有将插件项目的名称改为buildSrc，这两种形式都可以，大家可以根据自身的情况来使用。

#### 4.1 插件端

​	如果在编译期存在很多Transform那么肯定会对编译速度有一定的影响，那么有没有什么方式可以减少这种影响？有！并发+增量编译。

​	在这里推荐一个开源库[Hunter](https://github.com/Leaking/Hunter),它能够帮助你快速的开发插件，并且支持并发+增量编译，笔者在这里就使用了该开源库。

![](https://raw.githubusercontent.com/121880399/PictureManager/master/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20200418162100.png)

使用该开源库很简单，只需要在插件项目的build.gradle中引入依赖就行。

​	接下来为了创建我们的Transform并且将其注册到整个Transform队列中，我们需要创建一个类实现Plugin接口。

```java
public class LargeImageMonitorPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        List<String> taskNames = project.getGradle().getStartParameter().getTaskNames();
        //如果是Release版本，则不进行字节码替换
        for(String taskName : taskNames){
            if(taskName.contains("Release")){
                return;
            }
        }

        AppExtension appExtension = (AppExtension)project.getProperties().get("android");
        //创建自定义扩展
        project.getExtensions().create("largeImageMonitor",LargeImageExtension.class);
        project.afterEvaluate(new Action<Project>() {
            @Override
            public void execute(Project project) {
                LargeImageExtension extension = project.getExtensions().getByType(LargeImageExtension.class);
                Config.getInstance().init(extension);
            }
        });
        //将自定义Transform添加到编译流程中
        appExtension.registerTransform(new LargeImageTransform(project), Collections.EMPTY_LIST);
        //添加OkHttp
        appExtension.registerTransform(new OkHttpTransform(project),Collections.EMPTY_LIST);
        //添加UrlConnection
        appExtension.registerTransform(new UrlConnectionTransform(project),Collections.EMPTY_LIST);
    }
}
```

该类主要做了三件事：

1. 判断当前是否是Release变体，如果是的话就不进行字节码插桩。原因很简单，对超标图片的监控尽量在开发和测试阶段处理完，不要带到线上。
2. 获取自定义扩展，比如我需要增加一个插桩开关标识，来控制是否进行字节码增强。
3. 将自定义Transform进行注册。

在代码中可以看见，我们注册了三个自定义Transform，因为我们同时要对图片加载框架和网络请求库进行插桩。

1. LargeImageTransform:主要负责对Glide,Picasso,Fresco,ImageLoader进行字节码操作。
2. OkHttpTransform:主要负责对OkHttp进行字节码操作。
3. UrlConnectionTransform:主要负责对UrlConnection进行字节码操作。

##### 4.1.1 Hook图片加载库

​	由于使用了Hunter框架，使得我们编写Transform变得更加简单，不需要使用传统的方式编写Transform，我们主要来看关键代码。

```java
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
            return mv == null ? null : new ImageLoaderMethodAdapter(mv,access,methodName,desc);
        }
        return mv;
    }

}
```

从继承类的名字来看，这是一个类的访问者，我们项目和第三方库中的类都会经过这。

​	我们在visit方法中记录下当前经过的类的名字。并且在visitMethod方法中判断当前访问的是否是某个类的某个方法，如果当前访问的方法是我们需要hook的方法，那么我们就执行我们的字节码插桩操作。

​	那么问题来了，我们如何知道我们要hook哪个类的哪个方法呢？这就需要我们去阅读需要hook框架的源码了。在visitMethod方法中我们打算对Glide,Picasso,Fresco,ImageLoader四大图片加载框架进行hook。那么我们就先需要知道这四大框架的Hook点在哪。那么如何寻找Hook点呢？虽然滴滴的Dokit项目中已经给出了Hook点，但是抱着学习的态度，我们可以试图的分析一下，如何去寻找Hook点？

我们对图片加载框架进行Hook,必须要满足以下几点：

1.该Hook点是流程执行的必经之路。

2.在进行Hook以后，我们能获取到我们想要的数据。

3.进行Hook以后，不能影响正常的使用。

​	在经过对四大图片加载框架源码的大致分析以后，我发现大部分框架都在成功加载图片后会对接口进行回调，用来通知上层，图片加载成功。那么我们是否有可能把图片加载成功后回调的接口替换成我们的？或者增加一个我们自定义的接口进去，让图片加载成功以后也回调我们的接口，这样我们就能获取到图片的数据。

​	以Glide框架举例，Glide在成功加载完图片以后会在SingleRequest类的onResourceReady方法中对RequestListener接口进行遍历回调。

```java
private void onResourceReady(Resource<R> resource, R result, DataSource dataSource) {
 ...
  try {
    boolean anyListenerHandledUpdatingTarget = false;
    if (requestListeners != null) {
      for (RequestListener<R> listener : requestListeners) {
        anyListenerHandledUpdatingTarget |=
            listener.onResourceReady(result, model, target, dataSource, isFirstResource);
      }
    }
    anyListenerHandledUpdatingTarget |=
        targetListener != null
            && targetListener.onResourceReady(result, model, target, dataSource, isFirstResource);

    if (!anyListenerHandledUpdatingTarget) {
      Transition<? super R> animation = animationFactory.build(dataSource, isFirstResource);
      target.onResourceReady(result, animation);
    }
  } finally {
    isCallingCallbacks = false;
  }

  notifyLoadSuccess();
}
```

从这段代码中我们可以知道几点：

1. requestListeners是一个List。
2. 回调方法onResourceReady中有我们所需要的所有数据。

这样一来我们只需要在requestListeners中添加一个我们自定义的RequestListener。这样在接口回调时，我们也能获取到图片数据。那么在什么地方插入我们自定义的RequestListener呢？我们先来看requestListeners在SingleRequest中的定义。

```java
@Nullable private final List<RequestListener<R>> requestListeners;
```

requestListeners被声明成了final类型，那么在编写代码的时候就只能够赋值一次，如果是成员变量的话，则必须在构造方法中进行初始化。

```java
private SingleRequest(
    Context context,
    GlideContext glideContext,
    @NonNull Object requestLock,
    @Nullable Object model,
    Class<R> transcodeClass,
    BaseRequestOptions<?> requestOptions,
    int overrideWidth,
    int overrideHeight,
    Priority priority,
    Target<R> target,
    @Nullable RequestListener<R> targetListener,
    @Nullable List<RequestListener<R>> requestListeners,
    RequestCoordinator requestCoordinator,
    Engine engine,
    TransitionFactory<? super R> animationFactory,
    Executor callbackExecutor) {
  this.requestLock = requestLock;
  this.context = context;
  this.glideContext = glideContext;
  this.model = model;
  this.transcodeClass = transcodeClass;
  this.requestOptions = requestOptions;
  this.overrideWidth = overrideWidth;
  this.overrideHeight = overrideHeight;
  this.priority = priority;
  this.target = target;
  this.targetListener = targetListener;
  this.requestListeners = requestListeners;
  this.requestCoordinator = requestCoordinator;
  this.engine = engine;
  this.animationFactory = animationFactory;
  this.callbackExecutor = callbackExecutor;
  status = Status.PENDING;

  if (requestOrigin == null && glideContext.isLoggingRequestOriginsEnabled()) {
    requestOrigin = new RuntimeException("Glide request origin trace");
  }
}
```

如果我们在SingleRequest的构造方法中进行Hook,把我们自定义的RequestListener添加进requestListeners中，那么在图片成功加载时，就会回调我们的方法，从而获取到图片数据。这样我们就找到了对Glide框架的Hook点，也就有了visitMethod方法中下面这段代码：

```java
//对Glide4.11版本的SingleRequest类的构造方法进行字节码修改
if(className.equals("com/bumptech/glide/request/SingleRequest") && methodName.equals("<init>") && desc!=null){
    return mv == null ? null : new GlideMethodAdapter(mv,access,methodName,desc);
}
```

这段代码就是用于判断当前访问的是否是Glide框架中的SingleRequest类的构造方法？如果是的话就进行字节码插入。

​	现在我们已经有了Hook点，我们要把自定义的RequestListener添加到requestListeners中。那么现在有两种选择。

​	第一种，在SingleRequest类构造方法进入时，得到传入的参数requestListeners，将自定义RequestListener加入其中，接着再把参数requestListeners赋值给成员变量this.requestListeners。

​	第二种，让参数requestListeners先赋值给成员变量this.requestListeners，在方法退出之前拿到this.requestListeners，将我们自定义的RequestListener加入其中。

两种方法看似实现了相同的功能，但是字节码却不一样。

第一种方法的语句与字节码如下：

```java
//语句
GlideHook.process(requestListeners);
//字节码
mv.visitVarInsn(ALOAD, 12);
mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/aop/glide/GlideHook", "process", "(Ljava/util/List;)Ljava/util/List;", false);
```

第二种方法的语句与字节码如下：

```java
//语句
GlideHook.process(this.requestListeners);
//字节码
mv.visitVarInsn(ALOAD, 0);
mv.visitFieldInsn(GETFIELD, "com/bumptech/glide/request/SingleRequest", "requestListeners", "Ljava/util/List;");
mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/aop/glide/GlideHook", "process", "(Ljava/util/List;)Ljava/util/List;", false);
```

我们知道java在执行一个方法的同时会创建一个栈帧，栈帧中包括局部变量表，操作数栈，动态链接，方法出口等。其中局部变量表是在编译期就已经确定了，其索引是从0开始，表示该对象的实例引用，你可以大体认为就是this。

​	在第一种方法中，我们先是通过ALOAD指令将局部变量表中索引为12的引用型变量入栈（requestListeners），然后调用GlideHook的静态方法process,将其传入。

​	在第二种方法中，我们通过ALOAD指令将this入栈，然后访问this对象的requestListeners字段，将其传入GlideHook的静态方法process中。

从指令上来看，第一种方式的指令更少。但是我们考虑一个问题，第一种方式我们手动的获取了该方法局部变量表第12个索引的值。万一哪一天Glide想在该构造方法中增加或者删除一个参数，那我们的代码就不兼容了。所以为了代码的兼容性考虑，我们采用第二种方法，起码直接删除一个成员变量的概率要小于对构造方法入参的修改。

在这里大家可以思考一下，是否能直接在构造方法中add我们的自定义RequestListener？可以是可以，但是如果下次要再增加一个自定义RequestListener，我们又得在插件端修改字节码指令，太过于麻烦，我们不如直接得到List，然后在GlideHook的process方法中add。

我们来看看具体的实现代码：

```java
public class GlideMethodAdapter extends AdviceAdapter {

    /**
    * 方法退出时
     * 1.先拿到requestListeners
     * 2.然后对其进行修改
     * GlideHook.process(requestListeners);
    * 作者: ZhouZhengyi
    * 创建时间: 2020/4/1 15:51
    */
    @Override
    protected void onMethodExit(int opcode) {
        super.onMethodExit(opcode);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "com/bumptech/glide/request/SingleRequest", "requestListeners", "Ljava/util/List;");
        mv.visitMethodInsn(INVOKESTATIC, "org/zzy/lib/largeimage/aop/glide/GlideHook", "process", "(Ljava/util/List;)Ljava/util/List;", false);
    }
}
```

onMethodExit表示在SingleRequest构造方法退出前加入以下指令。这时候肯定有人会问了，字节码指令这么麻烦我写错了咋办？在这里推荐一款android studio插件ASM Bytecode Outline。安装成功以后，用Java将代码编写完成，然后右键生成字节码即可。例如我们可以创建一个测试类：

```java
public class Test {
    private List<RequestListener> requestListeners;
    //模拟glide
    private void init(){
        GlideHook.process(requestListeners);
    }
}
```

![](https://raw.githubusercontent.com/121880399/PictureManager/master/20200418222056.png)

![](https://raw.githubusercontent.com/121880399/PictureManager/master/20200418222224.png)

这样我们就能得到我们想要的字节码指令了，别忘了修改一下类的全限定名。该插件的详细操作网上有很多教程，这里就不多说了。

到此为止我们就成功将编写好的字节码插入到了Glide框架中。对其他三种图片加载框架的Hook点寻找也是类似的思路，而且大部分也都是在某个类的构造方法中进行Hook。这里提一下寻找Fresco Hook点的过程，本来按照以上寻找Hook点的思路，在Fresco中找到了一个接口，图片成功加载后也会回调该接口，但是郁闷的是回调该接口时，我们拿不到图片数据。最后是通过Hook Postprocessor拿到的Bitmap。具体的大家可以结合我github上的源码来分析。

总结一下：

​	1.寻找到的Hook点可能不止一个，大家根据自身情况进行采用。

​	2.拿到Hook对象以后，要看看是否能得到我们想要的数据，如果得不到需要重新寻找。

​	3.构造方法是一个好的Hook点，因为在这里一般都进行初始化操作。

​	4.在选择Hook方式的时候一定要考虑到代码兼容性问题。

在插入完字节码以后，当Glide执行到SingleRequest的构造方法时就会执行我们插入的字节码指令了。在图片成功加载后就会回调我们的自定义RequestListener，接着该怎么做，我们后面再说，这部分的逻辑我们将它放到了largeimage 这个Library中。

##### 4.1.2 Hook OkHttp

​	我们前面说到，当我们使用图片框架加载一张网络图片时，图片框架会先从网络将图片下载，然后再加载。以Glide为例，Glide会将图片下载存到本地，然后再把本地图片读入内存构建一个Resource,当图片加载成功的时候，就会回调我们自定义的监听器，但是这个时候我们只能获取到图片加载到内存后的数据，也就是说我们获取不到图片的文件大小。所以就考虑是否能再图片下载成功后拿到图片的文件大小呢？这就需要我们对网络下载框架进行Hook，每次得到Response时判断Content-Type是否是image开头，如果是的话我们就认为本次请求的是图片。

​	有了思路以后，我们就开始着手对OkHttp进行Hook,OkHttp的Hook点很容易寻找，一方面在于大家对OkHttp的源码都比较熟悉，另外一方面在于OkHttp的优秀架构。我们都知道OkHttp采用拦截链的方式来处理数据，并且作者预留了两处可以添加拦截器的地方，一处是应用拦截器，一处是网络拦截器。只要我们在这两处添加我们自己的拦截器，那么请求和响应数据都会经过我们的拦截器。所以OkHttp的Hook点我们就放在OkHttpClient$Builder类的构造方法中。

```java
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
            return methodVisitor == null ? null : new 		OkHttpMethodAdapter(methodVisitor,access,name,desc);
        }
        return methodVisitor;
    }
}
```

而且这种拦截器的添加是全局性的，以前你在项目中添加OkHttp的拦截器，只是你本项目的网络请求会回调。但是通过这种方法添加的拦截器，本项目中和第三方库中，只要使用了OkHttp框架都会添加相同的拦截器。说到这是不是想到了HttpDns？以前我们为了防止DNS劫持加快DNS解析速度，在OkHttp中通过自定义DNS的方式来实现HttpDns访问，但是如果使用第三方图片框架加载服务器上的图片，还是走的53端口的UDP形式。那么我们能不能顺便把OkHttp中的Dns也Hook了？这样就能全局添加我们自定义的Dns，实现整个项目都使用HttpDns来解析域名。

```java
public class OkHttpMethodAdapter extends AdviceAdapter {

   
    /**
     * 方法退出时插入
     * interceptors.addAll(LargeImage.getInstance().getOkHttpInterceptors());
     * networkInterceptors.
     * addAll(LargeImage.getInstance().getOkHttpNetworkInterceptors());
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
```

我们在OkHttpClient$Builder构造方法退出之前，将我们的拦截器和自定义dns插入。

同样的，插件端只负责插入字节码，后续所有的逻辑都放在了Library中。

##### 4.1.3 Hook HttpUrlConnection

​	可能很多人会觉得，现在还有人用HttpUrlConnection吗？还有必要对它进行处理吗？虽然现在普遍使用OkHttp框架，但使用HttpUrlConnection的还很多，而且还得考虑兼容性不是吗？像Glide框架使用的就是HttpUrlConnection请求网络，虽然Glide框架可以采用自定义ModelLoader的方式实现OkHttp请求网络。但是为了保险起见，我们统一进行处理。那这里要怎么对HttpUrlConnection进行Hook呢？HttpUrlConnection的源码也没看过呀？那我们能不能换一种思路，既然在前面我们已经对OkHttp进行了Hook，那么我们能不能将所有的HttpUrlConnection请求换成OkHttp来实现？也就是将HttpUrlConnection请求导向OkHttp，这样就可以在统一在OkHttp中对数据进行处理。

​	那怎么才能将HttpUrlConnection换成OkHttp呢？我们以前在做Hook的时候，通常的思路是，如果Hook的对象是接口，那么我们就使用动态代理，如果是类，那么我们就继承它并且重写其方法。在这里我们也可以自定义一个类继承HttpUrlConnection然后重写它的方法，方法里全部改用OkHttp来实现。那接下来的问题就是在什么地方将系统的HttpUrlConnection换成我们自定义的HttpUrlConnection。HttpUrlConnection是一个抽象类，不能直接用new来创建，要得到HttpUrlConnection对象，需要使用URL类的openConnection方法得到一个HttpURLConnection对象，那么我们就可以在所有调用openConnection方法的地方进行Hook，将系统返回的HttpURLConnection对象替换成我们自定义的HttpURLConnection对象。

​	既然所有调用到openConnection方法的地方都要Hook，那么就没用特定的类，所以这次我们不针对特定类。

```java
public class UrlConnectionClassAdapter extends ClassVisitor {

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
```

​	URL类有两个openConnection方法，都要进行Hook。

```java
public class UrlConnectionMethodAdapter extends AdviceAdapter {

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
```

这样我们就成功把由OkHttp实现的HttpURLConnection返回给使用者。

HttpUrlConnection字节码插桩部分到这里就结束了，剩下的逻辑也都在Library中。

#### 4.2 Library端

Library端主要完成这么几件事：

1.负责初始化并接收用户的配置。

2.从框架的回调中得到所需的数据。

3.对超标的图片数据进行保存。

4.对超标的图片进行展示。

##### 4.2.1 初始化与配置

​	LargeImage类负责初始化和接收用户的配置，是用户直接操作的类，该类被设置成了单例，并且采用链式调用的方式接收用户的配置。通过该类可以设置图片的文件大小阈值，图片所占内存大小的阈值，OkHttp应用拦截器的添加，OkHttp网络拦截器的添加等配置。

```java
 LargeImage.getInstance()
        .install(this)//一定要调用该方法进行初始化
        .setFileSizeThreshold(400.0)//设置文件大小阈值单位为KB (可选)
        .setMemorySizeThreshold(100)//设置内存占用大小阈值单位为KB (可选)
        .setLargeImageOpen(true)//是否开启大图监控，默认为开启，如果false，则不会在大图列表和弹窗显示超标图片 (可选)
        .addOkHttpInterceptor(new CustomGlobalInterceptor())//添加OKhttp自定义全局应用监听器 (可选)
        .addOkHttpNetworkInterceptor(new CustomGlobalNetworkInterceptor())//添加Okhttp值得你故意全局网络监听器 (可选)
        .setDns(new CustomHttpDns);//设置自定义的全局DNS，可以自己实现HttpDns (可选)
```

##### 4.2.2 获取数据

​	当我们在插件端将字节码插入到框架以后，框架会自动回调我们自定义的方法，在这些方法中就可以获取到图片的数据，所以关于这一块没什么好说的，都比较简单，无非就是获取到数据以后调用相关类的方法保存数据，并不做过多的业务处理。这里值得一说的是，在HttpUrlConnection进行Hook时，我们提到要自定义HttpUrlConnection并且使用OkHttp来实现，这部分的实现不用我们自己来完成，在OkHttp3.14版本之前有提供一个叫ObsoleteUrlFactory的类，已经帮我们实现好了，只是从3.14版本以后该类被去掉了，我们只需要把这个类拷贝过来直接使用就行。

##### 4.2.3 保存数据

​	获取到图片数据以后，我们就要进行保存，这部分的逻辑由LargeImageManager负责，LargeImageManager类也被设计成了单例。既然是要对数据进行保存，那么我们肯定是有选择性的保存，也就是只保存超标的图片信息，没有超标的图片，我们就不管了。而保存的超标信息是为了向用户进行报警。

​	在实现该类的时候遇到了这么几个问题，首先由于我们分别Hook了OkHttp和图片框架，所以在加载一张网络图片的时候，我们会先收到OkHttp的回调，在这里我们可以得到图片的文件大小信息，然后再收到图片框架的回调，得到图片所占用的内存大小信息。我们前面提到我们需要保存超标的图片信息，而对超标图片的定义是文件大小超标或者内存占用超标，所以我们在OkHttp回调的时候是没办法知道内存是否超标的，因为图片框架有可能会对图片进行压缩，那么我们在OkHttp回调时就不用判断当前图片是否保存，而是一律保存下来，将是否保存的判断延迟到图片框架回调时。在图片框架回调时，我们就能同时拥有文件大小和内存占用的数据，如果其中之一超标我们则保存，如果都不超标，我们再将数据删除。

​	其次我们还遇到了这样一个问题，当我使用Glide框架加载一张网络图片时，我们假设这张图片文件大小超标，但是内存不超标，那么我们会记录该图片的所有信息。但是在第二次启动APP时，由于Glide在磁盘中缓存了该图片，就不会再次调用OkHttp去下载图片，那么这时候我们只能收到图片框架的回调，换句话说我们只能得到图片所占用内存的数据，如果这时候图片内存不超标，那么我们就会删除此图片的信息，也就不会提示用户。为了解决这个问题，我们就必须在SD卡中保存超标图片的完整信息，这样就算图片框架从缓存中加载图片，我们也能得到图片的文件大小信息。

​	我们应该如何将超标图片的信息保存到本地呢？用SharedPreferences?还是数据库？因为使用场景会频繁的增加，删除和修改数据，而SP每次都是全量写入，也就是说SP在每次写入数据之前都会把xml文件改名为备份文件，然后再从xml文件中读取出数据与新增数据合并再写入到新的xml文件中，如果执行成功再将备份xml文件删除，这样效率太低了。至于数据库的效率跟SP也差不了太多，而且还要防止突然间奔溃导致数据没保存上的情况。这就要求使用的组件具有实时写入的能力，那么mmap内存映射文件正好适合这种场景，通过mmap内存映射文件，能够提供一段可供随时写入的内存块，APP只管往里面写数据，由操作系统负责将内存回写到文件，而不必担心crash导致数据丢失。由微信开源的[MMKV](https://github.com/Tencent/MMKV)就是基于mmap内存映射的key-value组件，它十分的高效，具有增量更新的能力。下面是微信团队对MMKV，SP，SQlite的对比测试数据。

![img](https://github.com/Tencent/MMKV/wiki/assets/profile_android.png)

单进程情况下，在华为 Mate 20 Pro 128G，Android 10手机上，每组操作重复 1k 次，结果以ms为单位，可以看见MMKV的效率很高。

​	使用了MMKV，就解决了图片框架从缓存加载数据时，得不到图片文件大小的问题。但是另外一个问题出现了，使用MMKV以后，我们将超标的图片数据都保存到了本地，如果超标图片之后一直未使用，那么我们就要一直保存着吗？也就是说我们何时清理MMKV保存的数据？使用LRU算法？也许可行，但是我这里使用了一个稍微简单一点的实现方式，首先我们设置一个清理值，达到该值就开始执行清理操作，这里我将默认值设置成了20，当然这个值是可以通过我们提供的接口进行修改的。在超标图片bean类中也增加一个记录当前图片未使用次数的字段。然后程序每次启动时会对当前启动次数加1，并且对MMKV中保存的超标图片未使用次数加1，如果图片被加载一次，超标图片中的未使用次数就重置为0。当启动次数达到清理值，那么我们就遍历MMKV，将未使用次数到20的图片信息进行删除，再重置当前启动次数。

##### 4.2.4 超标图片显示

​	对于超标图片显示，这里采取了两种查看方式，一种是通过弹窗提示，另外一种是通过列表展示。

![img](https://github.com/121880399/LargeImageMonitor/raw/master/wiki/example4.jpg)

![img](https://github.com/121880399/LargeImageMonitor/raw/master/wiki/example2.jpg)

这里没什么好说的，主要注意一下悬浮窗权限的问题。

​	在实现列表展示的时候，我纠结过列表中的数据是展示所有的超标图片呢？还是本次启动加载到的超标图片？最后决定还是展示本次加载到的超标图片，主要有这么几点考虑，首先如果加载所有超标图片，那么势必要从本地读取超标图片的数据，如果数据很多的话，列表就会很长，如果用户只是想看当前页面超标的图片信息，那么查找会很不方便。其次如果要加载历史的超标图片信息，涉及到一个问题，加载超标图片信息就要加载超标图片的略缩图，那么问题来了，我们Hook了四大图片加载框架，如果我们在加载略缩图时采用了这四大图片框架，那么就会再次收到图片信息，由于加载的是略缩图，所以图片框架肯定会对图片进行压缩，那么就会更新超标图片的信息，这样就会导致由于加载了一张超标图片的略缩图导致超标图片信息被更新为未超标，从而被删除。这是我们不希望看见的，而只加载本次遇见的超标图片，我们可以将本次超标的图片缓存在内存中，在列表展示的时候直接显示缓存的Bitmap对象，这样我们就不需要使用图片加载框架，也就不存在这个问题。

### 5.写在最后

​	到此大图监控的原理就讲解的差不多了，大家可以到我的[Github](https://github.com/121880399/LargeImageMonitor)上结合源码进行分析，如果觉得对您有用，可以给我点一个Star，该项目后续也会继续的进行迭代。在这里要感谢滴滴开源的[Dokit](https://github.com/didi/DoraemonKit)框架以及[Hunter](https://github.com/Leaking/Hunter)开源库。最后大家也可以看看字节跳动开源的[ByteX](https://github.com/bytedance/ByteX)库，该库是一个字节码插件开发平台，集成了很多有用的插件，更多详情可以查看ByteX的文档。