#!/bin/bash
echo "Fixing packages name!"
for file in $(find .)
do
	if [[ $file == *".java" ]]
	then
		for word in $(cat $file)
		do
			if [[ $word == *"gspd.ispd."* ]]
			then
				echo -n "Fixing $file ..."
                sed $file -e 's/gspd.ispd./ispd./g' -i
                echo " OK"
			fi
		done
	fi
done
