# Script for starting the game
# To be run in the root of the build tree
# No jar files used

# execute main in new terminal
export DISPLAY=:0.0
java -classpath ./out:./lib/repast.jar:./lib/SAJaS.jar:./lib/jade.jar Main
