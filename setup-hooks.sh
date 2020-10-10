#!/bin/sh
set -eu

if [ ! -d .git/hooks ]
then
  mkdir .git/hooks
fi

printf "#!/bin/sh\n./gradlew check" > .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
