# ClientBookPC

ClientBookPC 是 `CodeBookChat` 的PC端桌面版本，可以在不利用或没有浏览的情况下使用 `CodeBookChat` 聊天服务器。

## 快速开始

本章节将让您快速的用上软件！

### 下载软件包

请您 [点击这里下载](https://github.com/BeardedManZhao/ClientBookPC/releases/download/1.0/ClientBook-PC.zip) Windows和Linux的通用版本！

### 解压并运行

解压操作在这里就不演示了，有很多软件都可以解压，因为每个电脑的软件都不一样，我无法知晓您使用的是哪个解压软件，因此这里省略

解压后会出现文件目录，进入找到 `start.bat` 或者 `start.sh` 文件并双击就可以了！

注意：第一次启动需要几秒的时间初始化，后面就好啦！

## 编译方法 （可选，不需要可以不阅读）

项目中存在目录 `ClientBook-PC` 对应的就是系统的初始目录。

在项目中我们直接利用 maven 打包之后会出现 `lib` 和 `ClientBookPC-xxx.jar` 如下所示

```
PS D:\MyGithub\ClientBookPC\target> dir


    目录: D:\MyGithub\ClientBookPC\target


Mode                 LastWriteTime         Length Name                                                                                                                                                                             
----                 -------------         ------ ----                                                                                                                                                                             
d-----         2026/3/21     13:23                classes
d-----         2026/3/21     13:23                generated-sources
d-----         2026/3/21     13:23                generated-test-sources
d-----         2026/3/21     13:23                lib
d-----         2026/3/21     13:23                maven-archiver
d-----         2026/3/21     13:23                maven-status                                                                                                                                                                     
d-----         2026/3/21     13:23                test-classes
-a----         2026/3/21     13:23          30588 ClientBookPC-1.0-SNAPSHOT.jar

```

我们需要把这两个复制一下，放到初始目录即可

```
 D:\MyGithub\ClientBookPC\ClientBook-PC 的目录

2026/03/21  13:32    <DIR>          .
2026/03/21  13:32    <DIR>          ..
2026/03/21  13:23            30,588 ClientBookPC-1.0-SNAPSHOT.jar
2026/03/21  10:23    <DIR>          jre-linux
2026/03/21  10:20    <DIR>          jre-win
2026/03/21  13:32    <DIR>          lib
2026/03/21  13:31                53 start.bat
2026/03/21  13:30                57 start.sh
               3 个文件         30,698 字节
               5 个目录 226,313,056,256 可用字节
```