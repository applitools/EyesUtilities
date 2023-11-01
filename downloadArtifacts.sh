#!/bin/bash

set -e

if [ $# -eq 0 ]; then
    echo "No arguments provided. Usage:"
    echo "    ./downloadArtifacts.sh <command> <Applitools batch url> <downloadPath> <APPLITOOLS_VIEW_KEY>"
    echo "example:"
    echo "    ./downloadArtifacts.sh [all|anidiffs|diffs|images|playback|report] <Applitools batch url> myDownloadDir <APPLITOOLS_VIEW_KEY>"
    exit 1
fi

downloadCommand=$1
url=$2
downloadPath=$3
key=$4

if [[ -z $downloadCommand ]]; then
  downloadCommand="all"
  echo "downloadCommand not provided. Downloading $downloadCommand"
fi

if [[ -z $key ]]; then
  echo "Applitools View Key is not specified. Using the value set as env variable: 'APPLITOOLS_VIEW_KEY'"
  if [[ -z $APPLITOOLS_VIEW_KEY ]]; then
    echo "'APPLITOOLS_VIEW_KEY' is not set"
    exit 1
  fi
  key=$APPLITOOLS_VIEW_KEY
fi

if [[ -z $downloadPath ]]; then
  downloadPath="./downloadArtifacts/$(date "+%Y-%m-%d/%H-%M-%S")"
  echo "downloadPath is NOT SET. Using default: '$downloadPath'"
else
  downloadPath=$downloadPath/$(date "+%Y-%m-%d/%H-%M-%S")
  echo "downloadPath: '$downloadPath'"
fi
mkdir -p $downloadPath

if [[ -z $url ]]; then
  echo "URL is not provided."
  exit 1
fi

function download() {
  echo "Downloading batch artifacts using command: '$1' from batch url: '$2' to: '$downloadPath/$1'"
  echo "java -jar jars/EyesUtilities_1.5.18.jar $1 -k "APPLITOOLS_VIEW_KEY"  -d $downloadPath/$1/{test_id}-{test_name}/file:{step_index}{step_tag}{artifact_type}.{file_ext} $url"
  java -jar jars/EyesUtilities_1.5.18.jar $1 -k $key  -d $downloadPath/$1/{test_id}-{test_name}/file:{step_index}{step_tag}{artifact_type}.{file_ext} $url
}

function downloadReport() {
  echo "Generating batch 'report' from batch url: '$2' to: '$downloadPath/$1'"
  echo "java -jar jars/EyesUtilities_1.5.18.jar $1 -k "APPLITOOLS_VIEW_KEY" -d $downloadPath/$1/report.html $url"
  mkdir -p $downloadPath/$1
  java -jar jars/EyesUtilities_1.5.18.jar report -k $key -d "$downloadPath/$1/report.html" -rt "customName" $url
}

function run() {
  echo "downloadArtifacts"
  if [[ "$downloadCommand" = "all" ]]; then
    download "anidiffs" $url
    download "diffs" $url
    download "images" $url
    download "playback" $url
    downloadReport "report" $url
  elif [[ "$downloadCommand" = "report" ]]; then
    downloadReport "report" $url
  else
    download "$downloadCommand" $url
  fi
}

run
