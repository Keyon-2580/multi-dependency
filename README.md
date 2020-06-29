## 项目介绍

支持对单体和微服务系统的静态结构分析、动态运行分析、Git历史分析、三方依赖分析和克隆分析。

## 环境准备

- java 1.8+
- neo4j 3.5.3+

## 运行步骤

目前本地进行测试使用的开源项目主要为单体应用[depends](https://github.com/multilang-depends/depends)和微服务系统[train-ticket](https://github.com/FudanSELab/train-ticket)。由于动态运行信息、三方依赖和克隆信息需要运行额外的工具才能获取到，所以我们将这两个项目的相关依赖数据源作为文件保存在`resources`目录下。

因此若要得到五项依赖数据叠加的结果，暂时仅支持我们测试使用的这两个开源项目。

下面以分析train-ticket项目为例。

**1. 将[train-ticket](https://github.com/FudanSELab/train-ticket)的代码克隆到本地。**

**2.  在`src/main/resources/project`中，复制一份`train-ticket.json`，并改名，如：`train-ticket-zhou.json`，并将其中projects和gits下的项目路径批量改为本地train-ticket项目路径。**

train-ticket.json中有六个数据项：projects、architectures、dynamics、gits、libs、clones，分别对应着每项分析的配置。

- projects：每一条对应着一个项目。由于train-ticket为微服务项目，因此有多条project项。
- architectures：自定义的微服务项目各服务间的依赖关系。
- dynamics：动态分析配置，读取resources/dynamic和resources/features的信息。
- gits：git分析配置，读取本地train-ticket目录下的.git文件夹和resources/git信息。多个git库需要写多项，每一项对应着一个git库。
- libs：第三方库分析配置，读取resources/lib信息。
- clones：克隆分析配置，粒度分为file级别和function级别，读取resources/clone信息。

**3. 在`src/main/resources`中，复制一份`application-example.yml`，并改名，如：`application-zhou.yml`，然后修改里面的配置。**

- `spring.data.neo4j`：`username`和`password`改为本地neo4j数据库所设置的账户名和密码。

- `data.project_config`：静态分析配置，值为步骤2中所准备的`src/main/resources/project/train-ticket-zhou.json`。

- `data.dynamic_analyse`：true/false，是否进行动态分析。

- `data.git_analyse`：true/false，是否进行git库分析。

- `data.lib_analyse`：true/false，是否进行第三方库分析。

- `data.clone_analyse`：true/false，是否进行克隆分析。

  其中静态分析为最基础的数据，其他依赖信息都是叠加在静态上的。因此`dynamic`/`git`/`lib`/`clone`分析均为可选项，若不分析就将对应的项设置为`false`。

- `data.neo4j`：`delete`为`true`表示会对指定的数据库先做删除操作，再插入数据；`path`为项目运行时，所生成的数据库的存放路径。

- `serialize_path`：执行`InsertStaticData.java`后生成的静态数据的存放路径。

**4. 修改`application.yml`，将`active`的值改为对应的名字，如`active: zhou`。**

**5. 关闭neo4j数据库，执行插入操作。**

有两种方式。

方式1：运行`src/main/java/cn.edu.fudan.se.multidependency/InsertAllData.java`。

方式2：运行同目录下的`InsertStaticData.java`，执行静态分析，将结果序列化到文件；运行结束后，再运行`InsertOtherData.java`读取static序列化的文件，然后执行其他分析（克隆、动态、三方、git等）。

插入数据完成后，可开启neo4j数据库，使用浏览器打开网址[http://localhost:7474](http://localhost:7474)，检查数据是否插入成功。

**6. 打开neo4j数据库，运行`src/main/java/cn.edu.fudan.se.multidependency/MultipleDependencyApp`。**

springboot启动成功后，使用浏览器打开网址[http://127.0.0.1:8080](http://127.0.0.1:8080)，查看视图。