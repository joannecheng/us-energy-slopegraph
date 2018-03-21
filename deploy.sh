#!/bin/bash

set -e
echo "Building cljs files..."

lein clean
lein cljsbuild once min
cd resources/public

git init
git add .
git commit -m "Deploy to GitHub Pages"

echo "Pushing to GitHub..."
git push --force "git@github.com:joannecheng/us-energy-slopegraph.git" master:gh-pages

echo "Cleaning up..."
rm -rf .git
cd ../../
