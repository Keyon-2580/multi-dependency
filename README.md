## 项目介绍

支持对单体和微服务系统的静态结构分析、动态运行分析、Git历史分析、三方依赖分析和克隆分析。

## 环境准备

- java 1.8+
- neo4j 3.5.3+

## 运行步骤

我们目前进行测试使用的开源项目为单体应用[depends](https://github.com/multilang-depends/depends)和微服务系统[train-ticket](https://github.com/FudanSELab/train-ticket)。由于动态运行、三方依赖和克隆分析需要额外的工具运行，所以我们将这两个项目的相关依赖数据源作为文件保存在`resources`目录下。

因此若要得到五项依赖数据叠加的结果，暂时仅支持我们测试使用的这两个开源项目。即需要在运行前，将depends或train-ticket的代码克隆到本地。

1，在`src/main/resources`中，复制一份`application-example.yml`，并改名，如：`application-zhou.yml`，然后修改里面的配置。

下面以分析train-ticket项目为例。

- `spring.data.neo4j`：`username`和`password`改为本地neo4j数据库所设置的账户名和密码。

- `data.projects`：静态分析配置，将`src/main/resources/project/train-ticket.json`中的所有`projects.path`批量替换为本地train-ticket项目地址。

- `data.dynamic`：动态分析配置。

- `data.git`：git库分析配置，将`directory_root_path`设置为本地train-ticket项目地址（目录下需包含`.git`文件夹），`select_range`为`false`，表示对所有的commit记录进行分析，为`true`则只分析`commit_id_from`（较早）和`commit_id_to`之间的commit记录。

- `data.lib`：三方库分析配置。

- `data.clone`：克隆分析配置。

- `data.build`：构建分析配置（暂不支持）。

  其中静态分析为最基础数据，其他依赖信息都是叠加在静态上的。因此`dynamic`/`git`/`lib`/`clone`分析均为可选项，若不分析就将对应的`analyze`设置为`false`。

- `data.neo4j`：`delete`为`true`表示会对指定的数据库先做删除操作，再插入数据；`path`为本地数据库路径。

2，修改`application.yml`，将`active`的值改为对应的名字，如`active: zhou`。

3，关闭neo4j数据库，运行`src/main/java/cn.edu.fudan.se.multidependency/InsertDataMain`。

插入数据完成后，可开启neo4j数据库，使用浏览器打开网址http://localhost:7474/，检查数据是否插入成功。

4，打开neo4j数据库，运行`src/main/java/cn.edu.fudan.se.multidependency/MultipleDependencyApp`，springboot启动成功后，使用浏览器打开网址http://127.0.0.1:8080/，查看视图。

