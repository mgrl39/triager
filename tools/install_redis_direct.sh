#!/bin/bash

# Script para instalar Redis directamente en el sistema
echo "Actualizando repositorios..."
sudo apt update

echo "Instalando Redis Server..."
sudo apt install -y redis-server

# Configurar Redis para permitir conexiones externas
echo "Configurando Redis..."
sudo sed -i 's/^bind 127.0.0.1 ::1$/bind 0.0.0.0/' /etc/redis/redis.conf
sudo sed -i 's/^protected-mode yes/protected-mode no/' /etc/redis/redis.conf

# (Opcional) Si quieres poner una contraseña en Redis, descomenta y ajusta la siguiente línea:
# echo 'requirepass tu_contraseña_segura' | sudo tee -a /etc/redis/redis.conf

# Reiniciar el servidor Redis para aplicar cambios
echo "Reiniciando Redis..."
sudo systemctl restart redis-server
sudo systemctl enable redis-server

# Verificar que Redis está corriendo
echo "Verificando que Redis está corriendo..."
redis-cli ping

# Mostrar información IP para la conexión
echo "Redis está corriendo y configurado para aceptar conexiones externas."
echo "Tu dirección IP es:"
hostname -I | awk '{print $1}'
echo "Puedes acceder a Redis usando:"
echo "redis-cli -h <tu_ip> -p 6379"
echo "Si configuraste una contraseña, puedes usar:"
echo "redis-cli -h <tu_ip> -p 6379 -a tu_contraseña_segura"

echo "¡Listo! Redis está configurado y listo para usar." 