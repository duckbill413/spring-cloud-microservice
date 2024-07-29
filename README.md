# spring-cloud-microservice
[인프런] [Spring Cloud로 개발하는 마이크로서비스 애플리케이션(MSA) 강의 실습](https://www.inflearn.com/course/%EC%8A%A4%ED%94%84%EB%A7%81-%ED%81%B4%EB%9D%BC%EC%9A%B0%EB%93%9C-%EB%A7%88%EC%9D%B4%ED%81%AC%EB%A1%9C%EC%84%9C%EB%B9%84%EC%8A%A4/dashboard)  
Dowon Lee  

### 버전
- Spring boot 3.3
- OS: Window

## 강의 커리큘럼
- 섹션 0. Microservice와 Spring Cloud의 소개
- 섹션 1. Service Discovery
- 섹션 2. API Gateway Service
- 섹션 3. E-commerce 애플리케이션
- 섹션 4. Users Microservice ➀
- 섹션 5. Catalogs and Orders Microservice
- 섹션 6. Users Microservice ➁
- 섹션 7. Configuration Service
- 섹션 8. Spring Cloud Bus
- 섹션 9. 설정 정보의 암호화 처리
- 섹션 10. Microservice 간 통신
- 섹션 11. 데이터 동기화를 위한 Apache Kafka의 활용 ①
- 섹션 12. 데이터 동기화를 위한 Apache Kafka의 활용 ②
- 섹션 13. 장애 처리와 Microservice 분산 추적
- 섹션 14. Microservice 모니터링
- 섹션 15. 애플리케이션 배포를 위한 컨테이너 가상화
- 섹션 16. 애플리케이션 배포 - Docker Container

## Branch
- master  
  Application 의 도커 설정까지
- local  
  local 에서 실행 (Docker X)

## Docker Deploy
### Docker
- docker network 생성
    
    ```bash
    docker network create --gateway <IP> --subnet <IP>  <network-name>
    
    ex)
    docker network create --gateway 172.18.0.1 --subnet 172.18.0.0/16 ecommerce-network
    ```
    
- docker inspect network (네트워크 상세정보 조회)
    
    ```bash
    docker network inspect <network-name>
    
    ex)
    docker network inspect ecommerce-network
    ```
    ![Untitled](https://github.com/user-attachments/assets/fdd77bed-3617-491f-bd7d-870296c5ef5b)

- docker log 확인
    
    ```bash
    docker logs <container-name>
    ```
    
- docker 사용되지 않는 container 정리
    
    ```bash
    docker system prune
    ```
    
- docker 로 실행된 database 접속
    
    ```bash
    docker exec -it <database-container-name> mysql -u root -p <database-name>
    
    ex)
    docker exec -it my_mysql_container mysql -u root -p
    
    docker exec -it my_mysql_container mysql -u root -p my_db
    ```
### Docker Image Build & Run
- RabbitMQ Docker 실행
    
    ```bash
    docker run -d --name rabbitmq --network ecommerce-network `
    	-p 15672:15672 -p 5672:5672 -p 15671:15671 -p 5671:5671 -p 4369:4369 `
    	-e RABBITMQ_DEFAULT_USER=guest `
    	-e RABBITMQ_DEFAULT_PASS=guest rabbitmq:management
    ```
    
    - macOS
        
        ```bash
        docker run -d --name rabbitmq --network ecommerce-network \
        	-p 15672:15672 -p 5672:5672 -p 15671:15671 -p 5671:5671 -p 4369:4369 \
        	-e RABBITMQ_DEFAULT_USER=guest \
        	-e RABBITMQ_DEFAULT_PASS=guest rabbitmq:management
        ```
        
- Config Server Docker image
    - Docker Image Build
        
        ```bash
        docker build -t duckbill413/config-service:1.0 .
        ```
        
    - Docker Run
        
        ```bash
        docker run -d -p 8888:8888 --network ecommerce-network `
        	-e "spring.rabbitmq.host=rabbitmq" `
        	-e "spring.profiles.active=default" `
        	--name config-service duckbill413/config-service:1.0
        ```
        
- Eureka Server
    - Docker Image Build
        
        ```bash
        docker build -t duckbill413/eureka-server:1.0 .
        ```
        
    - Docker Run
        
        ```bash
        docker run -d -p 8761:8761 --network ecommerce-network `
        	-e "spring.cloud.config.uri=http://config-service:8888" `
        	-e "spring.profiles.active=default" `
        	--name eureka-server `
        	duckbill413/eureka-server:1.0
        ```
        
- Gateway Service
    - Docker Image Build
        
        ```bash
        docker build -t duckbill413/gateway-service:1.0 .
        ```
        
    - Docker Run
        
        ```bash
        docker run -d -p 8000:8000 --network ecommerce-network `
        	-e "spring.cloud.config.uri=http://config-service:8888" `
        	-e "spring.rabbitmq.host=rabbitmq" `
        	-e "eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka" `
        	--name gateway-service `
        	duckbill413/gateway-service:1.0
        ```
        
- Build, Run MySQL
    - `Dockerfile` 생성
        
        ```docker
        # Use the official MySQL image from the Docker Hub
        FROM mysql:latest
        
        ENV MYSQL_ROOT_PASSWORD 1234
        # ENV MYSQL_DATABASE=my_db
        
        # initialize the database
        COPY ./init.sql /docker-entrypoint-initdb.d/
        
        # Expose the MySQL port
        EXPOSE 3306
        
        # Run the MySQL server
        CMD ["mysqld"]
        ```
        
        - `username`: root
        - `password`: 1234
        - Window MySQL 설정 파일 경로
            
            ```sql
            mysql --help | findstr "Default options"
            ```
            
            현재) `C:\ProgramData\MySQL\MySQL Server 8.3\Data`
            
            위의 디렉터리내 파일을 복사
            
    - Docker Image Build
        
        ```bash
        docker build -t duckbill413/my_mysql:1.0 .
        ```
        
    - Docker Run
        
        ```sql
        docker run -d -p 3306:3306 --network ecommerce-network `
        	--name mysqldb duckbill413/my_mysql:1.0
        ```
        
    - 접속 권한 설정
        
        ```sql
        GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
        FLUSH PRIVILEGES;
        ```
        
        - 모든 계정의 모든 ip 주소에 대해서 접속 권한 허용
- Zookeeper + Kafka Standalone
    - docker-compose 로 실행
    - https://github.com/wurstmeister/kafka-docker
    - `docker-compose-single-broker.yml` 수정
        
        ```yaml
        version: "2"
        services:
          zookeeper:
            image: wurstmeister/zookeeper
            ports:
              - "2181:2181"
            networks:
              my-network:
        
          kafka:
            # build: .
            image: wurstmeister/kafka
            ports:
              - "9092:9092"
            environment:
              KAFKA_ADVERTISED_HOST_NAME: 172.18.0.101
              KAFKA_CREATE_TOPICS: "test:1:1"
              KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
            volumes:
              - /var/run/docker.sock:/var/run/docker.sock
            depends_on:
              - zookeeper
            networks:
              my-network:
        
        networks:
          my-network:
            external: true
            name: ecommerce-network
        ```
        
    - docker-compose 시작
        
        ```bash
        docker-compose -f docker-compose-single-broker.yml up -d
        ```
        
- Zipkin
    
    ```bash
    docker run -d -p 9411:9411 `
    	--network ecommerce-network `
    	--name zipkin `
    	openzipkin/zipkin
    ```
    
- Prometheus
    
    ```bash
    docker run -d -p 9090:9090 `
    	--network ecommerce-network `
    	--name prometheus `
    	-v D:/git/spring-cloud-microservice/prometheus.yml:/etc/prometheus/prometheus.yml `
    	prom/prometheus
    ```
    
    ```bash
    docker run -d -p 9090:9090 `
    	--network ecommerce-network `
    	--name prometheus `
    	-v C:\git\spring-cloud-microservice/prometheus.yml:/etc/prometheus/prometheus.yml `
    	prom/prometheus
    ```
    
- Grafana
    
    ```bash
    docker run -d -p 3000:3000 `
    	--network ecommerce-network `
    	--name grafana `
    	grafana/grafana
    ```
    
- User Microservice
    - Docker Build
        
        ```bash
        docker build -t duckbill413/user-service:1.0 .
        ```
        
    - Docker Run
        
        ```bash
        docker run -d --network ecommerce-network `
        	--name user-service `
        	-e "spring.cloud.config.uri=http://config-service:8888" `
        	-e "spring.rabbitmq.host=rabbitmq" `
        	-e "management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans" `
        	-e "eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka" `
        	-e "logging.file=/api-logs/users-ws.log" `
        	duckbill413/user-service:1.0
        ```
        
- Order Microservice
    - Docker Build
        
        ```bash
        docker build -t duckbill413/order-service:1.0 .
        ```
        
    - Docker Run
        
        ```bash
        docker run -d --network ecommerce-network `
        	--name order-service `
        	-e "spring.cloud.config.uri=http://config-service:8888" `
        	-e "spring.rabbitmq.host=rabbitmq" `
        	-e "management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans" `
        	-e "eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka" `
        	-e "spring.datasource.url=jdbc:mysql://mysqldb:3306/my_db" `
        	-e "spring.datasource.username=root" `
        	-e "spring.datasource.password=1234" `
        	-e "logging.file=/api-logs/orders-ws.log" `
        	duckbill413/order-service:1.0
        ```
        
- Catalog Microservice
    - Docker Build
        
        ```bash
        docker build -t duckbill413/catalog-service:1.0 .
        ```
        
    - Docker Run
        
        ```bash
        docker run -d --network ecommerce-network `
        	--name catalog-service `
        	-e "spring.cloud.config.uri=http://config-service:8888" `
        	-e "spring.rabbitmq.host=rabbitmq" `
        	-e "eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka" `
        	-e "logging.file=/api-logs/catalogs-ws.log" `
        	duckbill413/catalog-service:1.0
        ```

### Docker 배포중 Trouble Shooting
1. Port 충돌 문제
    - window 포트 사용중 확인
        
        ```bash
        netstat -aon | findstr :<PORT>
        
        netstat -aon | findstr :8888
        ```
        
        ![Untitled 1](https://github.com/user-attachments/assets/f787ea08-6e36-4a5b-a8d7-a2e92ecba22f)
        
        포트에서 사용중인 PID를 조회
        
    
    - PID 를 사용하여 사용중인 프로세스 확인
        
        ```bash
        tasklist /fi "PID eq <PID>"
        
        ex)
        tasklist /fi "PID eq 122808"
        ```
        
        ![Untitled 2](https://github.com/user-attachments/assets/748a06fc-7e3d-4fa2-927f-9ab8ddcf9678)
    
        
    - 사용 중인 프로세스 종료
        
        ```bash
        taskkill /PID <PID> /F
        
        ex)
        taskkill /PID 122808 /F
        ```
        
        ![Untitled 3](https://github.com/user-attachments/assets/0856e6b9-1386-496a-9c36-7e64b6c9b30e)
    
2. Spring Security 6
3. Spring Boot 3 + Zipkin + Micrometer
