#!/usr/bin/env bash

# Exit immediately if any command in the script fails
set -e

# Configure GIT
git config --global user.name "Gluon Bot"
git config --global user.email "githubbot@gluonhq.com"

# Decrypt encrypted files
openssl aes-256-cbc -K $encrypted_dc87922a4c8c_key -iv $encrypted_dc87922a4c8c_iv -in .ci/secring.gpg.enc -out secring.gpg -d
if [[ ! -s secring.gpg ]]
   then echo "Decryption failed."
   exit 1
fi

# Release artifacts
./gradlew publish closeAndReleaseRepository -i -PsonatypeUsername=$SONATYPE_USERNAME -PsonatypePassword=$SONATYPE_PASSWORD -Psigning.keyId=$GPG_KEYNAME -Psigning.password=$GPG_PASSPHRASE -Psigning.secretKeyRingFile=$TRAVIS_BUILD_DIR/secring.gpg

# Update version by 1
newVersion=${TRAVIS_TAG%.*}.$((${TRAVIS_TAG##*.} + 1))

# Update project version to next snapshot version
sed -i -z "0,/version = $TRAVIS_TAG/s//version = $newVersion-SNAPSHOT/" gradle.properties

git commit gradle.properties -m "Prepare development of $newVersion"
git push https://gluon-bot:$GITHUB_PASSWORD@github.com/$TRAVIS_REPO_SLUG HEAD:master