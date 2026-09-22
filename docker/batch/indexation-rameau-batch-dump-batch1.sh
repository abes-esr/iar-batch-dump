#!/bin/bash

exec java -jar /scripts/indexation-rameau-batch-dump-batch1.jar \
          -Duser.timezone=Europe/Paris \
          -Duser.language=fr

