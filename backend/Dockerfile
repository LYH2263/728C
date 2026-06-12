# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 复制 Maven 配置和 pom.xml
COPY settings.xml .
COPY pom.xml .

# 预下载依赖（利用 Docker 缓存层）
RUN mvn -s settings.xml dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn -s settings.xml package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app

# 复制打包好的 JAR 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8000

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
