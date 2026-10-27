#!/bin/bash

# Build script for Netlify
mkdir -p netlify-site
cp src/main/resources/static/*.html netlify-site/

echo "Build completed"
