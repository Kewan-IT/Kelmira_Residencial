#!/bin/bash
set -e

echo ">>> A instalar MariaDB..."
sudo apt-get update -y
sudo apt-get install -y mariadb-server mariadb-client

echo ">>> A iniciar serviço MariaDB..."
sudo service mariadb start

echo ">>> A configurar base de dados e utilizador..."
sudo mariadb <<'EOF'
CREATE DATABASE IF NOT EXISTS pensao_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'pensao_user'@'%' IDENTIFIED BY 'pensao_pass';
GRANT ALL PRIVILEGES ON pensao_db.* TO 'pensao_user'@'%';
FLUSH PRIVILEGES;
EOF

echo ">>> Permitir ligações remotas (bind-address)..."
sudo sed -i "s/^bind-address.*/bind-address = 0.0.0.0/" /etc/mysql/mariadb.conf.d/50-server.cnf || true
sudo service mariadb restart

echo ">>> Verificar versões instaladas:"
java -version
mvn -version
mariadb --version

echo ">>> Setup concluído!"
echo "Base de dados: pensao_db"
echo "Utilizador: pensao_user"
echo "Password: pensao_pass"
echo ""
echo "String de ligação JDBC para application.properties:"
echo "spring.datasource.url=jdbc:mariadb://localhost:3306/pensao_db"
echo "spring.datasource.username=pensao_user"
echo "spring.datasource.password=pensao_pass"
