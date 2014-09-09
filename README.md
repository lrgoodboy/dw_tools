DW Tools
========

## 开发环境

* 配置Maven镜像（可选）：

```xml
<!-- $HOME/.m2/settings.xml -->
<settings>
  <mirrors>
    <mirror>
      <url>http://10.20.8.31:8081/nexus/content/groups/public/</url>
      <mirrorOf>central</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```

* 确保已连接内网。由于该项目用到的[Markdown渲染库][1]在Maven中央仓库中没有，所以保存在了公司内网的镜像上。你也可以自行下载安装到本地仓库中。
* 获取代码

```bash
$ git clone git@gitlab.corp.anjuke.com:_bi/dw_tools
```

* 导入Eclipse（Maven项目）
* 打开`Appliction.java`, 点击 **调试** 按钮，即可浏览 http://127.0.0.1:8913/tools

[1]: https://code.google.com/p/markdown4j/

