# JavaClassScoreSystem v2

一个基于 **Java + SQLite** 的学生成绩管理系统（控制台版）。相比初版，本版本重构为分层架构（模型 / 数据访问 / 业务），引入 DAO 与 Service 层、班级/课程维度统计、GPA 计算、排名与 CSV 导出，并配套 Maven 构建与单元测试。

## 功能特性

- **学生管理**：新增、查询、按学号检索、删除学生
- **课程管理**：课程与学分维护
- **成绩录入**：为某学生在某课程录入/更新成绩
- **统计分析**：班级平均分、课程平均分、最高/最低分
- **排名**：按总绩点（GPA）对学生排名
- **导出**：将成绩明细导出为 CSV
- **持久化**：使用 SQLite 本地文件数据库，零外部服务依赖

## 技术栈

| 维度 | 选型 |
|---|---|
| 语言 | Java 17 |
| 构建 | Maven |
| 数据库 | SQLite（JDBC：`org.xerial:sqlite-jdbc`） |
| 设计 | 分层架构：Model / DAO / Service |

## 快速开始

```bash
# 1. 克隆
git clone https://github.com/swdscsc/JavaClassScoreSystem-v2.git
cd JavaClassScoreSystem-v2

# 2. 编译
mvn compile

# 3. 运行（自动初始化 SQLite 数据库 score.db）
mvn exec:java -Dexec.mainClass=com.swd.score.Main
```

> 首次运行会在项目根目录生成 `score.db`，所有数据持久化其中。

## 项目结构

```
src/main/java/com/swd/score/
├── Main.java                # 控制台交互入口
├── model/                  # 数据模型
│   ├── Student.java
│   ├── Course.java
│   └── Score.java
├── dao/                    # 数据访问层
│   ├── Database.java       # 连接与建表
│   ├── StudentDao.java
│   └── ScoreDao.java
└── service/                # 业务逻辑层
    └── ScoreService.java
src/main/resources/schema.sql
src/test/java/com/swd/score/ScoreServiceTest.java
```

## 设计亮点（面试可讲）

1. **分层解耦**：UI（Main）只调用 Service，Service 编排 DAO，DAO 只关心 SQL 与连接。便于替换为 Web 前端或加缓存。
2. **SQLite 零部署**：用文件型数据库，免去安装 MySQL 的麻烦，适合课程 Demo 与单机工具。
3. **GPA 计算可配置**：学分加权绩点，权重规则集中在 `ScoreService`，易于调整算法。
4. **可测试**：核心统计逻辑用纯函数实现，配套 JUnit 单测验证排名与平均分正确性。

## 后续可扩展

- 接入 JavaFX / Swing 图形界面
- 增加用户登录与权限（教师 / 学生视角）
- 导出 Excel（Apache POI）或对接 Web API
- 替换为 MySQL 以支持多端并发

## License

[MIT](LICENSE)
