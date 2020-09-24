Java IO分为字节流和字符流两大类
- 字节流
    - InputStream
        - FileInputStream（重要）
        - StringBufferInputStream
        - ByteArrayInputStream
        - PipedInputStream
    - OutputStream
        - PipedOutputStream
        - FileOutputStream
- 字符流
    - Reader
        - InputStreamReader(重要)
        - FileReader
        - BufferedReader
        - CharArrayReader
        - StringReader
    - Writer
        - OutputStreamWriter
        - FileWriter
        - BufferedWriter

## Java有几种类型的流

字节流和字符流，字节流继承InputStream和OutPutStream，字符流继承InputStreamReader和OutputStreamWriter

注：面试题 https://zhuanlan.zhihu.com/p/35066927

## 字节流和字符流更喜欢那一个？

更喜欢字符流，字符流有一些特性是字节流不存在;比如使用BufferedReader可以按行迭代直接读取一行；

## 什么是Filter流

Filter流是一种Java IO流，是对存在的流增加额外的功能,属于字节流；比如LineNumberInputStream给源文件增加行数，BufferedInputStream基于缓冲区提高性能等；

## 有哪些可用的Filter流

Java IO主要有四个，两个字节流和两个字符流，FilterInputStream，FilterOutputStream,FilterReader,FilterWriter;

Filter类的一些子类：
- LineNumberInputStream可以给原文件增加行号
- BufferedInputStream基于缓冲区提高读取效率
- SequenceInputStream拷贝多个文件到一个文件的时候可以用很少的代码实现

注：基于缓冲区的字节流和字符流能够提高数据存取性能；