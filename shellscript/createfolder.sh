#!/bin/bash

# Define the directory name
DIR_NAME="MyFolder"
cd /root/shellscript
# Create the directory if it doesn't already exist
if [ ! -d "$DIR_NAME" ]; then
    cd /root/shellscript
    mkdir "$DIR_NAME"
fi

touch "$DIR_NAME/file.txt"

echo "Directory and file have been created."
