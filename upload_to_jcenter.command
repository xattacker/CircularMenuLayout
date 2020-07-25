SOURCE="${BASH_SOURCE[0]}"
DIR="$( dirname "$SOURCE" )"
while [ -h "$SOURCE" ]
do 
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
  DIR="$( cd -P "$( dirname "$SOURCE"  )" && pwd )"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

cd "$DIR"

./gradlew clean build bintrayUpload -PbintrayUser=PRIVATE_BINTRAY_USERNAME -PbintrayKey=PRIVATE_BINTRAY_KEY -PdryRun=false
read -p "Press enter to continue"
