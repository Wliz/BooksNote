#! /bin/bash
x=10
y=20
if [ $x -gt $y ]
then
    echo "x is greater than y"
else
    echo "y is greater than x"
fi
echo $$

# 测试调用执行子脚本方式fork， exec，source
A=1
# Mark each name to be passed to child processes in the environment
export -p A

case $1 in
    "exec")
        echo -e "==> using exec ..."
        exec ./subShell.sh;;
    "source")
        echo -e "==> using source ..."
        source ./subShell.sh;;
    *)
        echo -e "==> using fork for default ..."
        ./subShell.sh;;
esac        

echo -e "in super script variable A = $A\n"        