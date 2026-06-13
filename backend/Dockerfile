# 构建阶段
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 网络不稳定时自动重试，避免传输中断（Premature end of Content-Length）导致构建失败
ENV MAVEN_OPTS="-Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 -Dmaven.wagon.http.pool=false -Dmaven.wagon.rto=120000"

# 复制 Maven 配置和 pom.xml
COPY settings.xml .
COPY pom.xml .

# 预下载依赖（利用 Docker 缓存层），失败可在下一步 package 时补齐
RUN mvn -s settings.xml -B dependency:go-offline || true

# 复制源码并打包
COPY src ./src
RUN mvn -s settings.xml -B package -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre
WORKDIR /app

# 复制打包好的 JAR 文件
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8000

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
