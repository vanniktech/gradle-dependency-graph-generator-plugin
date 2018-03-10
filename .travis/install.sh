#!/bin/bash

if [[ $TRAVIS_OS_NAME == 'osx' ]]; then
  brew install graphviz
else
  yes | sudo apt-get install graphviz
fi