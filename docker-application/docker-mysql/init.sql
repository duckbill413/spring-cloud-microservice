CREATE DATABASE my_db;

USE my_db;

# root 계정에 대하여 모든 ip 에서의 접근 권한 부여
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;