#!/bin/bash

# NOTE : File should be executed from the res directory and full resolution image should be in the drawable folder

ldpi=120
mdpi=160
hdpi=240
xhdpi=320
xxhdpi=480
xxxhdpi=640

# Scaled from dpi & dp constants using dp*(dpi/160)
ldpi_s="320x240"
mdpi_s="470x320"
hdpi_s="960x720"
xhdpi_s="1920x1440"

gen_low=0
is_icon=0

while getopts ":li" opt; do
    case $opt in
        l)
            gen_low=1
            ;;
        i)
            is_icon=1
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            ;;
    esac
done

# Convert Images

img_name=$1
arg_sum=$((gen_low+is_icon))

# Hardcoded image switch (Change later)
if [ $arg_sum == 1 ]; then
    img_name=$2
fi

if [ $arg_sum == 2 ]; then
    img_name=$3
fi

# Help Dialog
if [ $img_name == "/?" ]; then
    echo "Usage: execute from res folder and target an image in the drawable folder"
    echo "Use -i to generate an icon image (xxxdpi) and -l to generate a low-dpi image"
    echo "Syntax: AndroidImageConverter -i -l \"image name\""
    exit 0
fi

# Low resolution
if [ $gen_low == 1 ]
    then
        convert -units PixelsPerInch drawable/$img_name -density $ldpi -resize $ldpi_s drawable-ldpi/$img_name
fi

# Only for icons (xxhdpi)
if [ $is_icon == 1 ]
    then
        convert -units PixelsPerInch drawable/$img_name -density $xxxhdpi -resize $xhdpi_s drawable-xxxhdpi/$img_name
fi

convert -units PixelsPerInch drawable/$img_name -density $mdpi -resize $mdpi_s drawable-mdpi/$img_name

convert -units PixelsPerInch drawable/$img_name -density $hdpi -resize $hdpi_s drawable-hdpi/$img_name

convert -units PixelsPerInch drawable/$img_name -density $xhdpi -resize $xhdpi_s drawable-xhdpi/$img_name

convert -units PixelsPerInch drawable/$img_name -density $xxhdpi -resize $xhdpi_s drawable-xxhdpi/$img_name
