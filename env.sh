echo "Warning: Across is still using old plugins that are not multi-thread safe"

alias mi='mvn install'
alias mci='mvn clean install'
# TODO: add -T1C when multi-thread issue is fixed:
alias miwt='mvn install -DskipTests'
alias mciwt='mvn clean install -DskipTests'

export JAVA8_HOME=~/.jdks/1.8
export JAVA_HOME=${JAVA8_HOME}
export PATH=$JAVA_HOME/bin:$PATH
