#! /bin/bash

echo "PID for subShell = $$"
echo "In subShell script get variable A = $A from super script(super.sh)"

A=2
export A
echo -e "in subShell.sh variable A = $A"