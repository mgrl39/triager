#!/bin/bash

# Script para instalar RedisInsight desde Flathub
# RedisInsight es una GUI para Redis desarrollada por Redis, Inc.

# Colores para la salida en terminal
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Instalador de RedisInsight desde Flathub${NC}"
echo "--------------------------------------------------------"
echo "RedisInsight es una interfaz gráfica intuitiva y eficiente para Redis"
echo "que te permite interactuar con tus bases de datos y gestionar tus datos."
echo "--------------------------------------------------------"

# Verificar si Flatpak está instalado
if ! command -v flatpak &> /dev/null; then
    echo -e "${RED}Flatpak no está instalado en el sistema.${NC}"
    echo "Instalando Flatpak..."
    sudo apt update
    sudo apt install -y flatpak
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Error al instalar Flatpak. Por favor, instálalo manualmente.${NC}"
        exit 1
    else
        echo -e "${GREEN}Flatpak instalado correctamente.${NC}"
    fi
else
    echo -e "${GREEN}Flatpak ya está instalado.${NC}"
fi

# Verificar si Flathub está configurado como repositorio
if ! flatpak remotes | grep -q "flathub"; then
    echo "Configurando Flathub como repositorio..."
    flatpak remote-add --if-not-exists flathub https://flathub.org/repo/flathub.flatpakrepo
    
    if [ $? -ne 0 ]; then
        echo -e "${RED}Error al configurar Flathub. Por favor, configúralo manualmente.${NC}"
        exit 1
    else
        echo -e "${GREEN}Flathub configurado correctamente.${NC}"
    fi
else
    echo -e "${GREEN}Flathub ya está configurado como repositorio.${NC}"
fi

# Instalamos RedisInsight
echo "Instalando RedisInsight desde Flathub..."
flatpak install flathub com.redis.RedisInsight -y

if [ $? -ne 0 ]; then
    echo -e "${RED}Error al instalar RedisInsight.${NC}"
    exit 1
else
    echo -e "${GREEN}RedisInsight instalado correctamente.${NC}"
fi
