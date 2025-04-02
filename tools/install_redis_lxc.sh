#!/bin/bash

# Nombre del contenedor LXC
CONTAINER_NAME="redis-container"

# Crear el contenedor LXC con Ubuntu 20.04
echo "Creando contenedor LXC con Ubuntu 20.04..."
lxc launch ubuntu:20.04 $CONTAINER_NAME

# Esperar a que el contenedor esté en ejecución
echo "Esperando a que el contenedor esté en ejecución..."
sleep 10

# Acceder al contenedor e instalar Redis
echo "Instalando Redis en el contenedor..."
lxc exec $CONTAINER_NAME -- bash -c "sudo apt update && sudo apt install -y redis-server"

# Configurar Redis (permite conexiones externas y otras configuraciones)
echo "Configurando Redis..."
lxc exec $CONTAINER_NAME -- bash -c "sudo sed -i 's/^# bind 127.0.0.1 ::1$/bind 0.0.0.0/' /etc/redis/redis.conf"
lxc exec $CONTAINER_NAME -- bash -c "sudo sed -i 's/^protected-mode yes/protected-mode no/' /etc/redis/redis.conf"
# (Opcional) Si quieres poner una contraseña en Redis, descomenta y ajusta la siguiente línea:
# lxc exec $CONTAINER_NAME -- bash -c "echo 'requirepass tu_contraseña_segura' | sudo tee -a /etc/redis/redis.conf"

# Iniciar el servidor Redis
echo "Iniciando Redis..."
lxc exec $CONTAINER_NAME -- bash -c "sudo systemctl start redis-server"
lxc exec $CONTAINER_NAME -- bash -c "sudo systemctl enable redis-server"

# Verificar que Redis está corriendo
echo "Verificando que Redis está corriendo..."
lxc exec $CONTAINER_NAME -- redis-cli ping

# Exponer el puerto Redis (6379) para acceder desde fuera del contenedor
echo "Exponiendo el puerto Redis (6379) en el host..."
lxc config device add $CONTAINER_NAME redis-port proxy listen=tcp:0.0.0.0:6379 connect=tcp:127.0.0.1:6379

# Mostrar instrucciones finales
echo "Redis está corriendo dentro del contenedor LXC '$CONTAINER_NAME'."
echo "Puedes acceder a Redis desde fuera del contenedor utilizando el siguiente comando:"
echo "redis-cli -h <ip_del_host> -p 6379"
echo "Si configuraste una contraseña, puedes usar el siguiente comando:"
echo "redis-cli -h <ip_del_host> -p 6379 -a tu_contraseña_segura"

echo "¡Listo! Redis está configurado y listo para usar."
