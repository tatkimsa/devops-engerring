#!/bin/bash

echo "Do you want to backup or restore a volume? (Enter 1 for backup, 2 for restore)"
read action

backup_path="/root/docker/backup"  # Ensure this path is correct and accessible

if [ "$action" == "1" ]; then
    echo "Enter the name of the Docker volume you want to backup:"
    read volume_name
    if [ -z "$volume_name" ]; then
        echo "Volume name cannot be empty."
        exit 1
    fi
    backup_filename="${backup_path}/${volume_name}_backup_$(date +%Y%m%d).tar.gz"
    docker run --rm -v ${volume_name}:/volume -v ${backup_path}:/backup alpine tar czf /backup/$(basename "${backup_filename}") -C /volume ./
    echo "Backup of ${volume_name} completed. File: ${backup_filename}"
elif [ "$action" == "2" ]; then
    echo "Enter the full path of the backup file:"
    read backup_file
    if [ ! -f "$backup_file" ]; then
        echo "Backup file does not exist."
        exit 1
    fi
    echo "Enter the name of the new Docker volume to restore to:"
    read new_volume_name
    if [ -z "$new_volume_name" ]; then
        echo "Volume name cannot be empty."
        exit 1
    fi
    docker volume create ${new_volume_name}
    docker run --rm -v ${new_volume_name}:/volume -v ${backup_path}:/backup alpine tar xzf /backup/$(basename "${backup_file}") -C /volume
    echo "Restoration completed to volume: ${new_volume_name}"
else
    echo "Invalid action selected. Exiting."
fi
