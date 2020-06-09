## Gradle

Gradle 打包指定输入位置方式：
- 默认libs自定义目录（输出目录最内层为libs目录）
// 修改打包位置
//buildDir = "$rootDir/build/"
- 自定义输出目录（不推荐使用）
tasks.withType(Jar) {
    destinationDir = file("$rootDir/build/libs")
}