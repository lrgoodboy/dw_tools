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

* 获取代码

```bash
$ git clone git@gitlab.corp.anjuke.com:_bi/dw_tools
```

* 导入Eclipse（Maven项目）
* 打开`Appliction.java`, 点击 **调试** 按钮，即可浏览 http://127.0.0.1:8913/tools
