DW Tools
========

## Development Environment

* Setup Maven mirror: (optional)

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

* Checkout code:

```bash
$ git clone git@gitlab.corp.anjuke.com:_bi/dw_tools
```

* Import into Eclipse as Maven project.
* Open `Appliction.java`, click *Debug Application* button, and browse `http://127.0.0.1:8082/tools`.
