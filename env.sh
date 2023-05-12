export JAVA8_HOME=~/.jdks/1.8
export JAVA_HOME=${JAVA8_HOME}
export PATH=$JAVA_HOME/bin:$PATH

# Keep this in sync with across-bamboo-specs/docker-compose-ax-5.yml
export MAVEN_HOME=~/tools/apache-maven-3.8.6
export PATH=$MAVEN_HOME/bin:$PATH

echo "Warning: Across is still using old plugins that are not multi-thread safe"

alias mi='mvn install'
alias mci='mvn clean install'
# TODO: add -T1C when multi-thread issue is fixed:
alias miwt='mvn install -DskipTests -Dmaven.javadoc.skip=true -Djacoco.skip=true'
alias mciwt='mvn clean install -DskipTests -Dmaven.javadoc.skip=true -Djacoco.skip=true'
