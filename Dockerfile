###
# Image pour la compilation de indexation-rameau-batch-dump
FROM maven:3-eclipse-temurin-21 AS build-image
WORKDIR /build/
# Installation et configuration de la locale FR
RUN apt update && DEBIAN_FRONTEND=noninteractive apt -y install locales
RUN sed -i '/fr_FR.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen
ENV LANG=fr_FR.UTF-8
ENV LANGUAGE=fr_FR:fr
ENV LC_ALL=fr_FR.UTF-8
# On lance la compilation
# si on a un .m2 local on peut décommenter la ligne suivante pour
# éviter à maven de retélécharger toutes les dépendances
#COPY ./.m2/    /root/.m2/
COPY ./ /build/
RUN mvn --batch-mode \
    -Dmaven.test.skip=true \
    -Duser.timezone=Europe/Paris \
    -Duser.language=fr \
    package



###
# Image pour le module batch de indexation-rameau-batch-dump
# Remarque: l'image openjdk:21 n'est pas utilisée car nous avons besoin de cronie
#           qui n'est que disponible sous centos/rockylinux.
FROM rockylinux:8 AS batch-dump-image
WORKDIR /scripts/
# systeme pour les crontab
# cronie: remplacant de crond qui support le CTRL+C dans docker (sans ce système c'est compliqué de stopper le conteneur)
# gettext: pour avoir envsubst qui permet de gérer le template tasks.tmpl
RUN dnf install -y cronie gettext && \
    crond -V && rm -rf /etc/cron.*/*
COPY ./docker/batch/tasks.tmpl /etc/cron.d/tasks.tmpl
# Le JAR et le script pour le batch de IAR batch
RUN dnf install -y java-21-openjdk
COPY ./docker/batch/indexation-rameau-batch-dump-batch1.sh /scripts/indexation-rameau-batch-dump-batch1.sh
COPY --from=build-image /build/target/*.jar /scripts/indexation-rameau-batch-dump-batch1.jar
RUN chmod +x /scripts/*batch*.sh
# Les locales fr_FR
RUN dnf install langpacks-fr glibc-all-langpacks -y
ENV LANG=fr_FR.UTF-8
ENV LANGUAGE=fr_FR:fr
ENV LC_ALL=fr_FR.UTF-8
# Lancement de l'entrypoint et du démon crond
COPY ./docker/batch/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["crond", "-n"]


