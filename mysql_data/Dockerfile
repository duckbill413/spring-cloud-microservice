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