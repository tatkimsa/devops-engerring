#!/bin/bash
gitusername="MuyleangIng"
gittoken="" 
echo "Starting repository deletion script"
read -p "Enter repository: " nameRepo
if [ -z "$nameRepo" ]; then
  echo "Repository name cannot be empty. Exiting."
  exit 1
fi
echo "Name of repository to delete: $nameRepo"

# Confirm before deletion
read -p "Are you sure you want to delete the repository $nameRepo? [y/N]: " confirm
if [[ $confirm != [yY] ]]; then
    echo "Repository deletion cancelled."
    exit 0
fi

# Deleting the repository
http_status=$(curl -s -o /dev/null -w "%{http_code}" -L \
  -X DELETE \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer  $gittoken" \
  "https://api.github.com/repos/$gitusername/$nameRepo")

# Check response status
if [ "$http_status" -eq 204 ]; then
    echo "Repository $nameRepo deleted successfully."
else
    echo "Failed to delete repository. HTTP status: $http_status"
fi